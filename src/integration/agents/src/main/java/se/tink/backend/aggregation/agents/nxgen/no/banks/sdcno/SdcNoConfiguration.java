package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankmappers.AuthenticationType;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankmappers.ProviderMapping;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public class SdcNoConfiguration implements ClientConfiguration {
    private String bankCode;
    private AuthenticationType authenticationType;
    private Map<AuthenticationType, String> baseUrlMap = new HashMap<>();

    private static final Pattern PATTERN = Pattern.compile("\\{bankcode}");
    private static final Matcher NETTBANK_MATCHER =
            PATTERN.matcher(Authentication.NETTBANK_BANKID_LOGIN_URL);
    private static final Matcher PORTALBANK_MATCHER =
            PATTERN.matcher(Authentication.PORTALBANK_LOGIN_URL);

    public SdcNoConfiguration(Provider provider) {
        this.bankCode = getBankCode(provider.getPayload());
        this.authenticationType = ProviderMapping.getAuthenticationTypeByBankCode(bankCode);
        generateBaseUrlMap();
    }

    private void generateBaseUrlMap() {
        baseUrlMap.put(AuthenticationType.NETTBANK, NETTBANK_MATCHER.replaceAll(bankCode));
        baseUrlMap.put(AuthenticationType.PORTAL, PORTALBANK_MATCHER.replaceAll(bankCode));
        baseUrlMap.put(AuthenticationType.EIKA, Authentication.EIKA_LOGIN_URL);
    }

    public String getBankCode(String payload) {
        Preconditions.checkNotNull(
                Strings.emptyToNull(payload),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Bank Code"));
        return payload;
    }

    public String getBaseUrl() {
        return Optional.ofNullable(baseUrlMap.get(authenticationType))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Not found base url for " + authenticationType));
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }
}
