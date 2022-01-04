package se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation;

public interface LoginResponseProvider<INPUT> {
    INPUT getValidationInput();
}
