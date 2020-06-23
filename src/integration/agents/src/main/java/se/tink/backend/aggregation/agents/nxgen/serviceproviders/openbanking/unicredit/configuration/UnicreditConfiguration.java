package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class UnicreditConfiguration implements ClientConfiguration {

    @Secret private String baseUrl;

    private String clientKeyStorePath;

    private String clientKeyStorePassword;

    @Secret private String eidasQwac;

    @Secret private String psuIdType;

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base URL"));

        return baseUrl;
    }

    public String getClientKeyStorePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Key Store Path"));

        return clientKeyStorePath;
    }

    public String getClientKeyStorePassword() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePassword),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Key Store Password"));

        return clientKeyStorePassword;
    }

    public String getEidasQwac() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(eidasQwac),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "eIDAS QWAC"));

        return eidasQwac;
    }

    public String getPsuIdType() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psuIdType),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "PSU Id Type"));

        return psuIdType;
    }
}
