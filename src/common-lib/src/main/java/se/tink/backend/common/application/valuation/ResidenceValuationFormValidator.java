package se.tink.backend.common.application.valuation;

import se.tink.backend.common.application.ApplicationTemplate;
import se.tink.backend.common.application.field.ApplicationFieldValidator;
import se.tink.backend.common.application.form.ApplicationFormValidator;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.User;

public class ResidenceValuationFormValidator extends ApplicationFormValidator {
    public ResidenceValuationFormValidator(ApplicationTemplate template,
            User user,
            ApplicationFieldValidator fieldValidator) {
        super(template, user, fieldValidator);
    }

    @Override
    public void validate(ApplicationForm form, Application application) {
        super.validate(form, application);

        if (form.hasError()) {
            return;
        }

        // TODO: Any validation?
    }
}
