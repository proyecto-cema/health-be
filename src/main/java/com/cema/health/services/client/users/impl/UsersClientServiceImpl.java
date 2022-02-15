package com.cema.health.services.client.users.impl;

import com.cema.health.domain.ErrorResponse;
import com.cema.health.exceptions.ValidationException;
import com.cema.health.services.authorization.AuthorizationService;
import com.cema.health.services.client.users.UsersClientService;
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
public class UsersClientServiceImpl implements UsersClientService {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String PATH_VALIDATE_USER = "users/validate/{username}";

    private final RestTemplate restTemplate;
    private final String url;
    private final AuthorizationService authorizationService;
    private final ObjectMapper mapper = new ObjectMapper();

    public UsersClientServiceImpl(RestTemplate restTemplate, @Value("${back-end.users.url}") String url,
                                  AuthorizationService authorizationService) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.authorizationService = authorizationService;
    }

    @SneakyThrows
    @Override
    public void validateUser(String userName) {
        String authToken = authorizationService.getUserAuthToken();
        String searchUrl = url + PATH_VALIDATE_USER;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AUTHORIZATION_HEADER, authToken);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity("{}", httpHeaders);
        try {
            restTemplate.exchange(searchUrl, HttpMethod.GET, entity, Object.class, userName);
        } catch (RestClientResponseException httpClientErrorException) {
            String response = httpClientErrorException.getResponseBodyAsString();
            ErrorResponse errorResponse = mapper.readValue(response, ErrorResponse.class);
            throw new ValidationException(errorResponse.getMessage(), httpClientErrorException);
        }
    }
}
