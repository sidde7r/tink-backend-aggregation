package se.tink.backend.aggregation.nxgen.controllers.authentication.webscraping.loginvalidation;

import se.tink.backend.aggregation.nxgen.controllers.authentication.step.CallbackProcessorEmpty;

public interface LoginValidationCallbackProcessor<INPUT> extends CallbackProcessorEmpty {
    INPUT getValidationInput();
}
