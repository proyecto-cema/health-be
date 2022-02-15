package com.cema.health.config.advices;

import com.cema.health.domain.CemaUserDetails;
import com.cema.health.domain.audit.Audit;
import com.cema.health.services.client.administration.AdministrationClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@ControllerAdvice
@Slf4j
public class AuditResponseBodyAdviceAdapter implements ResponseBodyAdvice<Object> {

    private final AdministrationClientService administrationClientService;
    private static final String MODULE = "health";
    private ObjectMapper mapper = new ObjectMapper();

    public AuditResponseBodyAdviceAdapter(AdministrationClientService administrationClientService) {
        this.administrationClientService = administrationClientService;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof CemaUserDetails)) {
            return body;
        }
        if (request instanceof ServletServerHttpRequest &&
                response instanceof ServletServerHttpResponse) {
            Audit auditRequest = new Audit();

            try {
                Field field = returnType.getClass().getDeclaredField("returnValue");
                field.setAccessible(true);
                ResponseEntity value = (ResponseEntity) field.get(returnType);
                auditRequest.setResponseStatus(String.valueOf(value.getStatusCode()));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("Unable to recover response status.");
            }

            ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
            String input = "Unable to recover.";
            if (serverHttpRequest.getServletRequest() instanceof ContentCachingRequestWrapper) {
                ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper) serverHttpRequest.getServletRequest();
                try {
                    input = ByteSource.wrap(requestWrapper.getContentAsByteArray())
                            .asCharSource(StandardCharsets.UTF_8).read();
                    input = input.replaceAll("\\s*[\\r\\n]+\\s*", "").trim();
                } catch (IOException e) {
                    log.error("Unable to recover request body.");
                }
            } else {
                log.info("Cannot recover request body, request of wrong class {}", serverHttpRequest.getServletRequest().getClass());
            }

            auditRequest.setRequestBody(input);
            try {
                auditRequest.setResponseBody(mapper.writeValueAsString(body));
            } catch (JsonProcessingException e) {
                log.error("Unable to parse response body.");
            }
            auditRequest.setLocalAddress(String.valueOf(request.getLocalAddress()));
            auditRequest.setRequestHeaders(String.valueOf(request.getHeaders()));
            auditRequest.setUri(String.valueOf(request.getURI()));
            auditRequest.setHttpMethod(request.getMethodValue());
            auditRequest.setMethod(String.valueOf(returnType.getMethod()));
            auditRequest.setRole(String.valueOf(authentication.getAuthorities()));
            auditRequest.setAuditDate(new Date());
            auditRequest.setModule(MODULE);

            CemaUserDetails cemaUserDetails = (CemaUserDetails) authentication.getPrincipal();
            auditRequest.setUsername(cemaUserDetails.getUsername());
            auditRequest.setEstablishmentCuig(cemaUserDetails.getCuig());

            administrationClientService.sendAuditRequest(auditRequest);
        }
        return body;
    }
}
