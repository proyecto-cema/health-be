package com.cema.health.services.validation;

import com.cema.health.domain.Illness;
import com.cema.health.entities.CemaIllness;

import java.util.Date;

public interface HealthValidationService {

    void validateIllness(Illness illness);

    void validateIllnessDate(CemaIllness cemaIllness, Illness illness);
}
