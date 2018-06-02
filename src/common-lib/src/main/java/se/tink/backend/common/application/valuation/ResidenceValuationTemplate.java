package se.tink.backend.common.application.valuation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.Date;
import se.tink.backend.common.application.ApplicationTemplate;
import se.tink.backend.common.application.form.ApplicationFormTemplate;
import se.tink.backend.common.application.form.ApplicationFormTemplateMap;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.ApplicationFormStatus;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.core.enums.ApplicationFormType;
import se.tink.libraries.application.ApplicationType;

public class ResidenceValuationTemplate extends ApplicationTemplate {

    private static final ImmutableMap<String, ApplicationFormTemplate> FORM_TEMPLATES_BY_NAME = ApplicationFormTemplateMap
            .builder()

            .put(ApplicationFormName.VALUATION_RESIDENCE_TYPE,
                    ApplicationFormType.RESIDENCE_VALUATION_RESIDENCE_TYPE,
                    ApplicationFieldName.VALUATION_RESIDENCE_TYPE)

            .put(ApplicationFormName.VALUATION_APARTMENT_PARAMETERS,
                    ApplicationFormType.RESIDENCE_VALUATION_RESIDENCE_PARAMETERS,
                    ApplicationFieldName.LIVING_AREA,
                    ApplicationFieldName.NUMBER_OF_ROOMS,
                    ApplicationFieldName.VALUATION_MONTHLY_HOUSING_COMMUNITY_FEE)

            .put(ApplicationFormName.VALUATION_HOUSE_PARAMETERS,
                    ApplicationFormType.RESIDENCE_VALUATION_RESIDENCE_PARAMETERS,
                    ApplicationFieldName.LIVING_AREA,
                    ApplicationFieldName.NUMBER_OF_ROOMS,
                    ApplicationFieldName.CONSTRUCTION_YEAR,
                    ApplicationFieldName.PLOT_AREA,
                    ApplicationFieldName.ADDITIONAL_AREA,
                    ApplicationFieldName.VALUATION_MONTHLY_OPERATING_COST)

            .put(ApplicationFormName.VALUATION_LOADING,
                    ApplicationFormType.RESIDENCE_VALUATION_LOADING)

            .build();

    @Override
    public ApplicationType getType() {
        return ApplicationType.RESIDENCE_VALUATION;
    }

    @Override
    public ImmutableSet<String> getFormNames() {
        return FORM_TEMPLATES_BY_NAME.keySet();
    }

    @Override
    public ImmutableSet<String> getFieldNames(String formName) {
        return FORM_TEMPLATES_BY_NAME.get(formName).fieldNames;
    }

    @Override
    public ApplicationFormType getFormType(String formName) {
        return FORM_TEMPLATES_BY_NAME.get(formName).type;
    }

    @Override
    public ApplicationForm createEmptyForm(Application application, String formName) {
        ApplicationFormStatus status = new ApplicationFormStatus();
        status.setUpdated(new Date());
        status.setKey(ApplicationFormStatusKey.CREATED);

        ApplicationForm form = new ApplicationForm();
        form.setName(formName);
        form.setType(FORM_TEMPLATES_BY_NAME.get(formName).type);
        form.setStatus(status);
        form.setApplicationId(application.getId());
        form.setUserId(application.getUserId());

        return form;
    }

    @Override
    public Comparator<ApplicationForm> getFormComparator() {
        return (f1, f2) -> FORM_TEMPLATES_BY_NAME.get(f1.getName()).order
                .compareTo(FORM_TEMPLATES_BY_NAME.get(f2.getName()).order);
    }
}
