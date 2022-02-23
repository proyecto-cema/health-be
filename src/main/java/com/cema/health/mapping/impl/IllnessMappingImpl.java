package com.cema.health.mapping.impl;

import com.cema.health.domain.Illness;
import com.cema.health.domain.Note;
import com.cema.health.entities.CemaIllness;
import com.cema.health.entities.CemaNote;
import com.cema.health.mapping.Mapping;
import com.cema.health.services.authorization.AuthorizationService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IllnessMappingImpl implements Mapping<CemaIllness, Illness> {

    private final AuthorizationService authorizationService;

    public IllnessMappingImpl(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    private Note mapCemaNote(CemaNote cemaNote){
        return Note.builder().content(cemaNote.getContent()).creationDate(cemaNote.getCreationDate()).build();
    }

    @Override
    public Illness mapEntityToDomain(CemaIllness cemaIllness) {
        List<CemaNote> cemaNotes = cemaIllness.getCemaNotes();

        return Illness.builder()
                .id(cemaIllness.getId())
                .bovineTag(cemaIllness.getBovineTag())
                .diseaseName(cemaIllness.getDisease().getName())
                .endingDate(cemaIllness.getEndingDate())
                .startingDate(cemaIllness.getStartingDate())
                .establishmentCuig(cemaIllness.getEstablishmentCuig())
                .notes(cemaIllness.getCemaNotes().stream()
                        .sorted(Comparator.comparing(CemaNote::getCreationDate))
                        .map(this::mapCemaNote)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public CemaIllness mapDomainToEntity(Illness illness) {
        String workerUsername = authorizationService.getCurrentUserName();
        return CemaIllness.builder()
                .bovineTag(illness.getBovineTag())
                .endingDate(illness.getEndingDate())
                .startingDate(illness.getStartingDate())
                .establishmentCuig(illness.getEstablishmentCuig())
                .workerUsername(workerUsername)
                .build();
    }

    @Override
    public CemaIllness updateDomainWithEntity(Illness illness, CemaIllness cemaIllness) {
        Date endingDate = illness.getEndingDate() != null ? illness.getEndingDate() : cemaIllness.getEndingDate();
        cemaIllness.setEndingDate(endingDate);
        return cemaIllness;
    }
}
