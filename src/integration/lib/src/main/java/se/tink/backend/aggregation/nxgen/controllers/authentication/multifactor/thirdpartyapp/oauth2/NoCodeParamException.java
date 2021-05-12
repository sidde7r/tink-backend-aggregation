package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

public class NoCodeParamException extends RuntimeException {

    public NoCodeParamException(String message) {
        super(message);
    }
}
