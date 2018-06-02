package se.tink.backend.common.application;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.Date;
import se.tink.backend.common.application.mortgage.SwitchMortgageProviderTemplate;
import se.tink.backend.common.application.savings.OpenSavingsAccountTemplate;
import se.tink.backend.common.application.valuation.ResidenceValuationTemplate;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.ApplicationFormStatus;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.core.enums.ApplicationFormType;
import se.tink.libraries.application.ApplicationType;

public abstract class ApplicationTemplate {

    public abstract ApplicationType getType();

    public abstract ImmutableSet<String> getFormNames();

    public abstract ImmutableSet<String> getFieldNames(String formName);

    public ApplicationFormType getFormType(String formName) {
        return ApplicationFormType.FORM;
    }
    
    public ApplicationForm createEmptyForm(Application application, String formName) {
        ApplicationFormStatus status = new ApplicationFormStatus();
        status.setUpdated(new Date());
        status.setKey(ApplicationFormStatusKey.CREATED);

        ApplicationForm form = new ApplicationForm();
        form.setName(formName);
        form.setType(getFormType(formName));
        form.setStatus(status);
        form.setApplicationId(application.getId());
        form.setUserId(application.getUserId());

        return form;
    }

    public abstract Comparator<ApplicationForm> getFormComparator();

    public static ApplicationTemplate create(Application application) {
        switch(application.getType()) {
        case SWITCH_MORTGAGE_PROVIDER:
            return new SwitchMortgageProviderTemplate();
        case OPEN_SAVINGS_ACCOUNT:
            return new OpenSavingsAccountTemplate();
        case RESIDENCE_VALUATION:
            return new ResidenceValuationTemplate();
        default:
            throw new IllegalArgumentException("Type not implemented");
        }
    }
}
