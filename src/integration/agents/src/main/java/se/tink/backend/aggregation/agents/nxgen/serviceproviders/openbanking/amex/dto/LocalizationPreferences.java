package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LocalizationPreferences {

    @JsonProperty("geo_country_locale")
    private String geoCountryLocale;

    @JsonProperty("date_locale")
    private String dateLocale;

    @JsonProperty("language_preference_code")
    private String languagePreferenceCode;

    @JsonProperty("home_country_locale")
    private String homeCountryLocale;

    @JsonProperty("currency_locale")
    private String currencyLocale;

    @JsonProperty("language_preference")
    private String languagePreference;

    @JsonProperty("localization_id")
    private String localizationId;

    public String getGeoCountryLocale() {
        return geoCountryLocale;
    }

    public String getDateLocale() {
        return dateLocale;
    }

    public String getLanguagePreferenceCode() {
        return languagePreferenceCode;
    }

    public String getHomeCountryLocale() {
        return homeCountryLocale;
    }

    public String getCurrencyLocale() {
        return currencyLocale;
    }

    public String getLanguagePreference() {
        return languagePreference;
    }

    public String getLocalizationId() {
        return localizationId;
    }
}
