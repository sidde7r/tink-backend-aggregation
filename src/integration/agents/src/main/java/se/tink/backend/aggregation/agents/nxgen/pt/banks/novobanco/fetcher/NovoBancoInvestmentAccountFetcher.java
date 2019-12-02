package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.detail.InvestmentAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.investment.GetInvestmentsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.investment.GetInvestmentsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class NovoBancoInvestmentAccountFetcher implements AccountFetcher<InvestmentAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(NovoBancoInvestmentAccountFetcher.class);
    private final NovoBancoApiClient apiClient;

    public NovoBancoInvestmentAccountFetcher(NovoBancoApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        Collection<GetInvestmentsResponse> investmentResponses = apiClient.getInvestments();
        return investmentResponses.stream()
                .peek(
                        response -> {
                            if (!response.isSuccessful()) {
                                logger.warn(
                                        "ObterCarteiraFundos Response ended up with failure code: "
                                                + response.getResultCode());
                            }
                        })
                .filter(GetInvestmentsResponse::isSuccessful)
                .filter(this::hasInvestments)
                .map(InvestmentAccountMapper::mapToTinkAccount)
                .collect(Collectors.toList());
    }

    private boolean hasInvestments(GetInvestmentsResponse investmentsResponse) {
        return Optional.of(investmentsResponse)
                .map(GetInvestmentsResponse::getBody)
                .map(GetInvestmentsBodyEntity::getDossiers)
                .filter(dossiersEntities -> !dossiersEntities.isEmpty())
                .isPresent();
    }
}
