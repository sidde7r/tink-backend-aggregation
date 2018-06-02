package se.tink.backend.common.application.form;

import java.util.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.util.Objects;
import se.tink.backend.common.application.Enricher;
import se.tink.backend.common.application.field.ApplicationFieldEnricher;
import se.tink.backend.common.application.mortgage.MortgageProvider;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.enums.ApplicationFormStatusKey;

public class ApplicationFormEnricher extends Enricher {

    private static final ImmutableMap<String, LocalizableKey> FORM_STATUS_MESSAGES = ImmutableMap.<String, LocalizableKey> builder()
            .put(ApplicationFormStatusKey.AUTO_SAVED.name(), new LocalizableKey("The form is started but not complete"))
            .put(ApplicationFormStatusKey.COMPLETED.name(), new LocalizableKey("The form is validated and complete"))
            .put(ApplicationFormStatusKey.CREATED.name(), new LocalizableKey("The form is not yet started"))
            .put(ApplicationFormStatusKey.IN_PROGRESS.name(), new LocalizableKey("The form is started but not complete"))
            .build();

    private static final ImmutableMap<String, LocalizableKey> FORM_TITLES = ImmutableMap.<String, LocalizableKey>builder()
            .put(ApplicationFormName.APPLICANT, new LocalizableKey("Your profile"))
            .put(ApplicationFormName.SBAB_APPLICANT, new LocalizableKey("Your profile"))
            .put(ApplicationFormName.BAILMENT, new LocalizableKey("Did you bail someone loan?"))
            .put(ApplicationFormName.CO_APPLICANT, new LocalizableKey("Co-applicant"))
            .put(ApplicationFormName.CO_APPLICANT_ADDRESS, new LocalizableKey("Does your co-applicant live at the same address as you?"))
            .put(ApplicationFormName.CO_APPLICANT_EMPLOYMENT, new LocalizableKey("What is your co applicants employment?"))
            .put(ApplicationFormName.CO_APPLICANT_INTRODUCTION, new LocalizableKey("About you co-applicant"))
            .put(ApplicationFormName.CO_APPLICANT_OTHER_LOANS, new LocalizableKey("Does your co-applicant have other loans?"))
            .put(ApplicationFormName.CO_APPLICANT_PAYING_ALIMONY, new LocalizableKey("-"))
            .put(ApplicationFormName.CO_APPLICANT_RECEIVING_ALIMONY, new LocalizableKey("-"))
            .put(ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_CONFIRMATION, new LocalizableKey("Nice work!"))
            .put(ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_KYC, new LocalizableKey("Some more questions"))
            .put(ApplicationFormName.SWITCH_MORTGAGE_STATUS_CONFIRMATION, new LocalizableKey("Nice work!"))
            .put(ApplicationFormName.LOAN_TERMS_CONFIRMATION, new LocalizableKey("Nice job! Your application is complete."))
            .put(ApplicationFormName.OTHER_LOANS, new LocalizableKey("Do you have more loans?"))
            .put(ApplicationFormName.OTHER_ASSETS, new LocalizableKey("Do you have other assets?"))
            .put(ApplicationFormName.DEFERRAL_CAPITAL_GAINS_TAX, new LocalizableKey("Did you requested deferral of capital gains tax?"))
            .put(ApplicationFormName.EMPLOYMENT, new LocalizableKey("What is your employment?"))
            .put(ApplicationFormName.IS_PEP, new LocalizableKey("Are you or a family member in a political exposed position, or are you a known associate of such a person (PEP)?"))
            .put(ApplicationFormName.MORTGAGE_PRODUCTS, new LocalizableKey("Interest rates for you"))
            .put(ApplicationFormName.MORTGAGE_PRODUCTS_ALREADY_GOOD, new LocalizableKey("Congratulations! You already have a good interest rate!"))
            .put(ApplicationFormName.MORTGAGE_PRODUCTS_LOADING, new LocalizableKey("Tink thinks"))
            .put(ApplicationFormName.MORTGAGE_SECURITY_APARTMENT_DETAILS, new LocalizableKey("Apartment details"))
            .put(ApplicationFormName.MORTGAGE_SECURITY_APARTMENT_INTRODUCTION, new LocalizableKey("About your apartment"))
            .put(ApplicationFormName.MORTGAGE_SECURITY_HOUSE_DETAILS, new LocalizableKey("House details"))
            .put(ApplicationFormName.MORTGAGE_SECURITY_HOUSE_INTRODUCTION, new LocalizableKey("About your house"))
            .put(ApplicationFormName.MORTGAGE_SECURITY_MARKET_VALUE, new LocalizableKey("How much is your residence worth?"))            
            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_APPLICANT, new LocalizableKey("Just a couple of questions..."))
            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_OTHER_COUNTRY, new LocalizableKey("Do you have citizenship in other country than Sweden?"))
            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_YET_ANOTHER_COUNTRY, new LocalizableKey("Do you have citizenship in yet another country?"))
            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PEP, new LocalizableKey("Are you or someone in your family in a political exposed position?"))
            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_OTHER_COUNTRY, new LocalizableKey("Are you taxable in other country than Sweden?"))
            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCT_DETAILS, new LocalizableKey("About selected account"))
            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCTS, new LocalizableKey("Open savings account"))
            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_SWEDISH_CITIZEN, new LocalizableKey("Are you a Swedish citizen?"))
            .put(ApplicationFormName.PAYING_ALIMONY, new LocalizableKey("Are you paying alimony?"))
            .put(ApplicationFormName.RECEIVING_ALIMONY, new LocalizableKey("Are you receiving alimony?"))
            .put(ApplicationFormName.SBAB_PAYING_ALIMONY, new LocalizableKey("Are you paying alimony?"))
            .put(ApplicationFormName.SBAB_CO_APPLICANT_PAYING_ALIMONY, new LocalizableKey("Is your co-applicant paying alimony?"))
            .put(ApplicationFormName.SBAB_CSN_LOAN, new LocalizableKey("Do you have a student loan from CSN?"))
            .put(ApplicationFormName.SBAB_MORTGAGE_SECURITY_APARTMENT_DETAILS, new LocalizableKey("Apartment details"))
            .put(ApplicationFormName.SBAB_MORTGAGE_SECURITY_HOUSE_DETAILS, new LocalizableKey("House details"))
            .put(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_CONFIRMATION, new LocalizableKey("Nice work!"))
            .put(ApplicationFormName.SBAB_OTHER_PROPERTIES, new LocalizableKey("Do you have other properties?"))
            .put(ApplicationFormName.SEB_CSN_LOAN, new LocalizableKey("Do you have a student loan from CSN?"))
            .put(ApplicationFormName.SWITCH_MORTGAGE_STATUS_COMPLETE_TINK_PROFILE, new LocalizableKey("This is how it works"))
            .put(ApplicationFormName.TAXABLE_IN_OTHER_COUNTRY, new LocalizableKey("Are you taxable in another country?"))
            .put(ApplicationFormName.TAXABLE_IN_SWEDEN, new LocalizableKey("Are you taxable in Sweden?"))
            .put(ApplicationFormName.TAXABLE_IN_USA, new LocalizableKey("Are you taxable in USA?"))
            .put(ApplicationFormName.TINK_PROFILE_INTRODUCTION, new LocalizableKey("Questions about you"))
            .put(ApplicationFormName.VALUATION_RESIDENCE_TYPE, new LocalizableKey("What kind of residence is it?"))
            .put(ApplicationFormName.VALUATION_APARTMENT_PARAMETERS, new LocalizableKey("Apartment details"))
            .put(ApplicationFormName.VALUATION_HOUSE_PARAMETERS, new LocalizableKey("House details"))
            .put(ApplicationFormName.VALUATION_LOADING, new LocalizableKey("Tink thinks"))
            .put(ApplicationFormName.DIRECT_DEBIT, new LocalizableKey("Account for direct debit"))
            .put(ApplicationFormName.SIGNATURE, new LocalizableKey("Your signature"))
            .put(ApplicationFormName.SALARY_IN_FOREIGN_CURRENCY, new LocalizableKey("Do you have an income in another currency than Swedish crowns?"))
            .build();
    
    private static final ImmutableMap<String, LocalizableKey> FORM_DESCRIPTIONS = ImmutableMap.<String, LocalizableKey>builder()
            .put(ApplicationFormName.BAILMENT, new LocalizableKey("If you for example help a family member with taking a loan."))
            .put(ApplicationFormName.CO_APPLICANT_INTRODUCTION, new LocalizableKey("It may be helpful if you have the person nearby in case you do not have answers to all the questions"))
            .put(ApplicationFormName.CO_APPLICANT_OTHER_LOANS, new LocalizableKey("For example blanco loans, car loans or used credit on a credit card. Don't include other mortgages or student loans."))
            .put(ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_CONFIRMATION, new LocalizableKey("Confirm the Terms and Conditions and sign with BankID, and you can start using the account immediately!"))
            .put(ApplicationFormName.LOAN_TERMS_CONFIRMATION, new LocalizableKey("You are ready to apply! Here is everything you need to know about your new mortgage."))
            .put(ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_KYC, new LocalizableKey("We need some information about your new savings account."))
            .put(ApplicationFormName.OTHER_LOANS, new LocalizableKey("Second mortgage, car loans or used credit on a credit card. Don't include mortgages on other properties, for example the vacation house."))
            .put(ApplicationFormName.CURRENT_MORTGAGES, new LocalizableKey("If your second mortgage is absent, you can add it later on. Do not include loans for other properties, such as vacation home. You have to move the whole mortgage."))
            .put(ApplicationFormName.DEFERRAL_CAPITAL_GAINS_TAX, new LocalizableKey("If you for example sold an apartment and delayed paying of taxes."))
            .put(ApplicationFormName.SEB_CO_APPLICANT_BAILMENT, new LocalizableKey("If they for example helped a family member applying for a loan."))
            .put(ApplicationFormName.IS_PEP, new LocalizableKey("For example, work as prime minister, commander in cheif or judges of the Supreme Court."))
            .put(ApplicationFormName.MORTGAGE_PRODUCTS, new LocalizableKey("The interest rates are based on your information, your finances and loan size."))
            .put(ApplicationFormName.MORTGAGE_PRODUCTS_ALREADY_GOOD, new LocalizableKey("In comparison with other banks, you already have a good interest rate. But check on a regular basis to see if the interest rates change."))
            .put(ApplicationFormName.MORTGAGE_PRODUCTS_LOADING, new LocalizableKey("Calculating on your finances..."))
            .put(ApplicationFormName.MORTGAGE_SECURITY_APARTMENT_INTRODUCTION, new LocalizableKey("For the bank to be able to do a valuation they need to know more about your apartment."))
            .put(ApplicationFormName.MORTGAGE_SECURITY_HOUSE_INTRODUCTION, new LocalizableKey("For the bank to be able to do a valuation they need to know more about your house."))
            .put(ApplicationFormName.MORTGAGE_SECURITY_MARKET_VALUE, new LocalizableKey("Try to make the estimate as fair as possible to get more correct interest rate proposals."))
            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCT_DETAILS, new LocalizableKey("Details about your new savings account."))
            .put(ApplicationFormName.OTHER_ASSETS, new LocalizableKey("Those could be mutual funds, stocks, other securities, and savings. Do not include pension savings."))
            .put(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCTS, new LocalizableKey("Our partners' savings accounts are of course covered by the state deposit insurance, are totally free of charge and without binding period."))
            .put(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_CONFIRMATION, new LocalizableKey("Confirm the Terms and Conditions and sign with BankID, and you can start using the account immediately!"))
            .put(ApplicationFormName.TAXABLE_IN_OTHER_COUNTRY, new LocalizableKey("Your tax residence is the country where you pay your taxes, usually the country where you live or work."))
            .put(ApplicationFormName.TAXABLE_IN_USA, new LocalizableKey("For example, if you are an american citizen, have a green card or other relations to USA."))
            .put(ApplicationFormName.TINK_PROFILE_INTRODUCTION, new LocalizableKey("According to the law on measures against money laundering and terrorist financing and international tax law, the bank must have good knowledge of all its customers."))
            .put(ApplicationFormName.VALUATION_LOADING, new LocalizableKey("Calculating the value of your residence..."))
            .put(ApplicationFormName.DIRECT_DEBIT, new LocalizableKey("With direct debit your payment is debited every month from the account of your choice."))
            .put(ApplicationFormName.SALARY_IN_FOREIGN_CURRENCY, new LocalizableKey("We ask because changes in the exchange rate can affect your finances."))
            .build();

    private final ApplicationFieldEnricher fieldEnricher;

    public ApplicationFormEnricher(final ApplicationFieldEnricher fieldEnricher) {
        this.fieldEnricher = fieldEnricher;
    }

    private String getString(Catalog catalog, ImmutableMap<String, LocalizableKey> map, ApplicationFormStatusKey key) {
        return getString(catalog, map, key.name());
    }

    public void enrich(ApplicationForm form, Application application, Catalog catalog) {

        if (Strings.isNullOrEmpty(form.getStatus().getMessage())) {
            form.getStatus().setMessage(getString(catalog, FORM_STATUS_MESSAGES, form.getStatus().getKey()));
        }

        if (Strings.isNullOrEmpty(form.getTitle())) {
            form.setTitle(getCustomTitle(form));
            
            if (Strings.isNullOrEmpty(form.getTitle())) {
                form.setTitle(getString(catalog, FORM_TITLES, form.getName()));
            }
        }
        
        if (Strings.isNullOrEmpty(form.getDescription())) {
            form.setDescription(getCustomDescription(form, application));

            if (Strings.isNullOrEmpty(form.getDescription())) {
                form.setDescription(getString(catalog, FORM_DESCRIPTIONS, form.getName()));
            }
        }

        for (ApplicationField field : form.getFields()) {
            fieldEnricher.enrich(field, form, application, catalog);
        }
    }
    
    private String getCustomTitle(ApplicationForm form) {

        if (form == null || Strings.isNullOrEmpty(form.getName())) {
            return null;
        }

        switch (form.getName()) {
        case ApplicationFormName.MORTGAGE_SECURITY: {

            Optional<ApplicationField> streetAddress = form.getField(ApplicationFieldName.DEFAULT_STREET_ADDRESS);

            if (streetAddress.isPresent()) {
                String value = streetAddress.get().getValue();

                if (Strings.isNullOrEmpty(value)) {
                    value = streetAddress.get().getDefaultValue();
                }

                // FIXME: This copy became too long which caused the Yes/No buttons to be pushed out of the container
                // when displayed in an iPhone 5. Use this copy when a FE solution is in place.
//                String propertyType = isDefaultPropertyTypeApartment(form) ? "lägenheten" : "villan";
//                return Catalog.format("Är det lånet för {0} på {1} som du vill utmana räntan för?", propertyType, value);

                return Catalog.format("Är det lånet på {0} som du vill utmana räntan för?", value);
            }

            return null;
        }
        default:
            return null;
        }
    }

    private boolean isDefaultPropertyTypeApartment(ApplicationForm form) {
        ApplicationField field = form.getField(ApplicationFieldName.DEFAULT_PROPERTY_TYPE).get();
        return Objects.equals(field.getValue(), ApplicationFieldOptionValues.APARTMENT);
    }

    private String getCustomDescription(ApplicationForm form, Application application) {
        if (form == null || Strings.isNullOrEmpty(form.getName())) {
            return null;
        }

        switch (form.getName()) {
        case ApplicationFormName.SWITCH_MORTGAGE_STATUS_COMPLETE_TINK_PROFILE:
            return getStatusCompleteTinkProfileDescription(application);
        case ApplicationFormName.SWITCH_MORTGAGE_STATUS_CONFIRMATION:
            return getStatusConfirmationDescription(application);
        default:
            return null;
        }
    }

    private static String getStatusCompleteTinkProfileDescription(Application application) {
        switch (MortgageProvider.fromApplication(application)) {
        case SEB_BANKID:
            return "Du kan skicka en ansökan om att flytta ditt lån via Tink, men flytten sker inte förrän du har skrivit på papper som du får av banken.";
        case SBAB_BANKID:
            return "Du kan skicka en ansökan om att flytta ditt lån via Tink, genom att svara på några frågor från banken. Ansökan är inte bindande förrän du har skrivit på bankens kontrakt.";
        default:
            return null;
        }
    }

    private static String getStatusConfirmationDescription(Application application) {
        switch (MortgageProvider.fromApplication(application)) {
        case SEB_BANKID:
            return "Du är redo att skicka in din ansökan. Flytten av bolånet sker inte förrän du har skrivit på papper från banken.";
        case SBAB_BANKID:
            return "Du är redo att skicka in din ansökan till banken. Flytten av ditt bolån är inte bindande förrän du har skrivit på papper från banken.";
        default:
            return null;
        }
    }
}
