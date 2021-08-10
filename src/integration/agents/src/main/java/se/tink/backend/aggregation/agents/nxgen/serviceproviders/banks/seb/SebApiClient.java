package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants.InitResult;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants.ServiceInputKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants.ServiceInputValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants.SystemCode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants.UserMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.entities.DeviceIdentification;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.entities.HardwareInformation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.rpc.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.rpc.ChallengeSolutionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.entities.SystemStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.entities.UserInformation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.ReservedTransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.TransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.rpc.Request;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.rpc.Response;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.social.security.SocialSecurityNumber;

public class SebApiClient {
    private final TinkHttpClient httpClient;
    private final SebBaseConfiguration sebConfiguration;
    private final String sebUUID;
    private final SebSessionStorage sessionStorage;

    public SebApiClient(
            TinkHttpClient httpClient,
            SebBaseConfiguration sebConfiguration,
            SebSessionStorage sebSessionStorage) {
        this.httpClient = httpClient;
        this.sebConfiguration = sebConfiguration;
        this.sessionStorage = sebSessionStorage;
        sebUUID = UUID.randomUUID().toString().toUpperCase();

        this.httpClient.addFilter(new ServiceUnavailableBankServiceErrorFilter());
    }

    public AuthenticationResponse initiateBankId() {
        return AuthenticationResponse.fromHttpResponse(
                httpClient
                        .request(
                                SebConstants.Urls.getUrl(
                                        sebConfiguration.getAuthBaseUrl(), Urls.AUTHENTICATE))
                        .header(HeaderKeys.X_SEB_UUID, sebUUID)
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON)
                        .post(HttpResponse.class));
    }

    public AuthenticationResponse collectBankId(final String csrfToken) {
        return AuthenticationResponse.fromHttpResponse(
                httpClient
                        .request(
                                SebConstants.Urls.getUrl(
                                        sebConfiguration.getAuthBaseUrl(), Urls.AUTHENTICATE))
                        .header(HeaderKeys.X_SEB_UUID, sebUUID)
                        .header(HeaderKeys.X_SEB_CSRF, csrfToken)
                        .get(HttpResponse.class));
    }

    public ChallengeResponse getChallenge() {
        return httpClient
                .request(
                        SebConstants.Urls.getUrl(sebConfiguration.getAuthBaseUrl(), Urls.CHALLENGE))
                .header(HeaderKeys.X_SEB_UUID, sebUUID)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(ChallengeResponse.class);
    }

    public Response verifyChallengeSolution(String signature, String userId) {
        return httpClient
                .request(SebConstants.Urls.getUrl(sebConfiguration.getAuthBaseUrl(), Urls.VERIFY))
                .header(HeaderKeys.X_SEB_UUID, sebUUID)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .body(new ChallengeSolutionRequest(signature, userId))
                .post(Response.class);
    }

    private Response post(String path, Request request) {
        return httpClient
                .request(SebConstants.Urls.getUrl(sebConfiguration.getBaseUrl(), path))
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Response.class, request);
    }

    private void initiateSession(String userId) throws HttpResponseException {
        final Request request = new Request.Builder().withUserCredentials(userId).build();
        final Response response = post(SebConstants.Urls.INITIATE_SESSION, request);

        Preconditions.checkState(response.isValid());
        final String initResult = response.getInitResult();

        if (!InitResult.OK.equalsIgnoreCase(initResult)) {
            throw new IllegalStateException(
                    String.format(
                            "#login-refactoring - Expected initResult `ok` but received: %s",
                            initResult));
        }
    }

    public Response activateSession() throws AuthorizationException, AuthenticationException {
        final Request request =
                new Request.Builder()
                        .addComponent(new HardwareInformation())
                        .addComponent(new DeviceIdentification())
                        .addServiceInput(
                                ServiceInputKeys.CUSTOMER_TYPE,
                                sebConfiguration.isBusinessAgent()
                                        ? ServiceInputValues.BUSINESS
                                        : ServiceInputValues.PRIVATE)
                        .build();

        final Response response = post(SebConstants.Urls.ACTIVATE_SESSION, request);
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

        return response;
    }

    private void activateRole() throws HttpResponseException {
        final Response response =
                post(
                        Urls.ACTIVATE_ROLE,
                        new Request.Builder()
                                .addServiceInput(
                                        ServiceInputKeys.CUSTOMER_ID_EN,
                                        sessionStorage.getCustomerNumber())
                                .build());

        Preconditions.checkState(response.isValid());
    }

    public Response fetchAccounts(String customerId, String accountType) {
        Preconditions.checkNotNull(Strings.emptyToNull(customerId));
        Preconditions.checkNotNull(Strings.emptyToNull(accountType));
        final Request.Builder request =
                new Request.Builder().addServiceInput(ServiceInputKeys.CUSTOMER_ID, customerId);

        if (sebConfiguration.isBusinessAgent()) {
            request.addServiceInput(ServiceInputKeys.EXTRA_INFO, ServiceInputValues.YES);
        } else {
            request.addServiceInput(ServiceInputKeys.ACCOUNT_TYPE, accountType);
        }

        return post(sebConfiguration.getListAccountsUrl(), request.build());
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

    public Response fetchLoans() {
        final Request request = new Request.Builder().build();
        return post(Urls.LIST_LOANS, request);
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

    public Response fetchInvestmentAccounts() {
        final Request request = new Request.Builder().build();
        return post(Urls.LIST_INVESTMENT_ACCOUNTS, request);
    }

    public Response fetchInvestmentDetails(String handle) {
        final Request request =
                new Request.Builder()
                        .addServiceInput(ServiceInputKeys.INVESTMENT_DETAIL_HANDLE, handle)
                        .build();
        return post(Urls.INVESTMENT_ACCOUNT_DETAILS, request);
    }

    public void setupSession(String ssn) throws AuthenticationException, AuthorizationException {
        try {
            initiateSession("");
        } catch (HttpResponseException e) {
            if (sebConfiguration.isBusinessAgent()
                    && e.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
                throw LoginError.NOT_CUSTOMER.exception(e);
            }

            SocialSecurityNumber.Sweden formattedSsn = new SocialSecurityNumber.Sweden(ssn);
            if (!formattedSsn.isValid()) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(e);
            }

            // Check if the user is younger than 18 and then throw unauthorized exception.
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED
                    && formattedSsn.getAge(LocalDate.now(ZoneId.of("CET")))
                            < SebConstants.AGE_LIMIT) {
                throw AuthorizationError.UNAUTHORIZED.exception(
                        UserMessage.DO_NOT_SUPPORT_YOUTH.getKey(), e);
            }

            throw e;
        }

        final Response activateSessionResponse = activateSession();
        final UserInformation userInformation = activateSessionResponse.getUserInformation();
        Preconditions.checkNotNull(userInformation);

        // Check that the SSN from the credentials matches the logged in user. For business agent,
        // we cannot verify this since there is no SSN in the response
        if (!sebConfiguration.isBusinessAgent() && !userInformation.getSSN().equals(ssn)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        sessionStorage.putUserInformation(userInformation);

        if (sebConfiguration.isBusinessAgent()) {
            sessionStorage.putCompanyInformation(
                    activateSessionResponse.getMatchingCompanyInformation(
                            sebConfiguration.getOrganizationNumber()));
            activateRole();
        }
    }
}
