package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.entites.LoginDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.entites.NumpadDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc.NumpadRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc.NumpadResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts.TransactionAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions.InfoUdcEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.rpc.AccountIbanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.rpc.IbanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.rpc.TransactionalAccountTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.rpc.TransactionalAccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.storage.BnpParibasPersistentStorage;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class BnpParibasApiClient {
    private final TinkHttpClient client;
    private static final Logger log = LoggerFactory.getLogger(BnpParibasApiClient.class);
    private static final int MAX_TRIES = 3;

    public BnpParibasApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public NumpadDataEntity getNumpadParams() {
        NumpadRequest formBody = NumpadRequest.create();

        NumpadResponse response =
                client.request(BnpParibasConstants.Urls.NUMPAD)
                        .body(formBody, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(NumpadResponse.class);

        response.assertReturnCodeOk();
        return response.getData();
    }

    public LoginDataEntity login(
            String username,
            String gridId,
            String passwordIndices,
            BnpParibasPersistentStorage bnpParibasPersistentStorage)
            throws LoginException {
        LoginRequest formBody =
                LoginRequest.create(username, gridId, passwordIndices, bnpParibasPersistentStorage);

        LoginResponse response =
                client.request(BnpParibasConstants.Urls.LOGIN)
                        .body(formBody, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(LoginResponse.class);

        if (!Strings.isNullOrEmpty(response.getErrorCode())
                && response.getErrorCode()
                        .equals(BnpParibasConstants.Errors.INCORRECT_CREDENTIALS)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        response.assertReturnCodeOk();
        return response.getData();
    }

    public void keepAlive() {
        BaseResponse response =
                client.request(BnpParibasConstants.Urls.KEEP_ALIVE).get(BaseResponse.class);

        response.assertReturnCodeOk();
    }

    public Collection<Transaction> getTransactionalAccountTransactions(
            Date fromDate, Date toDate, String ibanKey) {
        TransactionalAccountTransactionsRequest request =
                TransactionalAccountTransactionsRequest.create(fromDate, toDate, ibanKey);

        TransactionalAccountTransactionsResponse response = null;
        int tries = 0;
        for (tries = 0; tries < MAX_TRIES; tries++) {
            try {
                response =
                        client.request(BnpParibasConstants.Urls.TRANSACTIONAL_ACCOUNT_TRANSACTIONS)
                                .type(MediaType.APPLICATION_JSON_TYPE)
                                .post(TransactionalAccountTransactionsResponse.class, request);
                break;
            } catch (javax.ws.rs.WebApplicationException wae) {
                log.error(
                        String.format(
                                "[Try %d]: WebApplicationException -- getTransactionalAccountTransactions",
                                tries),
                        wae);
            } catch (Exception e) {
                log.error(
                        String.format(
                                "[Try %d]: Exception -- getTransactionalAccountTransactions",
                                tries),
                        e);
            }
        }
        if (tries == MAX_TRIES) {
            log.info(
                    "getTransactionalAccountTransactions -- Max tries reached, returning empty list of transactions.");
            return Collections.EMPTY_LIST;
        }

        response.assertReturnCodeOk();

        return response.getData().transactionsInfo().getAccountTransactions().toTinkTransactions();
    }

    public InfoUdcEntity getAccounts() {
        AccountsResponse response =
                client.request(BnpParibasConstants.Urls.LIST_ACCOUNTS).get(AccountsResponse.class);

        response.assertReturnCodeOk();

        return response.getData().getInfoUdc();
    }

    public List<TransactionAccountEntity> getAccountIbanDetails() {
        AccountIbanDetailsRequest request =
                new AccountIbanDetailsRequest(
                        BnpParibasConstants.AccountIbanDetails.MODE_BENEFICIAIRE_TRUE);
        IbanDetailsResponse ibanDetailsResponse =
                client.request(BnpParibasConstants.Urls.LIST_IBANS)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .post(IbanDetailsResponse.class, request);

        ibanDetailsResponse.assertReturnCodeOk();

        return ibanDetailsResponse.getData().getTransferInfo().getCreditAccountsList();
    }
}
