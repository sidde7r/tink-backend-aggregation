package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsXmlUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.FundEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.PortfolioContentEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.PortfolioRepositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc.FundDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc.PortfolioDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SantanderEsInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final AggregationLogger LOG = new AggregationLogger(SantanderEsInvestmentFetcher.class);

    private final SantanderEsApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SantanderEsInvestmentFetcher(
            SantanderEsApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        List<InvestmentAccount> accounts = new ArrayList<>();

        LoginResponse loginResponse = getLoginResponse();

        String userDataXml = SantanderEsXmlUtils.parseJsonToXmlString(loginResponse.getUserData());

        // stocks
        Optional.ofNullable(loginResponse.getPortfolios()).orElse(Collections.emptyList()).stream()
                .map(portfolio -> parsePortfolioAccount(portfolio, userDataXml, loginResponse.getHolderName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(accounts::add);

        // funds
        Optional.ofNullable(loginResponse.getFunds()).orElse(Collections.emptyList()).stream()
                .map(fund -> parseFundAccount(fund, userDataXml))
                .forEach(accounts::add);

        return accounts;
    }

    private LoginResponse getLoginResponse() {
        String loginResponseString = sessionStorage.get(SantanderEsConstants.Storage.LOGIN_RESPONSE, String.class)
                .orElseThrow(() -> new IllegalStateException(
                        SantanderEsConstants.LogMessages.LOGIN_RESPONSE_NOT_FOUND));

        return SantanderEsXmlUtils.parseXmlStringToJson(loginResponseString, LoginResponse.class);
    }

    private InvestmentAccount parseFundAccount(FundEntity fundEntity, String userDataXml) {
        FundDetailsResponse fundDetailsResponse = apiClient.fetchFundDetails(userDataXml, fundEntity);
        return fundEntity.toInvestmentAccount(fundDetailsResponse);
    }

    private Optional<InvestmentAccount> parsePortfolioAccount(PortfolioEntity portfolio, String userDataXml,
            HolderName holderName) {
        try {
            List<PortfolioContentEntity> portfolioContent = new ArrayList<>();
            boolean moreData = true;
            boolean firstPage = true;
            PortfolioDetailsResponse portfolioResponse = null;
            PortfolioRepositionEntity paginationData = null;

            // portfolio details is paginated
            while (moreData) {
                portfolioResponse = apiClient.fetchPortfolioDetails(userDataXml, portfolio, firstPage, paginationData);
                firstPage = false;
                moreData = portfolioResponse.moreData();
                paginationData = portfolioResponse.getPaginationData();
                portfolioContent.addAll(portfolioResponse.getPortfolioContents());
            }

            return Optional.of(portfolioResponse
                    .toTinkInvestment(apiClient, userDataXml, portfolio, portfolioContent, holderName));
        } catch (Exception e) {
            // if amount is zero it looks like we cannot ask for details, we get a 500
            if (!portfolio.getTotalValue().getTinkAmount().isZero()) {
                LOG.info("Could not fetch investments " + SantanderEsConstants.Tags.INVESTMENT_ACCOUNT, e);
            }
        }

        return Optional.empty();
    }
}
