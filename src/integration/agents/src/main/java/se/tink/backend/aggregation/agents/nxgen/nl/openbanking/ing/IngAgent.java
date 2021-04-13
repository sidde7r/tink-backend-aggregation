package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.ing;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.ing.fetcher.rpc.IngNLTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.BaseFetchTransactionsResponse;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS})
public final class IngAgent extends IngBaseAgent {

    public static final Pattern HOLDER_NAME_SPLITTER =
            Pattern.compile(" en/of |, ", Pattern.CASE_INSENSITIVE);

    @Inject
    public IngAgent(AgentComponentProvider agentComponentProvider, QsealcSigner qsealcSigner) {
        super(agentComponentProvider, qsealcSigner);
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
