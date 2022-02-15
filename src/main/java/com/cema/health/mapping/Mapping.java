package com.cema.health.mapping;

public interface Mapping<ENTITY, DOMAIN> {

    DOMAIN mapEntityToDomain(ENTITY entity);

    ENTITY mapDomainToEntity(DOMAIN domain);

    ENTITY updateDomainWithEntity(DOMAIN domain, ENTITY entity);
}
