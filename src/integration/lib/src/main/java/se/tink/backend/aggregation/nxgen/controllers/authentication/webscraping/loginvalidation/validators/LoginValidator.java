package se.tink.backend.aggregation.nxgen.controllers.authentication.webscraping.loginvalidation.validators;

public interface LoginValidator<INPUT> {
    void validate(INPUT input);
}
