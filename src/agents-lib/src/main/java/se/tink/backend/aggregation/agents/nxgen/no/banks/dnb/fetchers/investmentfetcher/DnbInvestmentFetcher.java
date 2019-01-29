package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.entities.MyFundEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.entities.PensionDataEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.rpc.FetchFundsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.rpc.FetchPensionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.oauth.rpc.InitMyWealthResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.agents.rpc.Credentials;

public class DnbInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final AggregationLogger log = new AggregationLogger(DnbInvestmentFetcher.class);

    private final DnbApiClient apiClient;
    private final Credentials credentials;
    private String oauth_token = "";
    private String oauth_secret = "";

    public DnbInvestmentFetcher(DnbApiClient apiClient, Credentials credentials) {
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        Collection<InvestmentAccount> accounts = Lists.newArrayList();

        // OAuth authentication Begin
        String requestToken = this.apiClient.oauthGetRequestToken();
        this.extractOAuthTokensFromResponse(requestToken);

        String verifierService = this.apiClient.oauthVerifierService(this.oauth_token);

        String oauth_verifier = "";
        if (verifierService != null && verifierService.length() != 0 && verifierService.length() < 30) {
            oauth_verifier = verifierService;
        }

        String accessToken = this.apiClient.oauthGetAccessToken(this.oauth_token, oauth_verifier, this.oauth_secret);
        this.extractOAuthTokensFromResponse(accessToken);
        // OAuth authentication End

        InitMyWealthResponse initMyWealthResponse = this.apiClient.initMyWealth(this.oauth_token, this.oauth_secret);
        if (initMyWealthResponse.getSuccess()) {

            // Pension
            FetchPensionResponse pensionResponse = this.apiClient.fetchPension(this.oauth_token, this.oauth_secret);
            PensionDataEntity pensionData = pensionResponse.getData();
            if (pensionData != null && pensionData.getIpsHoldingsCount() > 0) {
                accounts.add(pensionData.toTinkAccount());
                if (pensionData.getAccountNumbers().size() > 1) {
                    log.warn("Dnb Norway, Pension with more than 1 accounts");
                }
            }

            // Funds
            FetchFundsResponse fundResponse = this.apiClient.fetchFunds(this.oauth_token, this.oauth_secret);

            List<MyFundEntity> myFunds = fundResponse.getMyFunds();
            if (myFunds != null) {
                myFunds.forEach(myFundEntity -> {
                    this.apiClient.getIsinFromFundDetailPdf(this.apiClient
                            .fetchFundDetails(this.oauth_token, this.oauth_secret, myFundEntity.getProductSystem(),
                                    myFundEntity.getProductId())
                            .getProductDetails().getProductSheetURI()).
                            ifPresent(myFundEntity::setIsin);
                });

                accounts.addAll(fundResponse.getInvestmentAccounts());
            }
        }

        return accounts;
    }

    // xiacheng NOTE: response is url encoded like "oauth_token=xxx&oauth_token_secret=yyy&oauth_callback_confirmed=true"
    private void extractOAuthTokensFromResponse(String response) {
        String[] tokens = response.split("&");
        for (String token : tokens) {
            String[] tokenPair = token.split("=");
            if (Objects.equals(tokenPair[0], DnbConstants.Header.OAUTH_TOKEN_KEY)) {
                this.oauth_token = tokenPair[1];
                continue;
            }
            if (Objects.equals(tokenPair[0], DnbConstants.OAuth.OAUTH_TOKEN_SECRET_KEY)) {
                this.oauth_secret = tokenPair[1];
            }
        }
    }
}
