package com.cema.health.services.database.impl;

import com.cema.health.entities.CemaDisease;
import com.cema.health.entities.CemaIllness;
import com.cema.health.entities.CemaNote;
import com.cema.health.exceptions.NotFoundException;
import com.cema.health.repositories.DiseaseRepository;
import com.cema.health.repositories.IllnessRepository;
import com.cema.health.repositories.NoteRepository;
import com.cema.health.services.database.DatabaseService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DatabaseServiceImpl implements DatabaseService {

    private final DiseaseRepository diseaseRepository;
    private final NoteRepository noteRepository;
    private final IllnessRepository illnessRepository;

    public DatabaseServiceImpl(DiseaseRepository diseaseRepository, NoteRepository noteRepository, IllnessRepository illnessRepository) {
        this.diseaseRepository = diseaseRepository;
        this.noteRepository = noteRepository;
        this.illnessRepository = illnessRepository;
    }

    @Override
    public CemaIllness saveCemaIllness(CemaIllness cemaIllness, String diseaseName, List<String> notes) {
        CemaDisease cemaDisease = diseaseRepository.findCemaDiseaseByNameAndEstablishmentCuigIgnoreCase(diseaseName, cemaIllness.getEstablishmentCuig());
        if (cemaDisease == null) {
            throw new NotFoundException(String.format("Disease with name %s doesn't exists for cuig %s", diseaseName, cemaIllness.getEstablishmentCuig()));
        }
        cemaIllness.setDisease(cemaDisease);

        cemaIllness = illnessRepository.save(cemaIllness);

        return addNotesToIllness(cemaIllness, notes);
    }

    @Override
    public CemaIllness addNotesToIllness(CemaIllness cemaIllness, List<String> notes) {
        List<CemaNote> cemaNotes = new ArrayList<>();
        for (String note : notes) {
            CemaNote cemaNote = CemaNote.builder()
                    .content(note)
                    .illness(cemaIllness)
                    .creationDate(new Date())
                    .build();
            cemaNotes.add(cemaNote);
        }
        noteRepository.saveAll(cemaNotes);
        cemaIllness.getCemaNotes().addAll(cemaNotes);
        return cemaIllness;
    }

    @Override
    public Page<CemaIllness> searchIllnesses(CemaIllness illness, int page, int size) {
        ExampleMatcher caseInsensitiveExampleMatcher = ExampleMatcher
                .matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Pageable paging = PageRequest.of(page, size, Sort.by("startingDate"));
        return illnessRepository.findAll(Example.of(illness, caseInsensitiveExampleMatcher), paging);
    }
}
