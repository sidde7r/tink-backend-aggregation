package se.tink.backend.aggregation.agents.nxgen.se.banks.seb;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Objects;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.InitResult;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.ServiceInputKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.ServiceInputValues;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.SystemCode;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.UserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.entities.DeviceIdentification;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.entities.HardwareInformation;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.rpc.BankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.SystemStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.UserInformation;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.ReservedTransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.TransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.rpc.Request;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.rpc.Response;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class SebApiClient {
    private final TinkHttpClient httpClient;
    private final String sebUUID;

    public SebApiClient(TinkHttpClient httpClient) {
        this.httpClient = httpClient;
        sebUUID = UUID.randomUUID().toString().toUpperCase();
    }

    public BankIdResponse fetchAutostartToken() {
        return httpClient
                .request(Urls.FETCH_AUTOSTART_TOKEN)
                .header(HeaderKeys.X_SEB_UUID, sebUUID)
                .body(new BankIdRequest(), MediaType.APPLICATION_JSON)
                .post(BankIdResponse.class);
    }

    public BankIdResponse collectBankId(final String reference) {
        return httpClient
                .request(Urls.COLLECT_BANKID.concat(reference))
                .header(HeaderKeys.X_SEB_UUID, sebUUID)
                .post(BankIdResponse.class);
    }

    private Response post(URL url, Request request) {
        return httpClient
                .request(url)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Response.class, request);
    }

    public void initiateSession() throws HttpResponseException {
        final Request request = new Request.Builder().withUserCredentials("").build();
        final Response response = post(Urls.INITIATE_SESSION, request);

        Preconditions.checkState(response.isValid());
        final String initResult = response.getInitResult();

        if (!InitResult.OK.equalsIgnoreCase(initResult)) {
            throw new IllegalStateException(
                    String.format(
                            "#login-refactoring - Expected initResult `ok` but received: %s",
                            initResult));
        }
    }

    public UserInformation activateSession()
            throws AuthorizationException, AuthenticationException {
        final Request request =
                new Request.Builder()
                        .addComponent(new HardwareInformation())
                        .addComponent(new DeviceIdentification())
                        .addServiceInput(ServiceInputKeys.CUSTOMER_TYPE, ServiceInputValues.PRIVATE)
                        .build();

        final Response response = post(Urls.ACTIVATE_SESSION, request);
        if (!response.isValid()) {
            final SystemStatus systemStatus = response.getSystemStatus();
            if (Objects.isNull(systemStatus)) {
                throw new IllegalStateException(
                        "Response was not valid, but contained no system status");
            }
            switch (response.getSystemStatus().getSystemCode()) {
                case SystemCode.BANKID_NOT_AUTHORIZED:
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                            UserMessage.MUST_AUTHORIZE_BANKID.getKey());
                case SystemCode.KYC_ERROR:
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                            UserMessage.MUST_ANSWER_KYC.getKey());
                default:
                    throw new IllegalStateException(
                            String.format(
                                    "#login-refactoring - SEB - Login failed with system code %s, "
                                            + "system message %s, first error message %s",
                                    systemStatus.getSystemCode(),
                                    systemStatus.getSystemMessage(),
                                    response.getFirstErrorMessage()));
            }
        }

        final UserInformation userInformation = response.getUserInformation();
        Preconditions.checkNotNull(userInformation);
        return userInformation;
    }

    public Response fetchAccounts(String customerId, String accountType) {
        Preconditions.checkNotNull(Strings.emptyToNull(customerId));
        Preconditions.checkNotNull(Strings.emptyToNull(accountType));
        final Request request =
                new Request.Builder()
                        .addServiceInput(ServiceInputKeys.CUSTOMER_ID, customerId)
                        .addServiceInput(ServiceInputKeys.ACCOUNT_TYPE, accountType)
                        .build();
        return post(Urls.LIST_ACCOUNTS, request);
    }

    public Response fetchTransactions(TransactionQuery query) {
        final Request request = new Request.Builder().addComponent(query).build();
        return post(Urls.LIST_TRANSACTIONS, request);
    }

    public Response fetchReservedTransactions(ReservedTransactionQuery query) {
        final Request request = new Request.Builder().addComponent(query).build();
        return post(Urls.LIST_RESERVED_TRANSACTIONS, request);
    }

    public Response fetchUpcomingTransactions(String customerId) {
        final Request request =
                new Request.Builder()
                        .addServiceInput(ServiceInputKeys.CUSTOMER_NUMBER, customerId)
                        .addServiceInput(ServiceInputKeys.MAX_ROWS, ServiceInputValues.MAX_ROWS)
                        .build();
        return post(Urls.LIST_UPCOMING_TRANSACTIONS, request);
    }

    public Response fetchCreditCardAccounts() {
        final Request request = new Request.Builder().build();
        return post(Urls.LIST_CARDS, request);
    }

    public Response fetchPendingCreditCardTransactions(String uniqueId) {
        final Request request =
                new Request.Builder()
                        .addServiceInput(ServiceInputKeys.CREDIT_CARD_HANDLE, uniqueId)
                        .addServiceInput(
                                ServiceInputKeys.PENDING_TRANSACTIONS, ServiceInputValues.YES)
                        .build();

        return post(Urls.LIST_PENDING_CREDIT_CARD_TRANSACTIONS, request);
    }

    public Response fetchBookedCreditCardTransactions(String uniqueId) {
        final Request request =
                new Request.Builder()
                        .addServiceInput(ServiceInputKeys.CREDIT_CARD_HANDLE, uniqueId)
                        .build();

        return post(Urls.LIST_BOOKED_CREDIT_CARD_TRANSACTIONS, request);
    }
}
