package se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators;

import java.util.List;

public interface LoginValidatorFactory {
    List<LoginValidator> getValidators();
}
