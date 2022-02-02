package com.cema.health.services.client.administration;

import com.cema.health.domain.audit.Audit;

public interface AdministrationClientService {

    void sendAuditRequest(Audit audit);
}
