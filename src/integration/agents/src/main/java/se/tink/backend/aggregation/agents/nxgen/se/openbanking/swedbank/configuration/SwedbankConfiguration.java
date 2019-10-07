package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class SwedbankConfiguration implements ClientConfiguration {

    @Secret private String clientId;
    @SensitiveSecret private String clientSecret;
    @Secret private String redirectUrl;
    private String eidasQwac;
    private String psuIpAddress;
    @Secret private boolean bypassTransactionConsent;
    @Secret private int monthsToFetch;

    public String getClientId() {
        Preconditions.checkNotNull(
                com.google.common.base.Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                com.google.common.base.Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return redirectUrl;
    }

    public String getEidasQwac() {
        return eidasQwac;
    }

    public String getPsuIpAddress() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psuIpAddress),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "PSU IP Address"));

        return psuIpAddress;
    }

    public boolean getBypassConsent() {
        return bypassTransactionConsent;
    }

    public int getMonthsToFetch() {
        return (monthsToFetch > SwedbankConstants.TimeValues.MONTHS_TO_FETCH_MAX
                        || monthsToFetch < SwedbankConstants.TimeValues.MONTHS_TO_FETCH_MIN)
                ? SwedbankConstants.TimeValues.MONTHS_TO_FETCH_MAX
                : monthsToFetch;
    }
}
