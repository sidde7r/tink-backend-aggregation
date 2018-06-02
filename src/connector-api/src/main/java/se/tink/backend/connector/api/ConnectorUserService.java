package se.tink.backend.connector.api;

import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.rpc.UserEntity;

public interface ConnectorUserService {

    void createUser(UserEntity entity) throws RequestException;

    void updateUser(String externalUserId, UserEntity entity) throws RequestException;

    void deleteUser(String externalUserId) throws RequestException;
}
