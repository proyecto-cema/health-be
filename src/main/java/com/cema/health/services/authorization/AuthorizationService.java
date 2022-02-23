package com.cema.health.services.authorization;

public interface AuthorizationService {

    String getCurrentUserName();

    String getUserAuthToken();

    String getCurrentUserCuig();

    boolean isOnTheSameEstablishment(String cuig);

    boolean isAdmin();
}
