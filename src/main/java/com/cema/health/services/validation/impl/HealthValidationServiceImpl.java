package com.cema.health.services.validation.impl;

import com.cema.health.domain.Illness;
import com.cema.health.entities.CemaIllness;
import com.cema.health.exceptions.ValidationException;
import com.cema.health.services.validation.HealthValidationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class HealthValidationServiceImpl implements HealthValidationService {

    @Override
    public void validateIllness(Illness illness) {
        LocalDateTime startingTime = illness.getStartingDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        if (startingTime.isAfter(now)) {
            throw new ValidationException(
                    String.format("You cannot register illness for the future. Starting date %s is after current date %s",
                            startingTime, now));
        }

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

        LocalDate existingStartingTime = cemaIllness.getStartingDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate existingEndingTime = cemaIllness.getEndingDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate newStartingTime = illness.getStartingDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate newEndingTime = illness.getEndingDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        String diseaseName = illness.getDiseaseName();
        String bovineTag = illness.getBovineTag();

        boolean overlaps;

        overlaps = isBetweenDates(newStartingTime, existingStartingTime, existingEndingTime)
                || isBetweenDates(newEndingTime, existingStartingTime, existingEndingTime)
                || isBetweenDates(existingStartingTime, newStartingTime, newEndingTime)
                || isBetweenDates(existingEndingTime, newStartingTime, newEndingTime);


        if (overlaps) {
            throw new ValidationException(
                    String.format("The bovine %s is already sick with %s from %s to %s. The new period from %s to %s overlaps.",
                            bovineTag, diseaseName, existingStartingTime, existingEndingTime, newStartingTime, newEndingTime));
        }
    }

    private boolean isBetweenDates(LocalDate toValidate, LocalDate start, LocalDate end) {
        return (start.isBefore(toValidate) || start.equals(toValidate)) && (end.isAfter(toValidate) || end.equals(toValidate));
    }
}
