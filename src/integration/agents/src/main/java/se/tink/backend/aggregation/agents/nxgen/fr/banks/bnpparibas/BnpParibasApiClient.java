package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas;

import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.entites.LoginDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.entites.NumpadDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc.NumpadRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc.NumpadResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts.RibListEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts.UserOverviewDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions.AccountTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.rpc.TransactionalAccountTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.rpc.TransactionalAccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.rpc.UserOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.storage.BnpParibasPersistentStorage;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class BnpParibasApiClient {
    private final TinkHttpClient client;

    public BnpParibasApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public NumpadDataEntity getNumpadParams() {
        NumpadRequest formBody = NumpadRequest.create();

        NumpadResponse response = client.request(BnpParibasConstants.Urls.NUMPAD)
                .body(formBody, MediaType.APPLICATION_FORM_URLENCODED)
                .post(NumpadResponse.class);

        response.assertReturnCodeOk();

        return response.getData();
    }

    public LoginDataEntity login(String username, String gridId, String passwordIndices,
            BnpParibasPersistentStorage bnpParibasPersistentStorage) {
        LoginRequest formBody = LoginRequest.create(username, gridId, passwordIndices, bnpParibasPersistentStorage);

        LoginResponse response = client.request(BnpParibasConstants.Urls.LOGIN)
                .body(formBody, MediaType.APPLICATION_FORM_URLENCODED)
                .post(LoginResponse.class);

        response.assertReturnCodeOk();

        return response.getData();
    }

    public void keepAlive() {
        BaseResponse response = client.request(BnpParibasConstants.Urls.KEEP_ALIVE)
                .get(BaseResponse.class);

        response.assertReturnCodeOk();
    }

    public UserOverviewDataEntity getUserOverview() {
        UserOverviewResponse response = client.request(BnpParibasConstants.Urls.USER_OVERVIEW)
                .queryParam(BnpParibasConstants.QueryParams.MODE_APPEL,
                        BnpParibasConstants.QueryParams.MODE_APPEL_0)
                .get(UserOverviewResponse.class);

        response.assertReturnCodeOk();

        return response.getData();
    }

    public RibListEntity getAccountDetails(String ibanKey) {
        AccountDetailsResponse response = client.request(BnpParibasConstants.Urls.ACCOUNT_DETAILS)
                .queryParam(BnpParibasConstants.QueryParams.SERVICE,
                        BnpParibasConstants.QueryParams.SERVICE_RIB)
                .queryParam(BnpParibasConstants.QueryParams.ACCOUNT_NUMBER, ibanKey)
                .get(AccountDetailsResponse.class);

        response.getSmc().assertReturnCodeOk();

        return response.getSmc().getData();
    }

    public AccountTransactionsEntity getTransactionalAccountTransactions(Date fromDate, Date toDate,
            String ibanKey) {

        TransactionalAccountTransactionsRequest request = TransactionalAccountTransactionsRequest.create(
                fromDate, toDate, ibanKey);

        TransactionalAccountTransactionsResponse response =
                client.request(BnpParibasConstants.Urls.TRANSACTIONAL_ACCOUNT_TRANSACTIONS)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .post(TransactionalAccountTransactionsResponse.class, request);

        response.assertReturnCodeOk();

        return response.getData().transactionsInfo().getAccountTransactions();
    }
}
