package se.tink.backend.common.application.mortgage;

import java.util.Objects;
import java.util.Optional;
import se.tink.backend.common.application.ApplicationNotValidException;
import se.tink.backend.common.application.ApplicationTemplate;
import se.tink.backend.common.application.ApplicationValidator;
import se.tink.backend.common.application.form.ApplicationFormValidator;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.TinkUserAgent;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.utils.ApplicationUtils;
import static se.tink.backend.utils.ApplicationUtils.getFirst;

public class SwitchMortgageProviderApplicationValidator extends ApplicationValidator {
    private final TinkUserAgent userAgent;
    public SwitchMortgageProviderApplicationValidator(final ApplicationTemplate template, final User user,
            final ApplicationFormValidator formValidator, TinkUserAgent userAgent) {
        
        super(template, user, formValidator);

        this.userAgent = userAgent;
    }

    public void validate(Application application) throws ApplicationNotValidException {
        super.validate(application);

        /*
         * Together with 2.5.17, we changed the confirmation form and added the signature form, to sign the power of
         * attorney. After release of this functionality, we'll no longer allow older versions of the app completing
         * the switch mortgage application (since we require the PoA).
         *
         * To make the transition a little bit smoother for the user, we don't display this error until the user
         * commits to the "actual" part of the application (i.e. after submitting the product details form).
         *
         * Since the client doesn't handle the application status in a nice way, we need to set the status on the last
         * submitted _form_.
         */
        if (isUserAgentPre2517()) {
            Optional<ApplicationForm> productDetailsForm = ApplicationUtils
                    .getFirst(application, ApplicationFormName.MORTGAGE_PRODUCT_DETAILS);

            if (productDetailsForm.isPresent() && !Objects
                    .equals(productDetailsForm.get().getStatus().getKey(), ApplicationFormStatusKey.CREATED)) {
                Optional<ApplicationForm> lastSubmittedForm = getLastSubmittedForm(application);

                if (lastSubmittedForm.isPresent()) {
                    lastSubmittedForm.get().updateStatusIfChanged(ApplicationFormStatusKey.ERROR,
                            "För att kunna gå vidare med ansökan behöver du uppdatera appen till den senaste versionen.");
                }
            }
        }
    }

    private static Optional<ApplicationForm> getLastSubmittedForm(Application application) {

        Optional<ApplicationForm> lastSubmittedForm = Optional.empty();

        for (ApplicationForm form : application.getForms()) {
            if (Objects.equals(form.getStatus().getKey(), ApplicationFormStatusKey.CREATED)) {
                break;
            } else {
                lastSubmittedForm = Optional.of(form);
            }
        }

        return lastSubmittedForm;
    }

    private boolean isUserAgentPre2517() {
        // Default to the newest version if nothing specified
        if (userAgent == null) {
            return false;
        }

        boolean is2517OrLater = userAgent.hasValidVersion("2.5.17", null);

        return !is2517OrLater;
    }
}
