package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.agent.sdk.operation.Provider;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConstants.EikaBankPortal;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConstants.NettBankPortal;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConstants.PortalBank;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.libraries.payloadparser.PayloadParser;

public class SdcNoConfiguration implements ClientConfiguration {
    private final String bankCode;
    private final AuthenticationType authenticationType;
    private final String domain;
    private Map<AuthenticationType, String> baseUrlMap = new HashMap<>();
    private Map<AuthenticationType, String> loginUrlMap = new HashMap<>();

    public SdcNoConfiguration(Provider provider) {
        this.bankCode = getBankCode(provider.getPayload());
        ProviderMapping providerMapping =
                ProviderMapping.getProviderMappingTypeByBankCode(bankCode);
        this.authenticationType = providerMapping.getAuthenticationType();
        this.domain = providerMapping.getDomain();
        generateBaseUrlMap();
        generateLoginUrlMap();
    }

    private void generateBaseUrlMap() {
        baseUrlMap.put(AuthenticationType.NETTBANK, NettBankPortal.BASE_URL);
        baseUrlMap.put(AuthenticationType.PORTAL, PortalBank.BASE_URL);
        baseUrlMap.put(AuthenticationType.EIKA, EikaBankPortal.BASE_URL);
    }

    private void generateLoginUrlMap() {
        loginUrlMap.put(
                AuthenticationType.NETTBANK, NettBankPortal.LOGIN_MATCHER.replaceAll(bankCode));
        loginUrlMap.put(AuthenticationType.PORTAL, PortalBank.LOGIN_MATCHER.replaceAll(bankCode));
        loginUrlMap.put(AuthenticationType.EIKA, EikaBankPortal.LOGIN_URL);
    }

    private String getBankCode(String payload) {
        Preconditions.checkNotNull(
                Strings.emptyToNull(payload),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Bank Code"));
        SdcPayload sdcPayload = PayloadParser.parse(payload, SdcPayload.class);
        return sdcPayload.getBankcode();
    }

    public String getLoginUrl() {
        return Optional.ofNullable(loginUrlMap.get(authenticationType))
                .orElseThrow(
                        () ->
                                new SdcConfigurationException(
                                        authenticationType + " login url not found."));
    }

    public String getBasePageUrl() {
        return Optional.ofNullable(baseUrlMap.get(authenticationType))
                .map(s -> s + bankCode + "/")
                .orElseThrow(
                        () ->
                                new SdcConfigurationException(
                                        authenticationType + " base page url not found."));
    }

    public String getBaseApiUrl() {
        return Optional.ofNullable(baseUrlMap.get(authenticationType))
                .orElseThrow(
                        () ->
                                new SdcConfigurationException(
                                        authenticationType + " base api url not found."));
    }

    public String getIndividualBaseURL() {
        return Optional.ofNullable(SdcNoConstants.DOMAIN_MATCHER.replaceAll(domain))
                .orElseThrow(
                        () ->
                                new SdcConfigurationException(
                                        bankCode + " individual base url not found."));
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }
}
