package se.tink.backend.common.application.field;

import java.util.Optional;
import com.google.common.base.Strings;
import java.util.Objects;
import se.tink.backend.common.application.Enricher;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationFieldOption;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.utils.ApplicationUtils;

public class ApplicationFieldEnricher extends Enricher {

    public void enrich(ApplicationField field, ApplicationForm form, Application application, Catalog catalog) {
        
        if (Strings.isNullOrEmpty(field.getLabel())) {
            field.setLabel(getCustomLabel(field, form, application));
            
            if (Strings.isNullOrEmpty(field.getLabel())) {                
                field.setLabel(tryGetString(catalog, ApplicationFieldStrings.FIELD_LABELS, field.getName()));
            }
        }
        
        if (Strings.isNullOrEmpty(field.getIntroduction())) {
            field.setIntroduction(getCustomIntroduction(field, form, application));
            
            if (Strings.isNullOrEmpty(field.getIntroduction())) {            
                field.setIntroduction(getString(catalog, ApplicationFieldStrings.FIELD_INTRODUCTIONS, field.getName()));
            }
        }
        
        if (Strings.isNullOrEmpty(field.getDescription())) {
            field.setDescription(getCustomDescription(field, form, application, catalog));
            
            if (Strings.isNullOrEmpty(field.getDescription())) {            
                field.setDescription(getString(catalog, ApplicationFieldStrings.FIELD_DESCRIPTIONS, field.getName()));
            }
        }
        
        if (Strings.isNullOrEmpty(field.getInfoTitle()) && Strings.isNullOrEmpty(field.getInfoBody())) {
            InfoSection<?,?> infoSection = ApplicationFieldStrings.INFO_SECTIONS.get(field.getName());
            
            if (infoSection != null) {
                if (infoSection.getTitle() instanceof LocalizableKey) {
                    field.setInfoTitle(catalog.getString((LocalizableKey) infoSection.getTitle()));
                } else {
                    field.setInfoTitle(String.valueOf(infoSection.getTitle()));
                }
                
                if (infoSection.getBody() instanceof LocalizableKey) {
                    field.setInfoBody(catalog.getString((LocalizableKey) infoSection.getBody()));
                } else {
                    field.setInfoBody(String.valueOf(infoSection.getBody()));
                }
            }
        }
        
        if (field.getOptions() != null) {
            for (ApplicationFieldOption option : field.getOptions()) {
                if (Strings.isNullOrEmpty(option.getLabel())) {
                    option.setLabel(getCustomOptionLabel(option.getValue(), field, form));
                            
                    if (Strings.isNullOrEmpty(option.getLabel())) {
                        option.setLabel(tryGetString(catalog, ApplicationFieldStrings.OPTION_LABELS, option.getValue()));
                    }
                }

                if (Strings.isNullOrEmpty(option.getDescription())) {
                    option.setDescription(getString(catalog, ApplicationFieldStrings.OPTION_DESCRIPTIONS,
                            option.getValue()));
                }
            }
        }

        if (field.hasError()) {
            
            Optional<String> message = ApplicationUtils.getFirstErrorMessage(field);
            
            if (message.isPresent()) {
                field.setDisplayError(message.get());
            } else {
                // TODO translate error to correct text
                field.setDisplayError("Has error");
            }
        }
    }
    
    private String getCustomOptionLabel(String value, ApplicationField field, ApplicationForm form) {
        
        if (form == null || Strings.isNullOrEmpty(form.getName())) {
            return null;
        }
        
        if (field == null || Strings.isNullOrEmpty(field.getName())) {
            return null;
        }
        
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        
        switch (form.getName()) {
        case ApplicationFormName.SBAB_OTHER_PROPERTIES:
        case ApplicationFormName.SBAB_CO_APPLICANT_OTHER_PROPERTIES: {
            switch (field.getName()) {
            case ApplicationFieldName.SBAB_PROPERTY_TYPE: {
                switch (value) {
                case ApplicationFieldOptionValues.HOUSE: {                    
                    return "Villa/Fritidshus";
                }
                default:
                    return null;
                }
            }
            default:
                return null;
            }
        }
        default:
            return null;
        }
    }
    
    private String getCustomLabel(ApplicationField field, ApplicationForm form, Application application) {
        
        if (form == null || Strings.isNullOrEmpty(form.getName())) {
            return null;
        }
        
        if (field == null || Strings.isNullOrEmpty(field.getName())) {
            return null;
        }
        
        switch (form.getName()) {
        case ApplicationFormName.OTHER_PROPERTIES: {
            switch (field.getName()) {
            case ApplicationFieldName.PROPERTY_TYPE: {
                boolean hasCoApplicant = ApplicationUtils.isFirstYes(application, ApplicationFormName.HAS_CO_APPLICANT,
                        ApplicationFieldName.HAS_CO_APPLICANT);

                if (hasCoApplicant) {
                    return "Har du eller din medsökande andra bostäder?";
                } else {
                    return "Har du andra bostäder?";
                }
            }
            default:
                return null;
            }
        }
        case ApplicationFormName.SBAB_IS_PEP: {
            switch (field.getName()) {
            case ApplicationFieldName.IS_PEP: {
                return "Är du en person i politiskt utsatt ställning, eller har du en familjemedlem eller medarbetare som är en sådan person?";
            }
            default:
                return null;
            }
        }
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PEP: {
            switch (field.getName()) {
            case ApplicationFieldName.IS_PEP: {
                if (application.getProductArticle().isPresent()) {
                    if (Objects.equals(application.getProductArticle().get().getProviderName(), "sbab-bankid")) {
                        return "Är du en person i politiskt utsatt ställning, eller har du en familjemedlem eller medarbetare som är en sådan person?";
                    } else {
                        return null;
                    }
                }
                // fall through
            }
            default:
                return null;
            }
        }
        case ApplicationFormName.CO_APPLICANT_EMPLOYMENT:
        case ApplicationFormName.SBAB_CO_APPLICANT_EMPLOYMENT: {
            switch (field.getName()) {
            case ApplicationFieldName.EMPLOYMENT_TYPE: {
                return "Vad är din medsökandes huvudsakliga sysselsättning?";
            }
            default:
                return null;
            }
        }
        case ApplicationFormName.SBAB_CO_APPLICANT_OTHER_PROPERTIES:{
            switch (field.getName()) {
            case ApplicationFieldName.SBAB_PROPERTY_TYPE: {
                return "Har din medsökande andra bostäder?";
            }
            default:
                return null;
            }
        }
        case ApplicationFormName.HOUSEHOLD_CHILDREN:
            return getCustomLabelForHouseholdChildren(field);
        case ApplicationFormName.SBAB_HOUSEHOLD_CHILDREN:
            return getCustomLabelForSbabHouseholdChildren(field);
        default:
            return null;
        }
    }

    private String getCustomLabelForHouseholdChildren(ApplicationField field) {
        if (field == null || Strings.isNullOrEmpty(field.getName())) {
            return null;
        }

        switch (field.getName()) {
        case ApplicationFieldName.HOUSEHOLD_CHILDREN:
            return "Bor det barn (under 18 år) i hushållet?";
        default:
            return null;
        }
    }

    private String getCustomLabelForSbabHouseholdChildren(ApplicationField field) {
        if (field == null || Strings.isNullOrEmpty(field.getName())) {
            return null;
        }

        switch (field.getName()) {
        case ApplicationFieldName.HOUSEHOLD_CHILDREN:
            return "Bor det barn i ditt hushåll?";
        default:
            return null;
        }
    }

    private String getCustomIntroduction(ApplicationField field, ApplicationForm form, Application application) {
        
        if (form == null || Strings.isNullOrEmpty(form.getName())) {
            return null;
        }
        
        switch (form.getName()) {
        case ApplicationFormName.SBAB_TAXABLE_IN_USA:
        case ApplicationFormName.TAXABLE_IN_USA: {
            return getCustomIntroductionForTIN(field, application);
        }
        case ApplicationFormName.HOUSEHOLD_CHILDREN:
            return getCustomIntroductionForHouseholdChildren(field);
        default:
            return null;
        }
    }

    private String getCustomIntroductionForHouseholdChildren(ApplicationField field) {
        if (field == null || Strings.isNullOrEmpty(field.getName())) {
            return null;
        }

        switch (field.getName()) {
        case ApplicationFieldName.HOUSEHOLD_CHILDREN_COUNT:
            return "Hur många barn bor i hushållet?";
        default:
            return null;
        }
    }

    private String getCustomIntroductionForTIN(ApplicationField field, Application application) {

        if (field == null || Strings.isNullOrEmpty(field.getName())) {
            return null;
        }
        
        switch (field.getName()) {
        case ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER_USA: {
            if (application.getProductArticle().isPresent()) {
                if (Objects.equals(application.getProductArticle().get().getProviderName(), "seb-bankid")) {
                    return "Ange ditt amerikanska skatteregistreringsnummer. Om du inte har något, kontakta SEB så hjälper de dig.";
                } else {
                    return "Ange ditt amerikanska skatteregistreringsnummer.";
                }
            }

            return null;
        }
        default:
            return null;
        }
    }

    private String getCustomDescription(ApplicationField field, ApplicationForm form, Application application,
            Catalog catalog) {
        
        if (form == null || Strings.isNullOrEmpty(form.getName())) {
            return null;
        }

        switch (form.getName()) {
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_OTHER_COUNTRY:
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_YET_ANOTHER_COUNTRY: {
            return getCustomDescriptionForCitizenship(field, form, application, catalog);
        }
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_OTHER_COUNTRY:
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_YET_ANOTHER_COUNTRY: {
            return getCustomDescriptionForResidenceForTaxPurposes(field, form, application, catalog);
        }
        case ApplicationFormName.SBAB_TAXABLE_IN_USA: {
            return getCustomDescriptionForTaxableInUSA_SBAB(field);
        }
        case ApplicationFormName.SBAB_HOUSEHOLD_CHILDREN:
            return getCustomDescriptionForSbabHouseholdChildren(field);
        default:
            return null;
        }
    }

    private String getCustomDescriptionForSbabHouseholdChildren(ApplicationField field) {
        if (field == null || Strings.isNullOrEmpty(field.getName())) {
            return null;
        }

        switch (field.getName()) {
        case ApplicationFieldName.HOUSEHOLD_CHILDREN:
            return "Även barn över 18 som bor hemma räknas.";
        default:
            return null;
        }
    }

    private String getCustomDescriptionForTaxableInUSA_SBAB(ApplicationField field) {
        
        if (field == null || Strings.isNullOrEmpty(field.getName())) {
            return null;
        }
        
        switch (field.getName()) {
        case ApplicationFieldName.TAXABLE_IN_USA: {
            return "Det har du om du är amerikansk medborgare, har ett Green card eller är bosatt i USA.";
        }
        default:
            return null;
        }
    }
    
    private String getCustomDescriptionForCitizenship(ApplicationField field, ApplicationForm form,
            Application application, Catalog catalog) {

        if (field == null || Strings.isNullOrEmpty(field.getName())) {
            return null;
        }
        
        switch (field.getName()) {
        case ApplicationFieldName.CITIZENSHIP_IN_OTHER_COUNTRY:
        case ApplicationFieldName.CITIZENSHIP_IN_YET_ANOTHER_COUNTRY: {
            
            boolean citizenInSweden = ApplicationUtils.isFirstYes(application,
                    ApplicationFormName.OPEN_SAVINGS_ACCOUNT_SWEDISH_CITIZEN,
                    ApplicationFieldName.SWEDISH_CITIZEN);
            
            boolean citizenInAnotherCountry = false;
            
            if (Objects.equals(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_YET_ANOTHER_COUNTRY, form.getName())) {
                // If we're populating the form asking if the user is a citizen in _yet_ another country, the user
                // obviously already entered that he/she was a citizen in another country (excluding Sweden).
                citizenInAnotherCountry = true;
            }
            
            if (citizenInSweden && citizenInAnotherCountry) {
                // Replace "previous choice" with the actual country, when the country list is not only in English.
                return catalog.getString("Besides Sweden and the previous choice.");
            } else if (citizenInSweden) {
                return catalog.getString("Besides Sweden.");
            } else if (citizenInAnotherCountry) {
                // Replace "previous choice" with the actual country, when the country list is not only in English.
                return catalog.getString("Besides the previous choice.");
            }
            // fall through
        }
        default:
            return null;
        }
    }

    private String getCustomDescriptionForResidenceForTaxPurposes(ApplicationField field, ApplicationForm form,
            Application application, Catalog catalog) {

        if (field == null || Strings.isNullOrEmpty(field.getName())) {
            return null;
        }
        
        switch (field.getName()) {
        case ApplicationFieldName.TAXABLE_IN_OTHER_COUNTRY:
        case ApplicationFieldName.TAXABLE_IN_YET_ANOTHER_COUNTRY: {
            
            boolean taxableInUSA = ApplicationUtils.isFirstYes(application,
                    ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_USA,
                    ApplicationFieldName.TAXABLE_IN_USA);
            
            boolean taxableInAnotherCountry = false;
            
            if (Objects.equals(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_YET_ANOTHER_COUNTRY, form.getName())) {
                // If we're populating the form asking if the user is a resident for tax purposes in _yet_ another
                // country, the user obviously already entered that he/she was a resident for tax purposes in another
                // country (excluding Sweden and USA).
                taxableInAnotherCountry = true;
            }
            
            if (taxableInUSA && taxableInAnotherCountry) {
                // Replace "previous choice" with the actual country, when the country list is not only in English.
                return catalog.getString("Besides Sweden, USA and the previous choice.");
            } else if (taxableInUSA) {
                return catalog.getString("Besides Sweden and USA.");
            } else if (taxableInAnotherCountry) {
                // Replace "previous choice" with the actual country, when the country list is not only in English.
                return catalog.getString("Besides Sweden and the previous choice.");
            } else {
                return catalog.getString("Besides Sweden.");
            }
        }
        default:
            return null;
        }
    }
}
