package com.cema.health.services.client.bovine.impl;

import com.cema.health.domain.ErrorResponse;
import com.cema.health.exceptions.ValidationException;
import com.cema.health.services.authorization.AuthorizationService;
import com.cema.health.services.client.bovine.BovineClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
public class BovineClientServiceImpl implements BovineClientService {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String PATH_VALIDATE_BOVINE = "bovines/validate/{tag}?cuig={cuig}";
    private static final String PATH_VALIDATE_BATCH = "batches/validate/{batchName}?cuig={cuig}";

    private final RestTemplate restTemplate;
    private final String url;
    private final AuthorizationService authorizationService;
    private final ObjectMapper mapper = new ObjectMapper();

    public BovineClientServiceImpl(RestTemplate restTemplate, @Value("${back-end.bovine.url}") String url,
                                   AuthorizationService authorizationService) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.authorizationService = authorizationService;
    }

    @SneakyThrows
    @Override
    public void validateBovine(String tag, String cuig) {
        String authToken = authorizationService.getUserAuthToken();
        String searchUrl = url + PATH_VALIDATE_BOVINE;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AUTHORIZATION_HEADER, authToken);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity("{}", httpHeaders);
        try {
            restTemplate.exchange(searchUrl, HttpMethod.GET, entity, Object.class, tag, cuig);
        } catch (RestClientResponseException httpClientErrorException) {
            String response = httpClientErrorException.getResponseBodyAsString();
            ErrorResponse errorResponse = mapper.readValue(response, ErrorResponse.class);
            throw new ValidationException(errorResponse.getMessage(), httpClientErrorException);
        }
    }

    @SneakyThrows
    @Override
    public void validateBatch(String batchName, String cuig) {
        String authToken = authorizationService.getUserAuthToken();
        String searchUrl = url + PATH_VALIDATE_BATCH;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AUTHORIZATION_HEADER, authToken);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity("{}", httpHeaders);
        try {
            restTemplate.exchange(searchUrl, HttpMethod.GET, entity, Object.class, batchName, cuig);
        } catch (RestClientResponseException httpClientErrorException) {
            String response = httpClientErrorException.getResponseBodyAsString();
            ErrorResponse errorResponse = mapper.readValue(response, ErrorResponse.class);
            throw new ValidationException(errorResponse.getMessage(), httpClientErrorException);
        }
    }

}
