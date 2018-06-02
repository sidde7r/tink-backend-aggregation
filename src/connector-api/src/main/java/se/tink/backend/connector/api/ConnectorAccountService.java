package se.tink.backend.connector.api;

import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.rpc.AccountListEntity;

public interface ConnectorAccountService {

    void createAccounts(String externalUserId, AccountListEntity accountListEntity) throws RequestException;

    void deleteAccount(String externalUserId, String externalAccountId) throws RequestException;
}
