package se.tink.backend.common.application.valuation;

import se.tink.backend.common.application.ApplicationTemplate;
import se.tink.backend.common.application.ApplicationValidator;
import se.tink.backend.common.application.form.ApplicationFormValidator;
import se.tink.backend.core.User;

public class ResidenceValuationApplicationValidator extends ApplicationValidator {
    public ResidenceValuationApplicationValidator(ApplicationTemplate template,
            User user,
            ApplicationFormValidator formValidator) {
        super(template, user, formValidator);
    }
}
