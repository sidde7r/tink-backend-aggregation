package se.tink.backend.common.application;

import java.util.Objects;
import se.tink.backend.common.application.form.ApplicationFormValidator;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.User;

/**
 * For user recoverable errors (e.g. a field value does not validate) the status for the applicable ApplicationForm will
 * be updated. For non-user recoverable errors (e.g. the wrong form has been attached to the Application) an
 * ApplicationNotValidException will be thrown
 */
public class ApplicationValidator {
    private final ApplicationFormValidator formValidator;
    private final ApplicationTemplate template;
    protected final Catalog catalog;

    public ApplicationValidator(final ApplicationTemplate template, final User user,
            final ApplicationFormValidator formValidator) {
        
        this.template = template;
        this.formValidator = formValidator;
        this.catalog = Catalog.getCatalog(user.getProfile().getLocale());
    }

    public void validate(Application application) throws ApplicationNotValidException {

        if (!Objects.equals(template.getType(), application.getType())) {
            throw new ApplicationNotValidException(String.format("ApplicationType \"%s\" is not supported.",
                    application.getType()));
        }

        for (ApplicationForm form : application.getForms()) {
            formValidator.validate(form, application);
        }
    }
}
