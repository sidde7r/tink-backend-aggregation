package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc.EngagementResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.UserDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

public class LaCaixaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final Logger LOG = LoggerFactory.getLogger(LaCaixaInvestmentFetcher.class);

    private final LaCaixaApiClient apiClient;

    public LaCaixaInvestmentFetcher(LaCaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {

        // we have very little information on LaCaixa investments
        // log errors, but let it pass
        try {
            UserDataResponse userDataResponse = apiClient.fetchUserData();

            EngagementResponse engagements = apiClient
                    .fetchEngagements(LaCaixaConstants.DefaultRequestParams.GLOBAL_POSITION_TYPE_P);

            // not used, only for logging
            EngagementResponse engagementsA = apiClient
                    .fetchEngagements(LaCaixaConstants.DefaultRequestParams.GLOBAL_POSITION_TYPE_A);

            Map<String, String> contractToCode = engagements.getProductCodeByContractNumber();
            List<PortfolioEntity> portfolios = getPortfolios();

            if (portfolios.size() > 0) {
                LOG.info("Investments found");
            }

            return portfolios.stream()
                    .map(portfolioEntity -> parseInvestmentAccount(userDataResponse.getHolderName(), contractToCode,
                            portfolioEntity))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Unable to fetch investments", e);
            return Collections.emptyList();
        }
    }

    private InvestmentAccount parseInvestmentAccount(HolderName holderName, Map<String, String> contractToCode,
            PortfolioEntity portfolioEntity) {
        List<Instrument> instruments = getInstruments(contractToCode, portfolioEntity);

        return portfolioEntity.toInvestmentAccount(holderName, instruments);
    }

    private List<Instrument> getInstruments(Map<String, String> contractToCode, PortfolioEntity portfolioEntity) {
        boolean fetchMore = false;
        List<Instrument> instruments = new ArrayList<>();

        instruments.addAll(
                apiClient.fetchDeposit(portfolioEntity.getId(), fetchMore).getPortfolioContents().stream()
                        .flatMap(portfolioContent -> portfolioContent.getDepositList().stream())
                        .map(depositEntity -> depositEntity.toTinkInstrument(
                                apiClient.fetchPositionDetails(portfolioEntity.getId(), depositEntity.getId()),
                                contractToCode))
                        .collect(Collectors.toList())
        );

        return instruments;
    }

    private List<PortfolioEntity> getPortfolios() {
        return Optional.ofNullable(apiClient.fetchDepositList().getPortfolioList()).orElse(Collections.emptyList());
    }
}
