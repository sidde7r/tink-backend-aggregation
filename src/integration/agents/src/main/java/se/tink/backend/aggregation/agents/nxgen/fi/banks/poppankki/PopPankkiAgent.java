package se.tink.backend.aggregation.agents.nxgen.fi.banks.poppankki;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;

import com.google.inject.Inject;
import java.util.Collections;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkV2Configuration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, LOANS})
public final class PopPankkiAgent extends SamlinkAgent {

    @Inject
    public PopPankkiAgent(AgentComponentProvider componentProvider) {
        super(
                componentProvider,
                new SamlinkV2Configuration(
                        PopPankkiConstants.Url.BASE, PopPankkiConstants.Header.CLIENT_APP_VALUE));
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return new FetchAccountsResponse(Collections.emptyList());
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }
}
