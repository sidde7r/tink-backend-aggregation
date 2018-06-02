package se.tink.backend.core.oauth2;

import com.google.common.collect.ImmutableSet;

public class OAuth2WebHookEvent {

    public static final String ACTIVITY_UPDATE = "activity:update";
    public static final String CREDENTIALS_CREATE = "credentials:create";
    public static final String CREDENTIALS_UPDATE = "credentials:update";
    public static final String SIGNABLE_OPERATION_UPDATE = "signable-operation:update";
    public static final String TRANSACTION_CREATE = "transaction:create";
    public static final String TRANSACTION_UPDATE = "transaction:update";

    public static final ImmutableSet<String> ALL = ImmutableSet.of(ACTIVITY_UPDATE, CREDENTIALS_CREATE,
            CREDENTIALS_UPDATE, SIGNABLE_OPERATION_UPDATE, TRANSACTION_CREATE, TRANSACTION_UPDATE);

    public static final String DOCUMENTED = "activity:update, credentials:create, credentials:update, signable-operation:update, transaction:create, transaction:update";
}
