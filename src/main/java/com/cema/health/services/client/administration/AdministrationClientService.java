package com.cema.health.services.client.administration;

import com.cema.health.domain.audit.Audit;

public interface AdministrationClientService {

    void validateEstablishment(String cuig);

    void sendAuditRequest(Audit audit);
}
