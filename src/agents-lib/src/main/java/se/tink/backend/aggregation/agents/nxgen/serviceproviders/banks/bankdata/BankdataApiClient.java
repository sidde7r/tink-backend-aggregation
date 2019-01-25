package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.TimeTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataDepositEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataPoolAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.AssetDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.AssetDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.DepositsContentListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetDepositsContentListRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetDepositsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.PoolAccountsResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class BankdataApiClient {

    private final TinkHttpClient client;
    private final String bankdataBankNumber;

    public BankdataApiClient(TinkHttpClient client, Provider provider) {

        this.client = client;
        this.bankdataBankNumber = provider.getPayload();
    }

    public TimeTokenResponse getTimeToken() {
        return getRequest(BankdataConstants.Url.LOGIN_TIME_TOKEN)
                .get(TimeTokenResponse.class);
    }

    public LoginResponse pinLogin(LoginRequest request) {
        return postRequest(BankdataConstants.Url.LOGIN)
                .post(LoginResponse.class, request);
    }

    public GetAccountsResponse getAccounts() {
        return getRequest(BankdataConstants.Url.ACCOUNTS)
                .get(GetAccountsResponse.class);
    }

    public GetTransactionsResponse getTransactions(GetTransactionsRequest getTransactionsRequest) {
        return postRequest(BankdataConstants.Url.PFM_TRANSACTIONS)
                .post(GetTransactionsResponse.class, getTransactionsRequest);
    }

    public GetTransactionsResponse getFutureTransactions(GetTransactionsRequest getTransactionsRequest) {
        return postRequest(BankdataConstants.Url.PFM_TRANSACTIONS_FUTURE)
                .post(GetTransactionsResponse.class, getTransactionsRequest);
    }

    public List<BankdataDepositEntity> fetchDeposits() {
        return getRequest(BankdataConstants.Url.DEPOSITS)
                .get(GetDepositsResponse.class).getDeposits();
    }

    public List<BankdataPoolAccountEntity> fetchPoolAccounts() {
        return getRequest(BankdataConstants.Url.INVESTMENT_POOL_ACCOUNTS)
                .get(PoolAccountsResponse.class).getPoolAccounts();
    }

    public DepositsContentListResponse fetchDepositContents(String regNo, String depositNo) {
        GetDepositsContentListRequest request = new GetDepositsContentListRequest()
                .setDepositNo(depositNo)
                .setRegNo(regNo);
        return postRequest(BankdataConstants.Url.DEPOSITS_CONTENT_LIST)
                .post(DepositsContentListResponse.class, request);
    }

    public AssetDetailsResponse fetchAssetDetails(int assetType, String depositRegNumber, String depositNumber,
            String securityId) {
        AssetDetailsRequest request = new AssetDetailsRequest()
                .setAssetType(String.valueOf(assetType))
                .setDepositRegNo(depositRegNumber)
                .setDepositNo(depositNumber)
                .setSecurityId(securityId);

        return postRequest(BankdataConstants.Url.ASSET_DETAILS)
                .post(AssetDetailsResponse.class, request);

    }

    private RequestBuilder postRequest(URL url) {
        return getRequest(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    private RequestBuilder getRequest(URL url) {
        return client.request(url)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(BankdataConstants.Headers.X_VERSION, BankdataConstants.Headers.X_VERSION_VALUE)
                .header(BankdataConstants.Headers.X_APPID, BankdataConstants.Headers.X_APPID_VALUE)
                .header(BankdataConstants.Headers.X_BANK_NO, bankdataBankNumber);
    }
}
