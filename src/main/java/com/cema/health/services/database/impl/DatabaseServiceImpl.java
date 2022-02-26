package com.cema.health.services.database.impl;

import com.cema.health.domain.Note;
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
    public CemaIllness saveCemaIllness(CemaIllness cemaIllness, String diseaseName, List<String> stringNotes) {
        CemaDisease cemaDisease = diseaseRepository.findCemaDiseaseByNameAndEstablishmentCuigIgnoreCase(diseaseName, cemaIllness.getEstablishmentCuig());
        if (cemaDisease == null) {
            throw new NotFoundException(String.format("Disease with name %s doesn't exists for cuig %s", diseaseName, cemaIllness.getEstablishmentCuig()));
        }
        cemaIllness.setDisease(cemaDisease);

        cemaIllness = illnessRepository.save(cemaIllness);

        List<Note> notes = stringNotes.stream().map(Note::new).collect(Collectors.toList());

        return addNotesToIllness(cemaIllness, notes);
    }

    @Override
    public CemaIllness addNotesToIllness(CemaIllness cemaIllness, List<Note> notes) {
        List<CemaNote> newCemaNotes = new ArrayList<>();
        for (Note note : notes.stream().filter(note -> note.getCreationDate() == null).collect(Collectors.toList())) {
            CemaNote cemaNote = CemaNote.builder()
                    .content(note.getContent())
                    .illness(cemaIllness)
                    .creationDate(new Date())
                    .build();
            newCemaNotes.add(cemaNote);
        }
        noteRepository.saveAll(newCemaNotes);
        cemaIllness.getCemaNotes().addAll(newCemaNotes);
        return cemaIllness;
    }

    @Override
    public Page<CemaIllness> searchIllnesses(CemaIllness illness, int page, int size) {
        ExampleMatcher caseInsensitiveExampleMatcher = ExampleMatcher
                .matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Pageable paging = PageRequest.of(page, size, Sort.by("startingDate").descending());
        return illnessRepository.findAll(Example.of(illness, caseInsensitiveExampleMatcher), paging);
    }
}
