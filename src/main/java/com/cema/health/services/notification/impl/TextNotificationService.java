package com.cema.health.services.notification.impl;

import com.cema.health.domain.User;
import com.cema.health.entities.CemaIllness;
import com.cema.health.repositories.IllnessRepository;
import com.cema.health.services.client.users.UsersClientService;
import com.cema.health.services.notification.NotificationService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TextNotificationService implements NotificationService {

    private final String accountSid;
    private final String authToken;
    private final String sender;
    private final String countryCode;
    private final IllnessRepository illnessRepository;
    private final UsersClientService usersClientService;

    public TextNotificationService(@Value("${notification.text.sid}") String accountSid,
                                   @Value("${notification.text.token}") String authToken,
                                   @Value("${notification.text.sender}") String sender,
                                   @Value("${notification.text.country-code}") String countryCode,
                                   IllnessRepository illnessRepository, UsersClientService usersClientService) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.sender = sender;
        this.countryCode = countryCode;
        this.illnessRepository = illnessRepository;
        this.usersClientService = usersClientService;
    }

    //@Scheduled(cron = "0 15 10 * * ?", zone = "America/Buenos_Aires")
    @Override
    public void notifyAllUsers() {
        List<CemaIllness> illnessList = illnessRepository.findAllIllnessEndingToday()
                .stream().filter(illness -> StringUtils.hasText(illness.getWorkerUsername())).collect(Collectors.toList());
        log.info("Found {} illnesses ending today", illnessList.size());
        for (CemaIllness cemaIllness : illnessList) {
            try {
                User toNotify = usersClientService.getUser(cemaIllness.getWorkerUsername());
                String phone = toNotify.getPhone();
                String name = toNotify.getName();
                String diseaseName = cemaIllness.getDisease().getName();
                String bovineTag = cemaIllness.getBovineTag();
                log.info("Notifying user {} with phone {} for disease {} of bovine {}", name, phone, diseaseName, bovineTag);

                String body = String.format("El bovino %s enfermo de %s debe ser revisado el dia de hoy", bovineTag, diseaseName);
                log.info("Sending message: {}", body);
                sendNotification(body, phone);
            } catch (Exception e) {
                log.error("unable to send notification to user {} due to error", cemaIllness.getWorkerUsername(), e);
            }
        }
    }

    @Override
    public void sendNotification(String body, String destination) {
        Twilio.init(accountSid, authToken);
        if (!destination.startsWith(countryCode)) {
            destination = countryCode + destination;
        }

        Message message = Message.creator(new PhoneNumber(destination),
                new PhoneNumber(sender),
                body).create();

        log.info("Sending message {}", message.getSid());
    }
}
