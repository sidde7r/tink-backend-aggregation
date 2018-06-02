package se.tink.backend.connector.abnamro.exceptions;

import se.tink.backend.core.Credentials;

public class AccountNotFoundException extends Exception {
    public AccountNotFoundException(Credentials credentials, String bankId) {
        super(String.format("Account not found (UserId = '%s', CredentialsId = '%s', BankId = '%s').",
                credentials.getUserId(), credentials.getId(), bankId));
    }
}
