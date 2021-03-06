package com.cema.health.controllers;

import com.cema.health.constants.Messages;
import com.cema.health.domain.Illness;
import com.cema.health.domain.Note;
import com.cema.health.entities.CemaIllness;
import com.cema.health.exceptions.NotFoundException;
import com.cema.health.exceptions.UnauthorizedException;
import com.cema.health.mapping.Mapping;
import com.cema.health.repositories.IllnessRepository;
import com.cema.health.services.authorization.AuthorizationService;
import com.cema.health.services.client.administration.AdministrationClientService;
import com.cema.health.services.client.bovine.BovineClientService;
import com.cema.health.services.database.DatabaseService;
import com.cema.health.services.notification.NotificationService;
import com.cema.health.services.validation.HealthValidationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1")
@Api(produces = "application/json", value = "Allows interaction with the illness database. V1")
@Validated
@Slf4j
public class IllnessController {

    private static final String BASE_URL = "/illness/";

    private final IllnessRepository illnessRepository;
    private final Mapping<CemaIllness, Illness> illnessMapping;
    private final AuthorizationService authorizationService;
    private final AdministrationClientService administrationClientService;
    private final DatabaseService databaseService;
    private final HealthValidationService healthValidationService;
    private final BovineClientService bovineClientService;
    private final NotificationService notificationService;

    public IllnessController(IllnessRepository illnessRepository, Mapping<CemaIllness, Illness> illnessMapping,
                             AuthorizationService authorizationService,
                             AdministrationClientService administrationClientService, DatabaseService databaseService,
                             HealthValidationService healthValidationService, BovineClientService bovineClientService,
                             NotificationService notificationService) {
        this.illnessRepository = illnessRepository;
        this.illnessMapping = illnessMapping;
        this.authorizationService = authorizationService;
        this.administrationClientService = administrationClientService;
        this.databaseService = databaseService;
        this.healthValidationService = healthValidationService;
        this.bovineClientService = bovineClientService;
        this.notificationService = notificationService;
    }

    @ApiOperation(value = "Launch notifications")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Notifications Sent")
    })
    @GetMapping(value = BASE_URL + "/notifications/send", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> sendNotifications() {
        notificationService.notifyAllUsers();

        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Register a new illness to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Illness created successfully"),
            @ApiResponse(code = 409, message = "The illness you were trying to create already exists"),
            @ApiResponse(code = 401, message = "You are not allowed to register this illness")
    })
    @PostMapping(value = BASE_URL, produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Illness> registerIllness(
            @ApiParam(
                    value = "Illness data to be inserted.")
            @RequestBody @Valid Illness illness) {

        log.info("Request to register new illness");

        String cuig = illness.getEstablishmentCuig();
        if (!authorizationService.isOnTheSameEstablishment(cuig)) {
            throw new UnauthorizedException(String.format(Messages.OUTSIDE_ESTABLISHMENT, cuig));
        }

        healthValidationService.validateIllness(illness);

        List<CemaIllness> illnesses = illnessRepository.findCemaIllnessByDiseaseNameAndBovineTagAndEstablishmentCuig(illness.getDiseaseName(), illness.getBovineTag(), cuig);
        if (!illnesses.isEmpty()) {
            for (CemaIllness cemaIllness : illnesses) {
                healthValidationService.validateIllnessDate(cemaIllness, illness);
            }
        }

        bovineClientService.validateBovine(illness.getBovineTag(), illness.getEstablishmentCuig());

        CemaIllness newIllness = illnessMapping.mapDomainToEntity(illness);

        newIllness = databaseService.saveCemaIllness(newIllness, illness.getDiseaseName(), illness.getNotes().stream().map(Note::getContent).collect(Collectors.toList()));

        Illness updatedIllness = illnessMapping.mapEntityToDomain(newIllness);

        return new ResponseEntity<>(updatedIllness, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Retrieve illness from cuig sent data", response = Illness.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully found illness"),
            @ApiResponse(code = 404, message = "Illness not found")
    })
    @GetMapping(value = BASE_URL + "{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Illness> lookUpIllnessById(
            @ApiParam(
                    value = "The cuig of the illness you are looking for.",
                    example = "123")
            @PathVariable("id") UUID id) {

        log.info("Request for illness with id {}", id);


        Optional<CemaIllness> cemaIllness = illnessRepository.findById(id);
        if (!cemaIllness.isPresent()) {
            throw new NotFoundException(String.format("Illness with id %s doesn't exits", id));
        }
        String cuig = cemaIllness.get().getEstablishmentCuig();

        if (!authorizationService.isOnTheSameEstablishment(cuig)) {
            throw new UnauthorizedException(String.format(Messages.OUTSIDE_ESTABLISHMENT, cuig));
        }

        Illness illness = illnessMapping.mapEntityToDomain(cemaIllness.get());

        return new ResponseEntity<>(illness, HttpStatus.OK);
    }

    @ApiOperation(value = "Retrieve illness from bovine tag data", response = Illness.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully found illness"),
            @ApiResponse(code = 404, message = "Illness not found")
    })
    @GetMapping(value = BASE_URL + "bovine/{tag}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Illness> lookUpIllnessByTag(
            @ApiParam(
                    value = "The tag of the ill bovine you are looking for.",
                    example = "123")
            @PathVariable("tag") String tag,
            @ApiParam(
                    value = "The cuig of the establishment of the batch. If the user is not admin will be ignored.",
                    example = "312")
            @RequestParam(value = "cuig", required = false) String cuig) {

        log.info("Request for illness with bovine tag {}", tag);

        if (!authorizationService.isAdmin() || !StringUtils.hasLength(cuig)) {
            cuig = authorizationService.getCurrentUserCuig();
        }

        List<CemaIllness> cemaIllness = illnessRepository.findIllBovine(tag, cuig);
        if (cemaIllness == null || cemaIllness.isEmpty()) {
            throw new NotFoundException(String.format("Bovine %s is not ill", tag));
        }

        Illness illness = illnessMapping.mapEntityToDomain(cemaIllness.get(0));

        return new ResponseEntity<>(illness, HttpStatus.OK);
    }

    @ApiOperation(value = "Modifies an existent Illness, also used to add notes")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Illness modified successfully"),
            @ApiResponse(code = 404, message = "The illness you were trying to modify doesn't exists"),
            @ApiResponse(code = 401, message = "You are not allowed to update this illness")
    })
    @PutMapping(value = BASE_URL + "{id}", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Illness> updateIllness(
            @ApiParam(
                    value = "The id of the illness we are looking for.",
                    example = "123")
            @PathVariable("id") UUID id,
            @ApiParam(
                    value = "The illness data we are modifying. Cuig cannot be modified and will be ignored.")
            @RequestBody Illness illness) {

        log.info("Request to modify illness with id: {}", id);

        Optional<CemaIllness> cemaIllnessOptional = illnessRepository.findById(id);
        if (!cemaIllnessOptional.isPresent()) {
            throw new NotFoundException(String.format("Illness with id %s doesn't exits", id));
        }
        CemaIllness cemaIllness = cemaIllnessOptional.get();
        String cuig = cemaIllness.getEstablishmentCuig();

        if (!authorizationService.isOnTheSameEstablishment(cuig)) {
            throw new UnauthorizedException(String.format(Messages.OUTSIDE_ESTABLISHMENT, cuig));
        }

        cemaIllness = illnessMapping.updateDomainWithEntity(illness, cemaIllness);

        List<CemaIllness> illnesses = illnessRepository.findCemaIllnessByDiseaseNameAndBovineTagAndEstablishmentCuig(cemaIllness.getDisease().getName(), cemaIllness.getBovineTag(), cuig);
        if (!illnesses.isEmpty()) {
            for (CemaIllness existingCemaIllness : illnesses.stream().filter(cemaIllness1 -> cemaIllness1.getId() != id).collect(Collectors.toList())) {
                healthValidationService.validateIllnessDate(existingCemaIllness, illnessMapping.mapEntityToDomain(cemaIllness));
            }
        }

        cemaIllness = illnessRepository.save(cemaIllness);

        List<Note> notes = illness.getNotes();

        if (notes != null && !notes.isEmpty()) {
            cemaIllness = databaseService.addNotesToIllness(cemaIllness, notes);
        }

        Illness updatedIllness = illnessMapping.mapEntityToDomain(cemaIllness);

        return new ResponseEntity<>(updatedIllness, HttpStatus.OK);
    }

    @ApiOperation(value = "Delete an existing illness by name")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Illness deleted successfully"),
            @ApiResponse(code = 404, message = "The illness you were trying to reach is not found")
    })
    @DeleteMapping(value = BASE_URL + "{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Illness> deleteIllness(
            @ApiParam(
                    value = "The id for the illness we are looking for.",
                    example = "123")
            @PathVariable("id") UUID id) {
        log.info("Request to delete illness with id {}", id);

        Optional<CemaIllness> cemaIllnessOptional = illnessRepository.findById(id);
        if (!cemaIllnessOptional.isPresent()) {
            throw new NotFoundException(String.format("Illness with id %s doesn't exits", id));
        }
        CemaIllness cemaIllness = cemaIllnessOptional.get();
        String cuig = cemaIllness.getEstablishmentCuig();

        if (!authorizationService.isOnTheSameEstablishment(cuig)) {
            throw new UnauthorizedException(String.format(Messages.OUTSIDE_ESTABLISHMENT, cuig));
        }

        log.info("Illness exists, deleting");
        illnessRepository.delete(cemaIllness);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "Retrieve list of illnesses", response = Illness.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Listed all illnesses", responseHeaders = {
                    @ResponseHeader(name = "total-elements", response = String.class, description = "Total number of search results"),
                    @ResponseHeader(name = "total-pages", response = String.class, description = "Total number of pages to navigate"),
                    @ResponseHeader(name = "current-page", response = String.class, description = "The page being returned, zero indexed")
            })
    })
    @GetMapping(value = BASE_URL + "list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Illness>> listIllnesses(
            @ApiParam(
                    value = "The page you want to retrieve.",
                    example = "1")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @ApiParam(
                    value = "The maximum number of illness entries to return per page.",
                    example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

        String cuig = authorizationService.getCurrentUserCuig();
        Pageable paging = PageRequest.of(page, size, Sort.by("startingDate").descending());

        Page<CemaIllness> cemaIllnessPage;
        if (authorizationService.isAdmin()) {
            cemaIllnessPage = illnessRepository.findAll(paging);
        } else {
            cemaIllnessPage = illnessRepository.findAllByEstablishmentCuig(cuig, paging);
        }

        List<CemaIllness> cemaIllnesses = cemaIllnessPage.getContent();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("total-elements", String.valueOf(cemaIllnessPage.getTotalElements()));
        responseHeaders.set("total-pages", String.valueOf(cemaIllnessPage.getTotalPages()));
        responseHeaders.set("current-page", String.valueOf(cemaIllnessPage.getNumber()));

        List<Illness> illnesses = cemaIllnesses.stream().map(illnessMapping::mapEntityToDomain).collect(Collectors.toList());

        return ResponseEntity.ok().headers(responseHeaders).body(illnesses);
    }

    @ApiOperation(value = "Retrieve a list of illnesses matching the sent data", response = Illness.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully found illnesses", responseHeaders = {
                    @ResponseHeader(name = "total-elements", response = String.class, description = "Total number of search results"),
                    @ResponseHeader(name = "total-pages", response = String.class, description = "Total number of pages to navigate"),
                    @ResponseHeader(name = "current-page", response = String.class, description = "The page being returned, zero indexed")
            })
    })
    @PostMapping(value = BASE_URL + "search", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Illness>> searchIllnesses(
            @ApiParam(
                    value = "The page you want to retrieve.",
                    example = "1")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @ApiParam(
                    value = "The maximum number of illnesses to return per page.",
                    example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "3") int size,
            @ApiParam(
                    value = "The illness data we are searching")
            @RequestBody Illness illness) {

        if (!authorizationService.isAdmin()) {
            illness.setEstablishmentCuig(authorizationService.getCurrentUserCuig());
        }

        CemaIllness cemaIllness = illnessMapping.mapDomainToEntity(illness);

        Page<CemaIllness> cemaIllnessPage = databaseService.searchIllnesses(cemaIllness, page, size);

        List<CemaIllness> cemaIllnesses = cemaIllnessPage.getContent();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("total-elements", String.valueOf(cemaIllnessPage.getTotalElements()));
        responseHeaders.set("total-pages", String.valueOf(cemaIllnessPage.getTotalPages()));
        responseHeaders.set("current-page", String.valueOf(cemaIllnessPage.getNumber()));

        List<Illness> illnesses = cemaIllnesses.stream().map(illnessMapping::mapEntityToDomain).collect(Collectors.toList());

        return ResponseEntity.ok().headers(responseHeaders).body(illnesses);
    }

    @ApiOperation(value = "Retrieve a list of illnesses matching the sent data", response = Illness.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully found illnesses", responseHeaders = {
                    @ResponseHeader(name = "total-elements", response = String.class, description = "Total number of search results"),
                    @ResponseHeader(name = "total-pages", response = String.class, description = "Total number of pages to navigate"),
                    @ResponseHeader(name = "current-page", response = String.class, description = "The page being returned, zero indexed")
            })
    })
    @GetMapping(value = BASE_URL + "search", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Illness>> getIllnesses(
            @ApiParam(
                    value = "The page you want to retrieve.",
                    example = "1")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @ApiParam(
                    value = "The maximum number of illnesses to return per page.",
                    example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "3") int size,
            @ApiParam(
                    value = "The tag of the sick bovine you are looking for", example = "54654")
            @RequestParam(value = "tag", required = false) String tag,
            @ApiParam(
                    value = "The endingDate of the illness you are searching", example = "2022-01-25 00:14:00")
            @RequestParam(value = "endingDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endingDate,
            @ApiParam(
                    value = "The startingDate of the illness you are searching", example = "2022-01-25 00:14:00")
            @RequestParam(value = "startingDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startingDate,
            @ApiParam(
                    value = "The cuig of the sick bovine you are looking for", example = "321")
            @RequestParam(value = "cuig", required = false) String cuig,
            @ApiParam(
                    value = "The worker of the sick bovine you are looking for", example = "merlin")
            @RequestParam(value = "worker", required = false) String worker) {

        if (!authorizationService.isAdmin()) {
            cuig = authorizationService.getCurrentUserCuig();
        }

        CemaIllness cemaIllness = CemaIllness.builder()
                .bovineTag(tag)
                .endingDate(endingDate)
                .startingDate(startingDate)
                .establishmentCuig(cuig)
                .workerUsername(worker)
                .build();

        Page<CemaIllness> cemaIllnessPage = databaseService.searchIllnesses(cemaIllness, page, size);

        List<CemaIllness> cemaIllnesses = cemaIllnessPage.getContent();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("total-elements", String.valueOf(cemaIllnessPage.getTotalElements()));
        responseHeaders.set("total-pages", String.valueOf(cemaIllnessPage.getTotalPages()));
        responseHeaders.set("current-page", String.valueOf(cemaIllnessPage.getNumber()));

        List<Illness> illnesses = cemaIllnesses.stream().map(illnessMapping::mapEntityToDomain).collect(Collectors.toList());

        return ResponseEntity.ok().headers(responseHeaders).body(illnesses);
    }
}
