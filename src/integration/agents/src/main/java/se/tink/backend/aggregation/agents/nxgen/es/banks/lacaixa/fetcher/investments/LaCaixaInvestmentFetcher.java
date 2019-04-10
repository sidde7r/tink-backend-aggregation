package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc.EngagementResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc.FundsListResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.rpc.UserDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.rpc.LaCaixaErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class LaCaixaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final Logger LOG = LoggerFactory.getLogger(LaCaixaInvestmentFetcher.class);

    private final LaCaixaApiClient apiClient;

    public LaCaixaInvestmentFetcher(LaCaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        UserDataResponse userDataResponse = apiClient.fetchIdentityData();
        EngagementResponse engagements = apiClient
                .fetchEngagements(LaCaixaConstants.DefaultRequestParams.GLOBAL_POSITION_TYPE_P);

        List<InvestmentAccount> investments = new ArrayList<>();
        investments.addAll(getStockInvestments(userDataResponse, engagements));
        investments.addAll(getFundInvestments(userDataResponse, engagements));

        if (investments.size() > 0) {
            LOG.info("Investments found");
        }

        return investments;
    }

    private List<InvestmentAccount> getFundInvestments(UserDataResponse userDataResponse,
            EngagementResponse engagements) {

        List<InvestmentAccount> fundInvestments = new ArrayList<>();
        boolean moreData = false;
        do {
            FundsListResponse fundsListResponse = apiClient.fetchFundList(moreData);
            moreData = fundsListResponse.isMoreData();
            fundInvestments.addAll(fundsListResponse
                    .getTinkInvestments(apiClient, userDataResponse.getHolderName(), engagements));
        } while (moreData);

        return fundInvestments;
    }

    private List<InvestmentAccount> getStockInvestments(UserDataResponse userDataResponse,
            EngagementResponse engagements) {
        try {
            Map<String, String> contractToCode = engagements.getProductCodeByContractNumber();
            List<PortfolioEntity> portfolios = getStockPortfolios();

            return portfolios.stream()
                    .map(portfolioEntity -> parseStockInvestmentAccount(userDataResponse.getHolderName(),
                            contractToCode,
                            portfolioEntity))
                    .collect(Collectors.toList());
        } catch (HttpResponseException hre) {
            HttpResponse response = hre.getResponse();
            if (isNoSecurities(response)) {
                return Collections.emptyList();
            }
            LOG.error("Unable to fetch stock investments", hre);
            throw hre;
        }
    }

    private boolean isNoSecurities(HttpResponse response) {
        if (response.getStatus() == HttpStatus.SC_CONFLICT) {
            LaCaixaErrorResponse error = response.getBody(LaCaixaErrorResponse.class);
            return error.isNoSecurities();
        }
        return false;
    }

    private InvestmentAccount parseStockInvestmentAccount(HolderName holderName, Map<String, String> contractToCode,
            PortfolioEntity portfolioEntity) {
        List<Instrument> instruments = getStockInstruments(contractToCode, portfolioEntity);

        return portfolioEntity.toInvestmentAccount(holderName, instruments);
    }

    private List<Instrument> getStockInstruments(Map<String, String> contractToCode, PortfolioEntity portfolioEntity) {
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

    private List<PortfolioEntity> getStockPortfolios() {
        return Optional.ofNullable(apiClient.fetchDepositList().getPortfolioList()).orElse(Collections.emptyList());
    }
}
