package com.cema.health.services.database;

import com.cema.health.domain.Note;
import com.cema.health.entities.CemaIllness;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DatabaseService {

    CemaIllness saveCemaIllness(CemaIllness cemaIllness, String diseaseName, List<String> notes);

    CemaIllness addNotesToIllness(CemaIllness cemaIllness, List<Note> notes);

    Page<CemaIllness> searchIllnesses(CemaIllness illness, int page, int size);
}
