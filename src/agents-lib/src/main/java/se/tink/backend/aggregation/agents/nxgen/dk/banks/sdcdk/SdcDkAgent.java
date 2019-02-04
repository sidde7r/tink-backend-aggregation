package se.tink.backend.aggregation.agents.nxgen.dk.banks.sdcdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.sdcdk.parser.SdcDkTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcPinAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcSmsOtpAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticationPasswordController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

/*
 * Configure market specific client, this is DK
 */
public class SdcDkAgent extends SdcAgent {
    private static Logger LOG = LoggerFactory.getLogger(SdcDkAgent.class);

    public SdcDkAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair,
                new SdcDkConfiguration(request.getProvider()),
                new SdcDkTransactionParser());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        if (SdcDkConstants.Authentication.BANKS_WITH_SMS_AUTHENTICATION.contains(agentConfiguration.getBankCode())) {
            return constructSmsAuthenticator();
        } else {
            return constructPinAuthenticator();
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
