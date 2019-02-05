package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsXmlUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.FundEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc.FundDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SantanderEsInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final Logger LOG = LoggerFactory.getLogger(SantanderEsInvestmentFetcher.class);

    private final SantanderEsApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SantanderEsInvestmentFetcher(
            SantanderEsApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        LoginResponse loginResponse = getLoginResponse();

        String userDataXml = SantanderEsXmlUtils.parseJsonToXmlString(loginResponse.getUserData());
        return Optional.ofNullable(loginResponse.getFunds()).orElse(Collections.emptyList()).stream()
                .map(fund -> parseFundAccount(fund, userDataXml))
                .collect(Collectors.toList());
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
}
