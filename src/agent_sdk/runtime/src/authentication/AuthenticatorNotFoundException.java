package se.tink.agent.runtime.authentication;

public class AuthenticatorNotFoundException extends RuntimeException {

    public AuthenticatorNotFoundException() {
        super(
                "Authenticator was not found. Have you implemented a new Authenticator without defining it within the SDK?");
    }
}
