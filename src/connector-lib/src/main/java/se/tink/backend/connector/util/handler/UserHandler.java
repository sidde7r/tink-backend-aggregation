package se.tink.backend.connector.util.handler;

import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.rpc.UserEntity;
import se.tink.backend.core.Market;
import se.tink.backend.core.User;

public interface UserHandler {
    
    User findUser(String externalUserId) throws RequestException;

    User mapToTinkModel(UserEntity userEntity, Market market) throws RequestException;

    void storeUser(User user);

    void deleteUser(User user);

    void updateUser(User user, UserEntity entity);
}
