package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.FundEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc.FundDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsXmlUtils;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class FundsAccountsFetcher {

    private final SantanderEsApiClient apiClient;
    private final SantanderEsSessionStorage santanderEsSessionStorage;

    public FundsAccountsFetcher(
            SantanderEsApiClient apiClient, SantanderEsSessionStorage santanderEsSessionStorage) {
        this.apiClient = apiClient;
        this.santanderEsSessionStorage = santanderEsSessionStorage;
    }

    public Collection<InvestmentAccount> fetchAccounts() {
        LoginResponse loginResponse = santanderEsSessionStorage.getLoginResponse();
        List<FundEntity> fundEntities = loginResponse.getFunds();
        String userDataXml = SantanderEsXmlUtils.parseJsonToXmlString(loginResponse.getUserData());
        return fundEntities.stream()
                .map(fundEntity -> parseFundAccount(fundEntity, userDataXml))
                .collect(Collectors.toList());
    }

    private InvestmentAccount parseFundAccount(FundEntity fundEntity, String userDataXml) {
        FundDetailsResponse fundDetailsResponse =
                apiClient.fetchFundDetails(userDataXml, fundEntity);
        return fundEntity.toInvestmentAccount(fundDetailsResponse);
    }
}
