package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.ing;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.ing.fetcher.rpc.IngNLTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.BaseFetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;

@AgentCapabilities({CHECKING_ACCOUNTS, TRANSFERS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.PIS_FUTURE_DATE,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER
        })
public final class IngAgent extends IngBaseAgent {

    public static final Pattern HOLDER_NAME_SPLITTER =
            Pattern.compile(" en/of |, ", Pattern.CASE_INSENSITIVE);

    @Inject
    public IngAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }

    @Override
    public LocalDate earliestTransactionHistoryDate() {
        return LocalDate.now().minusYears(2);
    }

    @Override
    public Class<? extends BaseFetchTransactionsResponse> getTransactionsResponseClass() {
        return IngNLTransactionResponse.class;
    }

    @Override
    public List<Party> convertHolderNamesToParties(String holderNames) {
        return HOLDER_NAME_SPLITTER
                .splitAsStream(holderNames)
                .map(name -> new Party(name, Party.Role.HOLDER))
                .collect(Collectors.toList());
    }
}
