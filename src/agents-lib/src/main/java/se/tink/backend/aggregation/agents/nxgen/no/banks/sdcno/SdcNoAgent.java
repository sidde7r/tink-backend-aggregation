package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.parser.SdcNoTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcPinAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcSmsOtpAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticationPasswordController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

/*
 * Configure market specific client, this is NO
 */
public class SdcNoAgent extends SdcAgent {
    private static Logger LOG = LoggerFactory.getLogger(SdcNoAgent.class);

    public SdcNoAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair,
                new SdcNoConfiguration(request.getProvider()),
                new SdcNoTransactionParser());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        if (SdcNoConstants.Authentication.BANKS_WITH_PIN_AUTHENTICATION.contains(agentConfiguration.getBankCode())) {
            return constructPinAuthenticator();
        } else {
            return constructSmsAuthenticator();
        }
    }

    private Authenticator constructSmsAuthenticator() {
        LOG.info("SDC bank using SMS authentication");
        SdcAutoAuthenticator noAutoAuthenticator = new SdcAutoAuthenticator(bankClient,
                sdcSessionStorage, agentConfiguration, credentials, sdcPersistentStorage);
        SdcSmsOtpAuthenticator noSmsOtpAuthenticator = new SdcSmsOtpAuthenticator(bankClient,
                sdcSessionStorage, agentConfiguration, credentials, sdcPersistentStorage);

        SmsOtpAuthenticationPasswordController smsOtpController = new SmsOtpAuthenticationPasswordController(catalog,
                supplementalInformationHelper, noSmsOtpAuthenticator);

        return new AutoAuthenticationController(request, context, smsOtpController,
                noAutoAuthenticator);
    }

    private Authenticator constructPinAuthenticator() {
        LOG.info("SDC bank using pin authentication");

        SdcPinAuthenticator dkAuthenticator = new SdcPinAuthenticator(bankClient,
                sdcSessionStorage, agentConfiguration);
        return new PasswordAuthenticationController(dkAuthenticator);
    }

    @Override
    protected SdcApiClient createApiClient(SdcConfiguration agentConfiguration) {
        return new SdcApiClient(client, agentConfiguration);
    }
}
