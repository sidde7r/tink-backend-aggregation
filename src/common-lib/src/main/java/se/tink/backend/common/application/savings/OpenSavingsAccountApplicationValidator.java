package se.tink.backend.common.application.savings;

import se.tink.backend.common.application.ApplicationTemplate;
import se.tink.backend.common.application.ApplicationValidator;
import se.tink.backend.common.application.form.ApplicationFormValidator;
import se.tink.backend.core.User;

public class OpenSavingsAccountApplicationValidator extends ApplicationValidator {
    public OpenSavingsAccountApplicationValidator(final ApplicationTemplate template, final User user,
            final ApplicationFormValidator formValidator) {
        
        super(template, user, formValidator);
    }
}
