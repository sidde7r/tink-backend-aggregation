package se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators;

public interface LoginValidator<INPUT> {
    void validate(INPUT input);
}
