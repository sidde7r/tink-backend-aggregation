package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.parser.SdcNoTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcSmsOtpAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticationController;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

/*
 * Configure market specific client, this is NO
 */
public class SdcNoAgent extends SdcAgent {

    public SdcNoAgent(CredentialsRequest request, AgentContext context) {
        super(request, context,
                new SdcNoConfiguration(request.getProvider()),
                new SdcNoTransactionParser());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        SdcAutoAuthenticator noAutoAuthenticator = new SdcAutoAuthenticator(bankClient,
                sdcSessionStorage, agentConfiguration, credentials, sdcPersistentStorage);
        SdcSmsOtpAuthenticator noSmsOtpAuthenticator = new SdcSmsOtpAuthenticator(bankClient,
                sdcSessionStorage, agentConfiguration, credentials, sdcPersistentStorage);

        SmsOtpAuthenticationController smsOtpController = new SmsOtpAuthenticationController(catalog,
                supplementalInformationController, noSmsOtpAuthenticator);

        return new AutoAuthenticationController(request, context, smsOtpController,
                noAutoAuthenticator);
    }

    @Override
    protected SdcApiClient createApiClient(SdcConfiguration agentConfiguration) {
        return new SdcApiClient(client, agentConfiguration);
    }
}
