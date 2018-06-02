package se.tink.backend.common.application.mortgage;

import java.time.LocalDate;
import java.util.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import se.tink.backend.common.application.ApplicationTemplate;
import se.tink.backend.common.application.field.ApplicationFieldValidator;
import se.tink.backend.common.application.form.ApplicationFormValidator;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationFieldOption;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.Currency;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.serialization.TypeReferences;
import se.tink.backend.utils.ApplicationUtils;

public class SwitchMortgageProviderFormValidator extends ApplicationFormValidator {

    private final CurrencyRepository currencyRepository;
    
    public SwitchMortgageProviderFormValidator(final ApplicationTemplate template,
            User user, final ApplicationFieldValidator fieldValidator, CurrencyRepository currencyRepository) {
        super(template, user, fieldValidator);
        
        this.currencyRepository = currencyRepository;
    }
    
    @Override
    public void validate(ApplicationForm form, Application application) {
        super.validate(form, application);
        
        if (!form.hasError()) {
            switch (form.getName()) {
            case ApplicationFormName.CURRENT_MORTGAGES: {
                Optional<Double> loanAmount = getSelectedMortgageAbsoluteAmount(form);
                validateCurrentMortgage(form, application, loanAmount);                
                break;
            }
            case ApplicationFormName.MORTGAGE_PRODUCTS: {
                Optional<Double> loanAmount = getSelectedMortgageAbsoluteAmount(application);
                validateTotalMortgageAmount(form, application, loanAmount);
                break;
            }
            case ApplicationFormName.APPLICANT: {
                // The mortgage debt ratio is only applicable if you don't have a co-applicant.

                if (ApplicationUtils.isFirstYes(application, ApplicationFormName.HAS_CO_APPLICANT,
                        ApplicationFieldName.HAS_CO_APPLICANT)) {
                    break;
                }

                // Find out the applicants age and monthly income.

                Optional<ApplicationField> personalNumberField = form.getField(ApplicationFieldName.PERSONAL_NUMBER);

                if (!personalNumberField.isPresent()) {
                    break;
                }

                SocialSecurityNumber.Sweden ssn = new SocialSecurityNumber.Sweden(personalNumberField.get().getValue());

                if (!ssn.isValid()) {
                    break;
                }

                int age = ssn.getAge(LocalDate.now());

                Optional<ApplicationField> monthlyIncomeField = form.getField(ApplicationFieldName.MONTHLY_INCOME);

                if (!monthlyIncomeField.isPresent()) {
                    break;
                }

                Double monthlyIncome = Double.parseDouble(monthlyIncomeField.get().getValue());

                // Find the selected mortgage accounts.

                Optional<ApplicationField> currentMortgageField = ApplicationUtils.getFirst(application,
                        ApplicationFormName.CURRENT_MORTGAGES, ApplicationFieldName.CURRENT_MORTGAGE);

                if (!currentMortgageField.isPresent() || Strings.isNullOrEmpty(currentMortgageField.get().getValue())) {
                    break;
                }

                // Sum the total mortgage amount.
                double selectedMortgageAbsoluteAmount = getSelectedMortgageAbsoluteAmount(currentMortgageField.get());

                // Evaluate the mortgage debt ratio.

                int mortgageDebtRatio;
                
                switch (MortgageProvider.fromApplication(application)) {
                case SEB_BANKID:
                    mortgageDebtRatio = age < 36 ? 6 : 5;
                    break;
                case SBAB_BANKID:
                default:
                    mortgageDebtRatio = 6;
                }

                if (monthlyIncome * 12 * mortgageDebtRatio < selectedMortgageAbsoluteAmount) {
                    form.updateStatusIfChanged(ApplicationFormStatusKey.DISQUALIFIED,
                            "På grund av att den inkomst du uppgett är för låg i relation till storleken på ditt bolån kan vi tyvärr inte hjälpa dig.");
                }

                break;
            }
            case ApplicationFormName.EMPLOYMENT: {
                if (isUnemployedOrStudent(form)) {
                    if (ApplicationUtils.isFirstNo(application, ApplicationFormName.HAS_CO_APPLICANT,
                            ApplicationFieldName.HAS_CO_APPLICANT)) {
                        form.updateStatusIfChanged(
                                ApplicationFormStatusKey.DISQUALIFIED,
                                "På grund av din sysselsättning kan vi tyvärr inte erbjuda dig något bolån, om du inte har någon medsökande.");
                    }
                }
                break;
            }
            case ApplicationFormName.SBAB_EMPLOYMENT: {
                if (isUnemployedOrStudent(form)) {
                    if (ApplicationUtils.isFirstNo(application, ApplicationFormName.HAS_CO_APPLICANT,
                            ApplicationFieldName.HAS_CO_APPLICANT)) {
                        form.updateStatusIfChanged(
                                ApplicationFormStatusKey.DISQUALIFIED,
                                "På grund av din sysselsättning kan vi tyvärr inte erbjuda dig något bolån, om du inte har någon medsökande.");
                    }
                }
                break;
            }
            case ApplicationFormName.SALARY_IN_FOREIGN_CURRENCY: {
                if (ApplicationUtils.isYes(form, ApplicationFieldName.SALARY_IN_FOREIGN_CURRENCY)) {
                    form.updateStatusIfChanged(
                            ApplicationFormStatusKey.DISQUALIFIED,
                            "Vi stödjer för tillfället inte löner i en annan valuta än svenska kronor, vänligen kontakta SEB för att få hjälp med ditt bolån."
                    );
                }
                break;
            }
            case ApplicationFormName.CO_APPLICANT_EMPLOYMENT: {
                if (isUnemployedOrStudent(form)) {
                    Optional<ApplicationForm> applicantEmploymentForm = ApplicationUtils.getFirst(application,
                            ApplicationFormName.EMPLOYMENT);
                    
                    if (!applicantEmploymentForm.isPresent()) {
                        applicantEmploymentForm = ApplicationUtils.getFirst(application,
                                ApplicationFormName.SBAB_EMPLOYMENT);
                        if (!applicantEmploymentForm.isPresent()) {
                            break;
                        }
                    }

                    if (isUnemployedOrStudent(applicantEmploymentForm.get())) {
                        form.updateStatusIfChanged(ApplicationFormStatusKey.DISQUALIFIED,
                                "På grund av din och din mesökandes sysselsättning kan vi tyvärr inte erbjuda dig något bolån.");
                    }
                }
                break;
            }
            case ApplicationFormName.SBAB_CO_APPLICANT_EMPLOYMENT: {
                if (isUnemployedOrStudent(form)) {
                    Optional<ApplicationForm> applicantEmploymentForm = ApplicationUtils.getFirst(application,
                            ApplicationFormName.EMPLOYMENT);
                    
                    if (!applicantEmploymentForm.isPresent()) {
                        applicantEmploymentForm = ApplicationUtils.getFirst(application,
                                ApplicationFormName.SBAB_EMPLOYMENT);
                        if (!applicantEmploymentForm.isPresent()) {
                            break;
                        }
                    }

                    if (isUnemployedOrStudent(applicantEmploymentForm.get())) {
                        form.updateStatusIfChanged(ApplicationFormStatusKey.DISQUALIFIED,
                                "På grund av din och din mesökandes sysselsättning kan vi tyvärr inte erbjuda dig något bolån.");
                    }
                }
                break;
            }
            case ApplicationFormName.SBAB_TAXABLE_IN_SWEDEN:
            case ApplicationFormName.TAXABLE_IN_SWEDEN: {
                if (ApplicationUtils.isNo(form, ApplicationFieldName.TAXABLE_IN_SWEDEN)) {
                    form.updateStatusIfChanged(
                            ApplicationFormStatusKey.DISQUALIFIED,
                            "För personer utan skatterättslig hemvist i Sverige krävs extra uppgifter som Tink inte har stöd för idag. Kontakta banken för att få hjälp med ditt bolån.");
                }
                break;
            }
            case ApplicationFormName.SBAB_IS_PEP:
            case ApplicationFormName.IS_PEP: {
                if (ApplicationUtils.isYes(form, ApplicationFieldName.IS_PEP)) {
                    form.updateStatusIfChanged(
                            ApplicationFormStatusKey.DISQUALIFIED,
                            "För personer i politiskt utsatt ställning krävs extra uppgifter som Tink inte har stöd för idag. Kontakta banken för att få hjälp med ditt bolån.");
                }
                break;
            }
            case ApplicationFormName.ON_OWN_BEHALF: {
                if (ApplicationUtils.isNo(form, ApplicationFieldName.ON_OWN_BEHALF)) {
                    form.updateStatusIfChanged(
                            ApplicationFormStatusKey.DISQUALIFIED,
                            "För att ansökan ska vara giltig, måste den fyllas i för egen räkning.");
                }
                break;
            }
            case ApplicationFormName.OTHER_LOANS:
            case ApplicationFormName.CO_APPLICANT_OTHER_LOANS: {
                if (!Objects.equals(form.getStatus().getKey(), ApplicationFormStatusKey.COMPLETED)) {
                    break;
                }
                if (ApplicationUtils.isFalse(form, ApplicationFieldName.OTHER_LOAN)) {
                    break;
                }
                Optional<ApplicationField> loanLenderField = form.getField(ApplicationFieldName.LOAN_LENDER);
                if (!loanLenderField.isPresent()) {
                    break;
                }
                Optional<ApplicationField> loanAmountField = form.getField(ApplicationFieldName.LOAN_AMOUNT);
                if (!loanAmountField.isPresent()) {
                    break;
                }
                Optional<ApplicationField> otherLoanField = form.getField(ApplicationFieldName.OTHER_LOAN);
                if (!otherLoanField.isPresent()) {
                    break;
                }

                // Reset other fields if submitted form has other loan added by user.
                loanLenderField.get().setValue(null);
                loanAmountField.get().setValue(null);
                otherLoanField.get().setValue(null);
                form.updateStatus(ApplicationFormStatusKey.IN_PROGRESS);
                break;
            }
            case ApplicationFormName.OTHER_ASSETS: {
                if (!Objects.equals(form.getStatus().getKey(), ApplicationFormStatusKey.COMPLETED)) {
                    break;
                }
                if (ApplicationUtils.isFalse(form, ApplicationFieldName.OTHER_ASSET)) {
                    break;
                }
                Optional<ApplicationField> assetNameField = form.getField(ApplicationFieldName.ASSET_NAME);
                if (!assetNameField.isPresent()) {
                    break;
                }
                Optional<ApplicationField> assetValueField = form.getField(ApplicationFieldName.ASSET_VALUE);
                if (!assetValueField.isPresent()) {
                    break;
                }
                Optional<ApplicationField> otherAssetField = form.getField(ApplicationFieldName.OTHER_ASSET);
                if (!otherAssetField.isPresent()) {
                    break;
                }

                // Reset other fields if submitted form has other loan added by user.
                assetNameField.get().setValue(null);
                assetValueField.get().setValue(null);
                otherAssetField.get().setValue(null);
                form.updateStatus(ApplicationFormStatusKey.IN_PROGRESS);
                break;
            }
            default:
                // Do nothing.
            }
        }
    }

    private void validateTotalMortgageAmount(ApplicationForm form, Application application,
            Optional<Double> loanAmount) {

        if (!loanAmount.isPresent()) {
            return;
        }

        int maxLoanAmount;

        switch (MortgageProvider.fromApplication(application)) {
        case SEB_BANKID:
            maxLoanAmount = 5000000;
            break;
        case SBAB_BANKID:
            String mortgageSecurityPropertyType = ApplicationUtils.getMortgageSecurityPropertyType(application);

            if (Objects.equals(mortgageSecurityPropertyType, ApplicationFieldOptionValues.VACATION_HOUSE)) {
                maxLoanAmount = 3000000;
            } else {
                maxLoanAmount = 8000000;
            }
            break;
        default:
            // When no product has been selected yet, don't validate it.
            return;
        }

        if (loanAmount.get() > maxLoanAmount) {
            Currency currency = currencyRepository.findOne("SEK");
            String currencyFormattedAmount = I18NUtils.formatCurrencyRound(Math.abs(maxLoanAmount), currency, locale);
            form.updateStatusIfChanged(ApplicationFormStatusKey.DISQUALIFIED, Catalog.format(
                    "Ditt lån är för stort. För att få flytta ditt lån får det vara som högst {0}.",
                    currencyFormattedAmount));
        }
    }

    private static void validateCurrentMortgage(ApplicationForm form, Application application,
            Optional<Double> loanAmount) {

        if (!Objects.equals(form.getStatus().getKey(), ApplicationFormStatusKey.DISQUALIFIED)) {
            validateCurrentMortgageMinimumAmount(form, application, loanAmount);
        }

        if (!Objects.equals(form.getStatus().getKey(), ApplicationFormStatusKey.DISQUALIFIED)) {
            validateCurrentMortgageDebtRatio(form, application, loanAmount);
        }
    }

    private static void validateCurrentMortgageMinimumAmount(ApplicationForm form, Application application,
            Optional<Double> loanAmount) {

        if (!loanAmount.isPresent()) {
            return;
        }

        // Both SBAB and SEB require at least 200k.
        if (loanAmount.get() < 200000) {
            form.updateStatusIfChanged(
                    ApplicationFormStatusKey.DISQUALIFIED,
                    "Ditt lån är för litet. För att kunna flytta ditt lån behöver det vara minst 200 000 kr.");
        }
    }

    private static void validateCurrentMortgageDebtRatio(ApplicationForm form, Application application,
            Optional<Double> loanAmount) {

        if (!loanAmount.isPresent()) {
            return;
        }

        Optional<ApplicationField> estimatedMarketValueField = ApplicationUtils.getFirst(application,
                ApplicationFormName.MORTGAGE_SECURITY_MARKET_VALUE, ApplicationFieldName.ESTIMATED_MARKET_VALUE);

        if (!estimatedMarketValueField.isPresent() || Strings.isNullOrEmpty(estimatedMarketValueField.get().getValue())) {
            return;
        }

        double estimatedMarketValue = Double.valueOf(estimatedMarketValueField.get().getValue());

        boolean isQualifiedMortgageValueRate = estimatedMarketValue != 0
                && (loanAmount.get() / estimatedMarketValue) <= 0.85;

        if (!isQualifiedMortgageValueRate) {
            form.updateStatusIfChanged(
                    ApplicationFormStatusKey.DISQUALIFIED,
                    "Ditt lån är för stort i förhållande till värdet som du har uppgett på ditt boende. För att få flytta ditt lån så behöver du ha en belåningsgrad på högst 85 %.");
        }
    }
    
    private static Optional<Double> getSelectedMortgageAbsoluteAmount(Application application) {
        Optional<ApplicationForm> form = application.getFirstForm(ApplicationFormName.CURRENT_MORTGAGES);
        
        if (form.isPresent()) {
            return getSelectedMortgageAbsoluteAmount(form.get());
        }
        
        return Optional.empty();
    }
    
    private static Optional<Double> getSelectedMortgageAbsoluteAmount(ApplicationForm form) {
        Optional<ApplicationField> currentMortgageField = form.getField(ApplicationFieldName.CURRENT_MORTGAGE);

        if (!currentMortgageField.isPresent() || Strings.isNullOrEmpty(currentMortgageField.get().getValue())) {
            return Optional.empty();
        }

        return Optional.of(getSelectedMortgageAbsoluteAmount(currentMortgageField.get()));
    }

    private static double getSelectedMortgageAbsoluteAmount(ApplicationField mortgageField) {
        double selectedMortgageAbsoluteAmount = 0;
        Set<String> selectedMortgageAccountId = Sets.newHashSet(SerializationUtils.deserializeFromString(
                mortgageField.getValue(), TypeReferences.LIST_OF_STRINGS));

        for (ApplicationFieldOption currentMortgageFieldOption : mortgageField.getOptions()) {
            if (!selectedMortgageAccountId.contains(currentMortgageFieldOption.getValue())) {
                continue;
            }

            Map<String, Object> payload = SerializationUtils
                    .deserializeFromString(currentMortgageFieldOption.getSerializedPayload(), TypeReferences.MAP_OF_STRING_OBJECT);

            selectedMortgageAbsoluteAmount += Math.abs((Double)payload.get("amount"));
        }
        return selectedMortgageAbsoluteAmount;
    }

    private static boolean isUnemployedOrStudent(ApplicationForm employmentForm) {
        Optional<ApplicationField> empoloymentTypeField = employmentForm.getField(ApplicationFieldName.EMPLOYMENT_TYPE);
        
        if (!empoloymentTypeField.isPresent()) {
            return false;
        }
        
        String empoloymentType = empoloymentTypeField.get().getValue();
        
        if (Objects.equals(empoloymentType, ApplicationFieldOptionValues.UNEMPLOYED)) {
            return true;
        } else if (Objects.equals(empoloymentType, ApplicationFieldOptionValues.STUDENT_RESEARCHER)) {
            return true;
        } else {
            return false;
        }
    }

}
