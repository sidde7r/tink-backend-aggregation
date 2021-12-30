package se.tink.backend.aggregation.nxgen.controllers.authentication.webscraping.loginvalidation;

public interface LoginResponseProvider<INPUT> {
    INPUT getValidationInput();
}
