package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
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

    private static final Pattern PATTERN = Pattern.compile("\\{bankcode}");
    private static final Matcher NETTBANK_MATCHER =
            PATTERN.matcher(Authentication.IFRAME_BANKID_LOGIN_URL);
    private static final Matcher PORTALBANK_MATCHER =
            PATTERN.matcher(Authentication.PORTALBANK_LOGIN_URL);

    public SdcNoConfiguration(Provider provider) {
        this.bankCode = getBankCode(provider.getPayload());
        this.authenticationType = ProviderMapping.getAuthenticationTypeByBankCode(bankCode);
    }

    public String getBankCode(String payload) {
        Preconditions.checkNotNull(
                Strings.emptyToNull(payload),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Bank Code"));
        return payload;
    }

    public String getBaseUrl() {
        if ((AuthenticationType.NETTBANK).equals(authenticationType)) {
            return NETTBANK_MATCHER.replaceAll(bankCode);
        }
        if ((AuthenticationType.PORTAL).equals(authenticationType)) {
            return PORTALBANK_MATCHER.replaceAll(bankCode);
        }
        throw new IllegalArgumentException(
                String.format("Not found base url for %s", authenticationType));
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }
}
