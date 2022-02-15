package com.cema.health.repositories;

import com.cema.health.entities.CemaNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<CemaNote, Long> {
}
