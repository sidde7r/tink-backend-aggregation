package se.tink.backend.common.application.savings;

import java.util.Optional;
import se.tink.backend.common.application.ApplicationTemplate;
import se.tink.backend.common.application.field.ApplicationFieldValidator;
import se.tink.backend.common.application.form.ApplicationFormValidator;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.utils.ApplicationUtils;

public class OpenSavingsAccountFormValidator extends ApplicationFormValidator {

    public OpenSavingsAccountFormValidator(final ApplicationTemplate template,
            User user, final ApplicationFieldValidator fieldValidator) {
        super(template, user, fieldValidator);
    }
    
    @Override
    public void validate(ApplicationForm form, Application application) {
        super.validate(form, application);
        
        if (!form.hasError()) {
            switch (form.getName()) {
            case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PEP: {
                if (ApplicationUtils.isYes(form, ApplicationFieldName.IS_PEP)) {
                    String message = catalog.getString("For people in a politically exposed position (PEP), extra information is required which Tink currently doesn't support.");
                    
                    if (application.getProductArticle().isPresent()) {
                        switch (application.getProductArticle().get().getProviderName()) {
                        case "collector-bankid": {
                            message += "\n\n" + catalog.getString("Please contact Collector Bank directly to open an account. Visit them at collector.se or call 010-161 00 00.");
                            break;
                        }
                        case "sbab-bankid": {
                            message += "\n\n" + "Vänligen kontakta SBAB direkt istället, för att öppna ett konto. Du kan besöka dem på sbab.se.";
                            break;
                        }
                        default:
                            // Do nothing.
                        }
                    }
                    
                    form.updateStatusIfChanged(ApplicationFormStatusKey.DISQUALIFIED, message);
                }
                break;
            }
            case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_OTHER_COUNTRY: {
                Optional<ProductArticle> productArticle = application.getProductArticle();
                if (productArticle.isPresent()) {
                    switch (productArticle.get().getProviderName()) {
                    case "sbab-bankid":
                        boolean hasSwedishCitizenship = ApplicationUtils.isFirstYes(application,
                                ApplicationFormName.OPEN_SAVINGS_ACCOUNT_SWEDISH_CITIZEN,
                                ApplicationFieldName.SWEDISH_CITIZEN);
                        boolean hasOtherCitizenship = ApplicationUtils.isFirstYes(application,
                                ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_OTHER_COUNTRY,
                                ApplicationFieldName.CITIZENSHIP_IN_OTHER_COUNTRY);

                        // We need at least one taxable country in SBAB
                        if (!hasSwedishCitizenship && !hasOtherCitizenship) {
                            form.updateStatusIfChanged(
                                    ApplicationFormStatusKey.DISQUALIFIED,
                                    "Du har inte angett något medborgarskap. För att kunna öppna ett sparkonto måste du ha åtminstone ett medborgarskap.");
                        }
                        break;
                    }
                }
                break;
            }
            case ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_USA: {
                // Collector doesn't support savings account customers that are taxable in USA.
                if (ApplicationUtils.isYes(form, ApplicationFieldName.TAXABLE_IN_USA)) {
                    form.updateStatusIfChanged(
                            ApplicationFormStatusKey.DISQUALIFIED,
                            catalog.getString("Collector Bank doesn't allow people that are taxable in the USA to open savings accounts (due to reporting requirements)."));
                }
                break;
            }
            default:
                // Do nothing.
            }
        }
    }
}
