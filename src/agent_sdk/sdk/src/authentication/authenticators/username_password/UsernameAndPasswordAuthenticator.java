package se.tink.agent.sdk.authentication.authenticators.username_password;

import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnection;

public interface UsernameAndPasswordAuthenticator
        extends UsernameAndPasswordLogin, VerifyBankConnection {}
