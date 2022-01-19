package com.cema.health.services.validation.impl;

import com.cema.health.domain.Illness;
import com.cema.health.entities.CemaIllness;
import com.cema.health.exceptions.ValidationException;
import com.cema.health.services.validation.HealthValidationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class HealthValidationServiceImpl implements HealthValidationService {

    @Override
    public void validateIllness(Illness illness) {
        LocalDateTime startingTime = illness.getStartingDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDateTime endingTime = illness.getEndingDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        if (startingTime.isAfter(endingTime)) {
            throw new ValidationException(
                    String.format("Starting date %s is after ending date %s",
                            startingTime, endingTime));
        }
    }


    @Override
    public void validateIllnessDate(CemaIllness cemaIllness, Illness illness) {

        LocalDateTime existingStartingTime = cemaIllness.getStartingDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDateTime existingEndingTime = cemaIllness.getEndingDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        LocalDateTime newStartingTime = illness.getStartingDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        LocalDateTime newEndingTime = illness.getEndingDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        String diseaseName = illness.getDiseaseName();
        String bovineTag = illness.getBovineTag();
        String startingTimeStr = existingStartingTime.toString();
        String endingTimeStr = existingEndingTime.toString();

        if (isBetweenDates(newStartingTime, existingStartingTime, existingEndingTime)) {
            String incorrectTime = newStartingTime.toString();
            throw new ValidationException(
                    String.format("The bovine %s is already sick with %s from %s to %s. Your starting time %s overlaps",
                            bovineTag, diseaseName, startingTimeStr, endingTimeStr, incorrectTime));
        }

        if (isBetweenDates(newEndingTime, existingStartingTime, existingEndingTime)) {
            String incorrectTime = newEndingTime.toString();
            throw new ValidationException(
                    String.format("The bovine %s is already sick with %s from %s to %s. Your ending time %s overlaps",
                            bovineTag, diseaseName, startingTimeStr, endingTimeStr, incorrectTime));
        }
    }

    private boolean isBetweenDates(LocalDateTime toValidate, LocalDateTime start, LocalDateTime end){
        return start.isBefore(toValidate) && end.isAfter(toValidate);
    }
}
