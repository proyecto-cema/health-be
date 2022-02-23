package com.cema.health.services.authorization.impl;

import com.cema.health.constants.Roles;
import com.cema.health.domain.CemaUserDetails;
import com.cema.health.services.authorization.AuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    @Override
    public String getCurrentUserName(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CemaUserDetails cemaUserDetails = (CemaUserDetails) authentication.getPrincipal();
        return cemaUserDetails.getUsername();
    }

    @Override
    public String getUserAuthToken() {
        CemaUserDetails cemaUserDetails = (CemaUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return cemaUserDetails.getAuthToken();
    }

    @Override
    public String getCurrentUserCuig() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CemaUserDetails cemaUserDetails = (CemaUserDetails) authentication.getPrincipal();
        return cemaUserDetails.getCuig();
    }

    @Override
    public boolean isOnTheSameEstablishment(String cuig) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CemaUserDetails cemaUserDetails = (CemaUserDetails) authentication.getPrincipal();
        String authenticationCuig = cemaUserDetails.getCuig();
        return authenticationCuig.equals(cuig) || isAdmin();
    }

    @Override
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(Roles.ADMIN));
    }
}
