package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.PortfolioContentEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.PortfolioRepositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc.PortfolioDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsXmlUtils;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

@Slf4j
public class PortfolioAccountsFetcher {

    private final SantanderEsApiClient apiClient;
    private final SantanderEsSessionStorage santanderEsSessionStorage;

    public PortfolioAccountsFetcher(
            SantanderEsApiClient apiClient, SantanderEsSessionStorage santanderEsSessionStorage) {
        this.apiClient = apiClient;
        this.santanderEsSessionStorage = santanderEsSessionStorage;
    }

    public Collection<InvestmentAccount> fetchAccounts() {
        LoginResponse loginResponse = santanderEsSessionStorage.getLoginResponse();
        List<PortfolioEntity> portfolioEntities = loginResponse.getPortfolios();
        String userDataXml = SantanderEsXmlUtils.parseJsonToXmlString(loginResponse.getUserData());
        HolderName holderName = loginResponse.getHolderName();
        return portfolioEntities.stream()
                .map(portfolio -> parsePortfolioAccount(portfolio, userDataXml, holderName))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<InvestmentAccount> parsePortfolioAccount(
            PortfolioEntity portfolio, String userDataXml, HolderName holderName) {
        try {
            List<PortfolioContentEntity> portfolioContent = new ArrayList<>();
            boolean moreData = true;
            boolean firstPage = true;
            PortfolioDetailsResponse portfolioResponse = null;
            PortfolioRepositionEntity paginationData = null;

            // portfolio details is paginated
            while (moreData) {
                portfolioResponse =
                        apiClient.fetchPortfolioDetails(
                                userDataXml, portfolio, firstPage, paginationData);
                firstPage = false;
                moreData = portfolioResponse.moreData();
                paginationData = portfolioResponse.getPaginationData();
                portfolioContent.addAll(portfolioResponse.getPortfolioContents());
            }

            return Optional.of(
                    portfolioResponse.toTinkInvestment(
                            apiClient, userDataXml, portfolio, portfolioContent, holderName));
        } catch (Exception e) {
            if (portfolio.getTotalValue().isZero()) {
                log.info("Ignoring http status 500 - there is no portfolio details");
            } else {
                log.error("Could not fetch investments (portfolio) details", e);
            }
        }

        return Optional.empty();
    }
}
