package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.N26SmsAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticationPasswordController;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, IDENTITY_DATA})
public final class N26SmsAuthAgent extends N26Agent {

    @Inject
    public N26SmsAuthAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        this.client.addFilter(new TimeoutFilter());
        N26SmsAuthenticator authenticator = new N26SmsAuthenticator(sessionStorage, n26APiClient);
        return new SmsOtpAuthenticationPasswordController<>(
                catalog, supplementalInformationHelper, authenticator, 6);
    }
}
