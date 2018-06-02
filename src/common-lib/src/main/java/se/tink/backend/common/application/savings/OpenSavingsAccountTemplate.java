package se.tink.backend.common.application.savings;

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

public class OpenSavingsAccountTemplate extends ApplicationTemplate {

    private static final ImmutableMap<String, ApplicationFormTemplate> FORM_TEMPLATES_BY_NAME = ApplicationFormTemplateMap
            .builder()

            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCTS,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_PRODUCTS,
                    ApplicationFieldName.SAVINGS_PRODUCT)

            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCT_DETAILS,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_PRODUCT_DETAILS)

            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_APPLICANT,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_APPLICANT,
                    ApplicationFieldName.NAME,
                    ApplicationFieldName.PERSONAL_NUMBER,
                    ApplicationFieldName.EMAIL,
                    ApplicationFieldName.PHONE_NUMBER,
                    ApplicationFieldName.STREET_ADDRESS,
                    ApplicationFieldName.POSTAL_CODE,
                    ApplicationFieldName.TOWN)
                    
            .put(ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_KYC,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC,
                    ApplicationFieldName.COLLECTOR_SAVINGS_PURPOSE,
                    ApplicationFieldName.COLLECTOR_INITIAL_DEPOSIT,
                    ApplicationFieldName.COLLECTOR_SAVINGS_FREQUENCY,
                    ApplicationFieldName.COLLECTOR_SAVINGS_SOURCES,
                    ApplicationFieldName.COLLECTOR_SAVINGS_SOURCES_REASON,
                    ApplicationFieldName.COLLECTOR_MONEY_WITHDRAWAL)

            .put(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_PURPOSE,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC_MULTI_SELECT_WITH_DEPENDENCIES,
                    ApplicationFieldName.SBAB_SAVINGS_PURPOSE,
                    ApplicationFieldName.SBAB_SAVINGS_PURPOSE_OTHER_VALUE)

            .put(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_INITIAL_DEPOSIT,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC,
                    ApplicationFieldName.SBAB_INITIAL_DEPOSIT)

            .put(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_FREQUENCY,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC,
                    ApplicationFieldName.SBAB_SAVINGS_FREQUENCY)

            .put(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_PERSONS_SAVING,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC_MULTI_SELECT_WITH_DEPENDENCIES,
                    ApplicationFieldName.SBAB_PERSONS_SAVING,
                    ApplicationFieldName.SBAB_PERSONS_SAVING_OTHER_VALUE)

            .put(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_SOURCES,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC_MULTI_SELECT_WITH_DEPENDENCIES,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_MY_ACCOUNT_IN_SWEDISH_BANK,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_MY_ACCOUNT_IN_SWEDISH_BANK_OTHER_VALUE,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_OTHER_WAY_VALUE)

            .put(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_AMOUNT_PER_MONTH,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC,
                    ApplicationFieldName.SBAB_SAVINGS_AMOUNT_PER_MONTH)

            .put(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_SOURCES_REASON,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC_MULTI_SELECT_WITH_DEPENDENCIES,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_RESTAURANT_COMPANY_NAME,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_RESTAURANT_COMPANY_REGISTRATION_NUMBER,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_PAYMENT_COMPANY_NAME,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_PAYMENT_COMPANY_REGISTRATION_NUMBER,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_GAMING_COMPANY_NAME,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_GAMING_COMPANY_REGISTRATION_NUMBER,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_HAIRDRESSER_COMPANY_NAME,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_HAIRDRESSER_COMPANY_REGISTRATION_NUMBER,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_CONSTRUCTION_COMPANY_NAME,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_CONSTRUCTION_COMPANY_REGISTRATION_NUMBER,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_WEAPON_COMPANY_NAME,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_WEAPON_COMPANY_REGISTRATION_NUMBER,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_OTHER_VALUE,
                    ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OTHER_VALUE)

            .put(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_WITHDRAWAL,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC,
                    ApplicationFieldName.SBAB_MONEY_WITHDRAWAL)

            .put(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_MONTHLY_INCOME,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC,
                    ApplicationFieldName.SBAB_SAVINGS_MONTHLY_INCOME)

            .put(ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_WITHDRAWAL_ACCOUNT,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_WITHDRAWAL_ACCOUNT,
                    ApplicationFieldName.COLLECTOR_ACCOUNT_FOR_MONEY_WITHDRAWAL)

            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PEP,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC_PEP,
                    ApplicationFieldName.IS_PEP)

            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_SWEDISH_CITIZEN,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC_CITIZENSHIP,
                    ApplicationFieldName.SWEDISH_CITIZEN)
                    
            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_OTHER_COUNTRY,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC_CITIZENSHIP,
                    ApplicationFieldName.CITIZENSHIP_IN_OTHER_COUNTRY,
                    ApplicationFieldName.CITIZENSHIP_COUNTRY)

            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_YET_ANOTHER_COUNTRY,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC_CITIZENSHIP,
                    ApplicationFieldName.CITIZENSHIP_IN_YET_ANOTHER_COUNTRY,
                    ApplicationFieldName.CITIZENSHIP_COUNTRY)
            
            .put(ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_USA,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC_TAX,
                    ApplicationFieldName.TAXABLE_IN_USA)
                    
            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_USA,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC_TAX,
                    ApplicationFieldName.TAXABLE_IN_USA,
                    ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER_USA)

            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_OTHER_COUNTRY,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC_TAX,
                    ApplicationFieldName.TAXABLE_IN_OTHER_COUNTRY,
                    ApplicationFieldName.TAXABLE_COUNTRY,
                    ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER)
                    
            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_YET_ANOTHER_COUNTRY,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_KYC_TAX,
                    ApplicationFieldName.TAXABLE_IN_YET_ANOTHER_COUNTRY,
                    ApplicationFieldName.TAXABLE_COUNTRY,
                    ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER)

            .put(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_CONFIRMATION,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_CONFIRMATION,
                    ApplicationFieldName.SBAB_SAVINGS_CONFIRMATION)

            .put(ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_CONFIRMATION,
                    ApplicationFormType.OPEN_SAVINGS_ACCOUNT_CONFIRMATION,
                    ApplicationFieldName.COLLECTOR_SAVINGS_CONFIRMATION)

            .build();

    @Override
    public ApplicationType getType() {
        return ApplicationType.OPEN_SAVINGS_ACCOUNT;
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
