package se.tink.backend.aggregation.agents.nxgen.se.banks.seb;

import com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.InitResult;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.ServiceInputKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.ServiceInputValues;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.SystemCode;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.UserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.entities.DeviceIdentification;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.entities.HardwareInformation;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.rpc.BankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.SystemStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.UserInformation;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.PendingTransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.TransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.rpc.Request;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.rpc.Response;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.social.security.SocialSecurityNumber;

public class SEBApiClient {
    private final TinkHttpClient httpClient;
    private final String sebUUID;
    private final String credentialsUserName;

    public SEBApiClient(TinkHttpClient httpClient, String credentialsUserName) {
        this.httpClient = httpClient;
        this.credentialsUserName = credentialsUserName;
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

    public void initiateSession() throws AuthorizationException, AuthenticationException {
        final Request request = new Request.Builder().withUserCredentials("").build();
        final Response response;

        try {
            response = post(Urls.INITIATE_SESSION, request);
        } catch (HttpResponseException e) {
            // Check if the user is younger than 18 and then throw unauthorized exception.
            SocialSecurityNumber.Sweden ssn = new SocialSecurityNumber.Sweden(credentialsUserName);
            if (!ssn.isValid()) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            if (e.getResponse().getStatus() == 403
                    && ssn.getAge(LocalDate.now(ZoneId.of("CET"))) < 18) {
                throw AuthorizationError.UNAUTHORIZED.exception(
                        UserMessage.DO_NOT_SUPPORT_YOUTH.getKey());
            }

            throw e;
        }

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
            if (systemStatus == null) {
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

    public Response fetchPendingTransactions(PendingTransactionQuery query) {
        final Request request = new Request.Builder().addComponent(query).build();
        return post(Urls.LIST_PENDING_TRANSACTIONS, request);
    }
}
