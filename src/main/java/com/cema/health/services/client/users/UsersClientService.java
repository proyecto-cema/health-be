package com.cema.health.services.client.users;

import com.cema.health.domain.User;
import lombok.SneakyThrows;

public interface UsersClientService {

    void validateUser(String userName);

    @SneakyThrows
    User getUser(String userName);
}
