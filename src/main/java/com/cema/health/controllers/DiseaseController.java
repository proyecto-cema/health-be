package com.cema.health.controllers;

import com.cema.health.constants.Messages;
import com.cema.health.domain.Disease;
import com.cema.health.entities.CemaDisease;
import com.cema.health.entities.CemaIllness;
import com.cema.health.exceptions.AlreadyExistsException;
import com.cema.health.exceptions.NotFoundException;
import com.cema.health.exceptions.UnauthorizedException;
import com.cema.health.exceptions.ValidationException;
import com.cema.health.mapping.Mapping;
import com.cema.health.repositories.DiseaseRepository;
import com.cema.health.repositories.IllnessRepository;
import com.cema.health.services.authorization.AuthorizationService;
import com.cema.health.services.client.administration.AdministrationClientService;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1")
@Api(produces = "application/json", value = "Allows interaction with the disease database. V1")
@Validated
@Slf4j
public class DiseaseController {

    private static final String BASE_URL = "/disease/";

    private final DiseaseRepository diseaseRepository;
    private final Mapping<CemaDisease, Disease> diseaseMapping;
    private final AuthorizationService authorizationService;
    private final AdministrationClientService administrationClientService;
    private final IllnessRepository illnessRepository;

    public DiseaseController(DiseaseRepository diseaseRepository, Mapping<CemaDisease, Disease> diseaseMapping, AuthorizationService authorizationService, AdministrationClientService administrationClientService, IllnessRepository illnessRepository) {
        this.diseaseRepository = diseaseRepository;
        this.diseaseMapping = diseaseMapping;
        this.authorizationService = authorizationService;
        this.administrationClientService = administrationClientService;
        this.illnessRepository = illnessRepository;
    }

    @ApiOperation(value = "Register a new disease to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Disease created successfully"),
            @ApiResponse(code = 409, message = "The disease you were trying to create already exists"),
            @ApiResponse(code = 401, message = "You are not allowed to register this disease")
    })
    @PostMapping(value = BASE_URL, produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Disease> registerDisease(
            @ApiParam(
                    value = "Disease data to be inserted.")
            @RequestBody @Valid Disease disease) {

        log.info("Request to register new disease");

        String cuig = disease.getEstablishmentCuig();
        if (!authorizationService.isOnTheSameEstablishment(cuig)) {
            throw new UnauthorizedException(String.format(Messages.OUTSIDE_ESTABLISHMENT, cuig));
        }

        CemaDisease cemaDisease = diseaseRepository.findCemaDiseaseByNameAndEstablishmentCuigIgnoreCase(disease.getName(), cuig);
        if (cemaDisease != null) {
            throw new AlreadyExistsException(String.format("Disease with name %s already exits", disease.getName()));
        }
        administrationClientService.validateEstablishment(cuig);

        CemaDisease newDisease = diseaseMapping.mapDomainToEntity(disease);

        newDisease = diseaseRepository.save(newDisease);

        Disease updatedDisease = diseaseMapping.mapEntityToDomain(newDisease);

        return new ResponseEntity<>(updatedDisease, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Retrieve disease from cuig sent data", response = Disease.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully found disease"),
            @ApiResponse(code = 404, message = "Disease not found")
    })
    @GetMapping(value = BASE_URL + "{name}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Disease> lookUpDiseaseByName(
            @ApiParam(
                    value = "The name of the disease you are looking for.",
                    example = "123")
            @PathVariable("name") String name,
            @ApiParam(
                    value = "The cuig of the establishment of the disease. If the user is not admin will be ignored.",
                    example = "312")
            @RequestParam(value = "cuig", required = false) String cuig) {

        log.info("Request for disease with name {}", name);

        if (!authorizationService.isAdmin() || !StringUtils.hasLength(cuig)) {
            cuig = authorizationService.getCurrentUserCuig();
        }

        CemaDisease cemaDisease = diseaseRepository.findCemaDiseaseByNameAndEstablishmentCuigIgnoreCase(name, cuig);
        if (cemaDisease == null) {
            throw new NotFoundException(String.format("Disease with name %s doesn't exits", name));
        }

        Disease disease = diseaseMapping.mapEntityToDomain(cemaDisease);

        return new ResponseEntity<>(disease, HttpStatus.OK);
    }

    @ApiOperation(value = "Modifies an existent Disease")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Disease modified successfully"),
            @ApiResponse(code = 404, message = "The disease you were trying to modify doesn't exists"),
            @ApiResponse(code = 401, message = "You are not allowed to update this disease")
    })
    @PutMapping(value = BASE_URL + "{name}", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Disease> updateDisease(
            @ApiParam(
                    value = "The name of the disease we are looking for.",
                    example = "123")
            @PathVariable("name") String name,
            @ApiParam(
                    value = "The disease data we are modifying. Cuig cannot be modified and will be ignored.")
            @RequestBody Disease disease,
            @ApiParam(
                    value = "The cuig of the establishment of the disease. If the user is not admin will be ignored.",
                    example = "321")
            @RequestParam(value = "cuig") String cuig) {

        log.info("Request to modify disease with name: {}", name);

        if (!authorizationService.isAdmin()) {
            cuig = authorizationService.getCurrentUserCuig();
        }
        administrationClientService.validateEstablishment(cuig);
        CemaDisease cemaDisease = diseaseRepository.findCemaDiseaseByNameAndEstablishmentCuigIgnoreCase(name, cuig);
        if (cemaDisease == null) {
            log.info("Disease doesn't exists");
            throw new NotFoundException(String.format("Disease with name %s doesn't exits", name));
        }

        disease.setEstablishmentCuig(cuig);

        cemaDisease = diseaseMapping.updateDomainWithEntity(disease, cemaDisease);

        cemaDisease = diseaseRepository.save(cemaDisease);

        Disease updatedDisease = diseaseMapping.mapEntityToDomain(cemaDisease);

        return new ResponseEntity<>(updatedDisease, HttpStatus.OK);
    }

    @ApiOperation(value = "Delete an existing disease by name")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Disease deleted successfully"),
            @ApiResponse(code = 404, message = "The disease you were trying to reach is not found")
    })
    @DeleteMapping(value = BASE_URL + "{name}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Disease> deleteDisease(
            @ApiParam(
                    value = "The name for the disease we are looking for.",
                    example = "123")
            @PathVariable("name") String name,
            @ApiParam(
                    value = "The cuig of the establishment of the disease. If the user is not admin will be ignored.",
                    example = "312")
            @RequestParam(value = "cuig", required = false) String cuig) {

        if (!authorizationService.isAdmin() || !StringUtils.hasLength(cuig)) {
            cuig = authorizationService.getCurrentUserCuig();
        }
        log.info("Request to delete disease with name {} and cuig {}", name, cuig);
        CemaDisease cemaDisease = diseaseRepository.findCemaDiseaseByNameAndEstablishmentCuigIgnoreCase(name, cuig);
        if (cemaDisease != null) {
            List<CemaIllness> illnesses = illnessRepository.findCemaIllnessByDiseaseName(cemaDisease.getName());
            if (illnesses != null && !illnesses.isEmpty()) {
                log.error("Cannot delete disease while still being referenced by illnesses");
                throw new ValidationException(
                        String.format("The disease %s cannot be delete while is still being referenced from existing illnesses", cemaDisease.getName()));
            }
            log.info("Disease exists, deleting");
            diseaseRepository.delete(cemaDisease);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        log.info("Not found");
        throw new NotFoundException(String.format("Disease %s doesn't exits", name));
    }

    @ApiOperation(value = "Retrieve diseases for your cuig", response = Disease.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Listed all diseases", responseHeaders = {
                    @ResponseHeader(name = "total-elements", response = String.class, description = "Total number of search results"),
                    @ResponseHeader(name = "total-pages", response = String.class, description = "Total number of pages to navigate"),
                    @ResponseHeader(name = "current-page", response = String.class, description = "The page being returned, zero indexed")
            })
    })
    @GetMapping(value = BASE_URL + "list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Disease>> listDiseases(
            @ApiParam(
                    value = "The page you want to retrieve.",
                    example = "1")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @ApiParam(
                    value = "The maximum number of disease entries to return per page.",
                    example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

        String cuig = authorizationService.getCurrentUserCuig();
        Pageable paging = PageRequest.of(page, size);

        Page<CemaDisease> cemaDiseasePage;
        if (authorizationService.isAdmin()) {
            cemaDiseasePage = diseaseRepository.findAll(paging);
        } else {
            cemaDiseasePage = diseaseRepository.findAllByEstablishmentCuig(cuig, paging);
        }

        List<CemaDisease> cemaDiseases = cemaDiseasePage.getContent();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("total-elements", String.valueOf(cemaDiseasePage.getTotalElements()));
        responseHeaders.set("total-pages", String.valueOf(cemaDiseasePage.getTotalPages()));
        responseHeaders.set("current-page", String.valueOf(cemaDiseasePage.getNumber()));

        List<Disease> diseases = cemaDiseases.stream().map(diseaseMapping::mapEntityToDomain).collect(Collectors.toList());

        return ResponseEntity.ok().headers(responseHeaders).body(diseases);
    }
}
