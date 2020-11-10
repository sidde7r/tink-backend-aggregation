package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbExceptionsHelper;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.entities.MyFundEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.entities.PensionDataEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.rpc.FetchFundsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.rpc.FetchPensionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.oauth.rpc.InitMyWealthResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
@Slf4j
public class DnbInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final DnbApiClient apiClient;
    private final Credentials credentials;
    private String oauthToken = "";
    private String oauthSecret = "";

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        try {
            Collection<InvestmentAccount> accounts = Lists.newArrayList();

            // OAuth authentication Begin
            String requestToken = this.apiClient.oauthGetRequestToken();
            this.extractOAuthTokensFromResponse(requestToken);

            String verifierService = this.apiClient.oauthVerifierService(this.oauthToken);

            String oauthVerifier = "";
            if (verifierService != null
                    && verifierService.length() != 0
                    && verifierService.length() < 30) {
                oauthVerifier = verifierService;
            }

            String accessToken =
                    this.apiClient.oauthGetAccessToken(
                            this.oauthToken, oauthVerifier, this.oauthSecret);
            this.extractOAuthTokensFromResponse(accessToken);
            // OAuth authentication End

            InitMyWealthResponse initMyWealthResponse =
                    this.apiClient.initMyWealth(this.oauthToken, this.oauthSecret);
            if (initMyWealthResponse.getSuccess()) {

                // Pension
                FetchPensionResponse pensionResponse =
                        this.apiClient.fetchPension(this.oauthToken, this.oauthSecret);
                PensionDataEntity pensionData = pensionResponse.getData();
                if (pensionData != null && pensionData.getIpsHoldingsCount() > 0) {
                    accounts.add(pensionData.toTinkAccount());
                    if (pensionData.getAccountNumbers().size() > 1) {
                        log.warn("Dnb Norway, Pension with more than 1 accounts");
                    }
                }

                // Funds
                FetchFundsResponse fundResponse =
                        this.apiClient.fetchFunds(this.oauthToken, this.oauthSecret);

                List<MyFundEntity> myFunds = fundResponse.getMyFunds();
                if (myFunds != null) {
                    myFunds.forEach(
                            myFundEntity ->
                                    this.apiClient
                                            .getIsinFromFundDetailPdf(
                                                    this.apiClient
                                                            .fetchFundDetails(
                                                                    this.oauthToken,
                                                                    this.oauthSecret,
                                                                    myFundEntity.getProductSystem(),
                                                                    myFundEntity.getProductId())
                                                            .getProductDetails()
                                                            .getProductSheetURI())
                                            .ifPresent(myFundEntity::setIsin));

                    accounts.addAll(fundResponse.getInvestmentAccounts());
                }
            }

            return accounts;
        } catch (HttpResponseException e) {
            if (DnbExceptionsHelper.customerDoesNotHaveAccessToResource(e)) {
                return Collections.emptyList();
            }
            throw e;
        }
    }

    // xiacheng NOTE: response is url encoded like
    // "oauth_token=xxx&oauth_token_secret=yyy&oauth_callback_confirmed=true"
    private void extractOAuthTokensFromResponse(String response) {
        String[] tokens = response.split("&");
        for (String token : tokens) {
            String[] tokenPair = token.split("=");
            if (Objects.equals(tokenPair[0], DnbConstants.Header.OAUTH_TOKEN_KEY)) {
                this.oauthToken = tokenPair[1];
                credentials.setSensitivePayload(Storage.OAUTH_TOKEN, this.oauthToken);
                continue;
            }
            if (Objects.equals(tokenPair[0], DnbConstants.OAuth.OAUTH_TOKEN_SECRET_KEY)) {
                this.oauthSecret = tokenPair[1];
                credentials.setSensitivePayload(Storage.OAUTH_SECRET, this.oauthSecret);
            }
        }
    }
}
