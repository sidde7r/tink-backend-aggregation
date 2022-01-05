package se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation;

@FunctionalInterface
public interface LoginResponseProvider<INPUT> {
    INPUT getValidationInput();
}
