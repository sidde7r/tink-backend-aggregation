package se.tink.backend.aggregation.agents.banks.sbab.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.List;
import java.util.Map;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import static se.tink.backend.sbab.utils.CountryMaps.CITIZENSHIP_COUNTRY_MAPPING;
import static se.tink.backend.sbab.utils.CountryMaps.RESIDENCE_FOR_TAX_COUNTRY_MAPPING;

public class OpenSavingsAccountModelMapper {

    private static final Map<String, Map<String, String>> VALUE_MAP_BY_FIELD = ImmutableMap.<String, Map<String, String>>builder()
            
            .put(ApplicationFieldName.SBAB_SAVINGS_PURPOSE,
                    ImmutableMap.<String, String>builder()
                    .put(ApplicationFieldOptionValues.PURPOSE_PAYMENTS_OF_LOAN_AT_SBAB, "1")
                    .put(ApplicationFieldOptionValues.PURPOSE_OPERATING_COSTS_OF_MY_RESIDENCE, "2")
                    .put(ApplicationFieldOptionValues.PURPOSE_FINANCIAL_BUFFER, "3")
                    .put(ApplicationFieldOptionValues.PURPOSE_ONGOING_EXPENSES, "4")
                    .put(ApplicationFieldOptionValues.PURPOSE_SAVING_FOR_RELATED_PARTIES, "5")
                    .put(ApplicationFieldOptionValues.OTHER, "6")
                    .build())

            .put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON,
                    ImmutableMap.<String, String>builder()
                    .put(ApplicationFieldOptionValues.SALARY_OR_PENSION, "1")
                    .put(ApplicationFieldOptionValues.OTHER_SAVINGS, "2")
                    .put(ApplicationFieldOptionValues.SALE_OF_ASSETS, "3")
                    .put(ApplicationFieldOptionValues.GIFT_OR_INHERITANCE, "4")
                    .put(ApplicationFieldOptionValues.OWN_BUSINESS_OR_DIVIDEND, "5")
                    .put(ApplicationFieldOptionValues.OTHER, "6")
                    .build())
                    
            .put(ApplicationFieldName.SBAB_INITIAL_DEPOSIT,
                    ImmutableMap.<String, String>builder()
                    .put(ApplicationFieldOptionValues.LESS_THAN_50000, "1")
                    .put(ApplicationFieldOptionValues.BETWEEN_50000_AND_150000, "2")
                    .put(ApplicationFieldOptionValues.BETWEEN_150000_AND_250000, "3")
                    .put(ApplicationFieldOptionValues.BETWEEN_250000_AND_350000, "4")
                    .put(ApplicationFieldOptionValues.MORE_THAN_350000, "5")
                    .build())
                    
            .put(ApplicationFieldName.SBAB_SAVINGS_FREQUENCY,
                    ImmutableMap.<String, String>builder()
                    .put(ApplicationFieldOptionValues.LESS_THAN_ONCE_A_MONTH, "1")
                    .put(ApplicationFieldOptionValues.ONE_TO_FIVE_TIMES_A_MONTH, "2")
                    .put(ApplicationFieldOptionValues.SIX_TO_TEN_TIMES_A_MONTH, "3")
                    .put(ApplicationFieldOptionValues.MORE_THAN_TEN_TIMES_A_MONTH, "4")
                    .build())
                    
            .put(ApplicationFieldName.SBAB_SAVINGS_AMOUNT_PER_MONTH,
                    ImmutableMap.<String, String>builder()
                    .put(ApplicationFieldOptionValues.LESS_THAN_10000, "1")
                    .put(ApplicationFieldOptionValues.BETWEEN_10000_AND_20000, "2")
                    .put(ApplicationFieldOptionValues.BETWEEN_20000_AND_30000, "3")
                    .put(ApplicationFieldOptionValues.BETWEEN_30000_AND_40000, "4")
                    .put(ApplicationFieldOptionValues.MORE_THAN_40000, "4")
                    .build())
                    
            .put(ApplicationFieldName.SBAB_PERSONS_SAVING,
                    ImmutableMap.<String, String>builder()
                    .put(ApplicationFieldOptionValues.PERSONS_SAVING_ME, "1")
                    .put(ApplicationFieldOptionValues.PERSONS_SAVING_OTHER, "2")
                    .build())
                    
            .put(ApplicationFieldName.SBAB_SAVINGS_SOURCES,
                    ImmutableMap.<String, String>builder()
                    .put(ApplicationFieldOptionValues.MY_ACCOUNT_IN_SWEDISH_BANK, "1")
                    .put(ApplicationFieldOptionValues.OTHER_WAY, "2")
                    .build())
                    
            .put(ApplicationFieldName.SBAB_MONEY_WITHDRAWAL,
                    ImmutableMap.<String, String>builder()
                    .put(ApplicationFieldOptionValues.LESS_THAN_ONCE_A_MONTH, "1")
                    .put(ApplicationFieldOptionValues.ONE_TO_FIVE_TIMES_A_MONTH, "2")
                    .put(ApplicationFieldOptionValues.FIVE_TO_TEN_TIMES_A_MONTH, "3")
                    .put(ApplicationFieldOptionValues.MORE_THAN_TEN_TIMES_A_MONTH, "4")
                    .build())
            
            .put(ApplicationFieldName.SBAB_SAVINGS_MONTHLY_INCOME,
                    ImmutableMap.<String, String>builder()
                    .put(ApplicationFieldOptionValues.LESS_THAN_20000, "1")
                    .put(ApplicationFieldOptionValues.BETWEEN_20000_AND_35000, "2")
                    .put(ApplicationFieldOptionValues.BETWEEN_35000_AND_50000, "3")
                    .put(ApplicationFieldOptionValues.BETWEEN_50000_AND_70000, "4")
                    .put(ApplicationFieldOptionValues.MORE_THAN_70000, "5")
                    .build())
            
            .put(ApplicationFieldName.IS_PEP,
                    ImmutableMap.<String, String>builder()
                    .put(ApplicationFieldOptionValues.YES, "1")
                    .put(ApplicationFieldOptionValues.NO, "2")
                    .build())

            .put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_MY_ACCOUNT_IN_SWEDISH_BANK,
                    ImmutableMap.<String, String>builder()
                    .put(ApplicationFieldOptionValues.SWEDBANK, "1")
                    .put(ApplicationFieldOptionValues.HANDELSBANKEN, "2")
                    .put(ApplicationFieldOptionValues.NORDEA, "3")
                    .put(ApplicationFieldOptionValues.SEB, "4")
                    .put(ApplicationFieldOptionValues.DANSKE_BANK, "5")
                    .put(ApplicationFieldOptionValues.OTHER, "6")
                    .build())

            .put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY,
                    ImmutableMap.<String, String>builder()
                    .put(ApplicationFieldOptionValues.INDUSTRY_IT, "1")
                    .put(ApplicationFieldOptionValues.INDUSTRY_SALES, "2")
                    .put(ApplicationFieldOptionValues.INDUSTRY_HOTEL, "3")
                    .put(ApplicationFieldOptionValues.INDUSTRY_CULTURE, "4")
                    .put(ApplicationFieldOptionValues.INDUSTRY_RESTAURANT, "5")
                    .put(ApplicationFieldOptionValues.INDUSTRY_PAYMENTS, "6")
                    .put(ApplicationFieldOptionValues.INDUSTRY_GAMING, "7")
                    .put(ApplicationFieldOptionValues.INDUSTRY_HAIRDRESSER, "8")
                    .put(ApplicationFieldOptionValues.INDUSTRY_HEALTH, "9")
                    .put(ApplicationFieldOptionValues.INDUSTRY_CONSTRUCTION, "10")
                    .put(ApplicationFieldOptionValues.INDUSTRY_WEAPON, "11")
                    .put(ApplicationFieldOptionValues.INDUSTRY_TECH, "12")
                    .put(ApplicationFieldOptionValues.OTHER, "13")
                    .build())
                    
            .build();
    
    public static String getAnswer(String fieldName, String fieldValue) {
        
        if (Strings.isNullOrEmpty(fieldName) || Strings.isNullOrEmpty(fieldValue)) {
            return null;
        }
        
        Map<String, String> valueMap = VALUE_MAP_BY_FIELD.get(fieldName);
        
        if (valueMap == null || valueMap.isEmpty()) {
            return null;
        }
        
        return valueMap.get(fieldValue);
    }
    
    public static String getAnswer(GenericApplicationFieldGroup fieldGroup, String fieldName) {
        
        if (fieldGroup == null ||  Strings.isNullOrEmpty(fieldName)) {
            return null;
        }
        
        String fieldValue = fieldGroup.getField(fieldName);
        
        if (Strings.isNullOrEmpty(fieldValue)) {
            return null;
        }
        
        return getAnswer(fieldName, fieldValue);
    }
    
    public static void populateAnswer(MultivaluedMapImpl informationBody, String answerKey, String answer) {
        if (!Strings.isNullOrEmpty(answer)) {
            informationBody.putSingle(String.format("svar['%s']", answerKey), answer);
        }
    }

    public static void populateAnswer(MultivaluedMapImpl informationBody, String answerKey,
            GenericApplicationFieldGroup fieldGroup, String fieldName) {
        populateAnswer(informationBody, answerKey, getAnswer(fieldGroup, fieldName));
    }
    
    public static void populateMultiSelectAnswer(MultivaluedMapImpl informationBody, String answerKey, String answer) {
        String multiSelectAnswerkey = String.format("%s_%s", answerKey, answer);
        populateAnswer(informationBody, multiSelectAnswerkey, "on");
    }

    public static void populateMultiSelectAnswers(MultivaluedMapImpl informationBody, String answerKey,
            GenericApplicationFieldGroup fieldGroup, String fieldName) {
        List<String> values = fieldGroup.getFieldAsListOfStrings(fieldName);

        Preconditions.checkNotNull(values, "Field values == null. Should be a list in a valid multi select form. "
                + "Did we use wrong form type in application?");

        for (String value : values) {
            populateMultiSelectAnswer(informationBody, answerKey, getAnswer(fieldName, value));
        }
    }

    /*
     * In the SBAB form model, an answer in a (multi)select question might require a custom free-text supplement.
     * This method populates the body with the answer of such a question.
     */
    public static void populateMultiSelectCustomValueAnswer(MultivaluedMapImpl informationBody, String answerKey,
            String answer, String customValue) {
        String customValueAnswerKey = String.format("%s_%s_fritext", answerKey, answer);
        populateAnswer(informationBody, customValueAnswerKey, customValue);
    }

    /**
     * In the SBAB form model, a free text answer supplement might require a trigger of a hidden input with value 1.
     * The actual value is placed in the corresponding _1_fritext field.
     */
    public static void populateSelectCustomAnswerWithHiddenTrigger(MultivaluedMapImpl informationBody, String answerKey,
            String customValue) {
        populateAnswer(informationBody, answerKey, "1");
        populateAnswer(informationBody, answerKey + "_1_fritext", customValue);
    }
    
    public static String getCitizenshipCountryAnswer(String countryCode) {
        if (Strings.isNullOrEmpty(countryCode)) {
            return null;
        }
        
        return CITIZENSHIP_COUNTRY_MAPPING.get(countryCode.toUpperCase());
    }
    
    public static String getResidenceForTaxPurposesAnswer(String countryCode) {
        if (Strings.isNullOrEmpty(countryCode)) {
            return null;
        }
        
        return RESIDENCE_FOR_TAX_COUNTRY_MAPPING.get(countryCode.toUpperCase());
    }
}
