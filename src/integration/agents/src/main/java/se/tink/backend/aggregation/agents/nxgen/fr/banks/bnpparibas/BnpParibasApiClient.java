package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas;

import static se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasConstants.*;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasConstants.Errors;
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
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;

@Slf4j
@RequiredArgsConstructor
public class BnpParibasApiClient {

    private static final int RETRY_POLICY_MAX_ATTEMPTS = 3;

    private final TinkHttpClient client;
    private final BnpParibasConfigurationBase configuration;
    private final BnpParibasPersistentStorage bnpParibasPersistentStorage;

    public NumpadDataEntity getNumpadParams() {
        NumpadRequest formBody = new NumpadRequest(configuration.getGridType());

        NumpadResponse response =
                client.request(createUrl(Urls.NUMPAD))
                        .body(formBody, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(NumpadResponse.class);

        response.assertReturnCodeOk();
        return response.getData();
    }

    public LoginDataEntity login(String username, String gridId, String passwordIndices)
            throws LoginException {

        LoginRequest formBody =
                LoginRequest.builder()
                        .username(username)
                        .gridId(gridId)
                        .passwordIndices(passwordIndices)
                        .userAgent(configuration.getUserAgent())
                        .gridType(configuration.getGridType())
                        .distId(configuration.getDistId())
                        .appVersion(configuration.getAppVersion())
                        .buildNumber(configuration.getBuildNumber())
                        .idFaValue(bnpParibasPersistentStorage.getIdfaValue())
                        .idFvValue(bnpParibasPersistentStorage.getIdfVValue())
                        .build();

        LoginResponse response =
                client.request(createUrl(Urls.LOGIN))
                        .body(formBody, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(LoginResponse.class);

        if (!Strings.isNullOrEmpty(response.getErrorCode())) {
            handleErrors(response);
        }
        response.assertReturnCodeOk();
        return response.getData();
    }

    public void keepAlive() {
        BaseResponse response = client.request(createUrl(Urls.KEEP_ALIVE)).get(BaseResponse.class);

        response.assertReturnCodeOk();
    }

    public Collection<Transaction> getTransactionalAccountTransactions(
            Date fromDate, Date toDate, String ibanKey) {
        TransactionalAccountTransactionsRequest request =
                TransactionalAccountTransactionsRequest.create(
                        configuration.getTriAvValue(),
                        fromDate,
                        toDate,
                        ibanKey,
                        configuration.getPastOrPendingValue());

        TransactionalAccountTransactionsResponse response = null;
        int tries = 0;
        for (; tries < RETRY_POLICY_MAX_ATTEMPTS; tries++) {
            try {
                response =
                        client.request(createUrl(Urls.TRANSACTIONAL_ACCOUNT_TRANSACTIONS))
                                .type(MediaType.APPLICATION_JSON_TYPE)
                                .post(TransactionalAccountTransactionsResponse.class, request);
                break;
            } catch (HttpClientException hce) {
                log.error(
                        String.format(
                                "[Try %d]: WebApplicationException -- getTransactionalAccountTransactions",
                                tries),
                        hce);
            }
            Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
        }
        if (tries == RETRY_POLICY_MAX_ATTEMPTS) {
            log.info(
                    "getTransactionalAccountTransactions -- Max tries reached, returning empty list of transactions.");
            return Collections.emptyList();
        }
        if (Objects.isNull(response)) {
            return Collections.emptyList();
        }
        response.assertReturnCodeOk();

        return response.getData().transactionsInfo().getAccountTransactions().toTinkTransactions();
    }

    public InfoUdcEntity getAccounts() {
        AccountsResponse response =
                client.request(createUrl(Urls.LIST_ACCOUNTS)).get(AccountsResponse.class);

        response.assertReturnCodeOk();

        return response.getData().getInfoUdc();
    }

    public List<TransactionAccountEntity> getAccountIbanDetails() {
        AccountIbanDetailsRequest request =
                new AccountIbanDetailsRequest(AccountIbanDetails.MODE_BENEFICIAIRE_TRUE);
        IbanDetailsResponse ibanDetailsResponse =
                client.request(createUrl(Urls.LIST_IBANS))
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .post(IbanDetailsResponse.class, request);

        ibanDetailsResponse.assertReturnCodeOk();

        return ibanDetailsResponse.getData().getTransferInfo().getCreditAccountsList();
    }

    private String createUrl(String path) {
        return configuration.getHost() + path;
    }

    private void handleErrors(LoginResponse response) {
        String errorCode = response.getErrorCode();
        if (Errors.INCORRECT_CREDENTIALS.equals(errorCode)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else if (Errors.LOGIN_ERROR.equals(errorCode)) {
            throw LoginError.DEFAULT_MESSAGE.exception();
        } else if (Errors.ACCOUNT_ERROR.equals(errorCode)) {
            throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(response.getMessage());
        }
    }
}
