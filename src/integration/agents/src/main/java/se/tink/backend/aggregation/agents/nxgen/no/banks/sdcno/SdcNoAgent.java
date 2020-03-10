package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.SdcNoBankIdIFrameSSAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.parser.SdcNoTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.BankIdIframeSSAuthenticationController;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.selenium.WebDriverHelper;

/*
 * Configure market specific client, this is NO
 */
public class SdcNoAgent extends SdcAgent {
    private static final Pattern PATTERN = Pattern.compile("\\{bankcode}");
    private static final Matcher NETTBANK_MATCHER =
            PATTERN.matcher(Authentication.IFRAME_BANKID_LOGIN_URL);

    public SdcNoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                new SdcNoConfiguration(request.getProvider()),
                new SdcNoTransactionParser());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BankIdIframeSSAuthenticationController controller =
                new BankIdIframeSSAuthenticationController(getLoginUrl(), new WebDriverHelper());
        return new SdcNoBankIdIFrameSSAuthenticator(controller);
    }

    @Override
    protected SdcApiClient createApiClient(SdcConfiguration agentConfiguration) {
        return new SdcApiClient(client, agentConfiguration);
    }

    private String getLoginUrl() {
        String bankCode = agentConfiguration.getBankCode();
        return NETTBANK_MATCHER.replaceAll(bankCode);
    }
}
