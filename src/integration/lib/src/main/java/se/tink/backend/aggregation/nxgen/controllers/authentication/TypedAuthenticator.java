package se.tink.backend.aggregation.nxgen.controllers.authentication;

/**
 * Any authenticator that is associated with exactly one credentials type, e.g. a multi-factor
 * authenticator.
 */
public interface TypedAuthenticator extends Authenticator, CredentialsTyped {}
