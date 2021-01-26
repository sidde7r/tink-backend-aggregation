package se.tink.backend.aggregation.agents.creditcards.sebkort;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.CollectBankIdRequest;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.ContractEntity;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.ContractsResponse;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.InvoiceBillingUnitEntity;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.InvoiceBillingUnitResponse;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.InvoiceBillingUnitsResponse;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.InvoiceDetailsEntity;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.InvoiceDetailsResponse;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.InvoiceEntity;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.OrderBankIdRequest;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.OrderBankIdResponse;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.utils.jsoup.ElementUtils;
import se.tink.backend.aggregation.agents.utils.signicat.SignicatParsingUtils;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n.Catalog;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class SEBKortAgent extends AbstractAgent implements DeprecatedRefreshExecutor {
    @SuppressWarnings("serial")
    private static class RetryableError extends Exception {
        public RetryableError(String msg) {
            super(msg);
        }
    }

    private static final int BANKID_MAX_ATTEMPTS = 80;
    private static final String AUTHENTICATION_BANKID_URL =
            "https://id.signicat.com/std/method/seb?method={0}&profile=nis_cobrand&language=sv&target=https%3A%2F%2Fsecure.sebkort.com%2Fsea%2Fexternal%2FProcessSignicatResponse%3Fmethod%3D{0}%26target%3D%252Fnis%252Fm%252F{1}%252Fexternal%252FvalidateEidLogin%26prodgroup%3D{2}%26SEB_Referer%3D%252Fnis%26uname%26countryCode%3DSE";
    private static final String BASE_URL = "https://application.sebkort.com/nis/m/";

    private final Client client;
    private final String code;
    private final Credentials credentials;
    private final String product;
    private boolean hasRefreshed = false;

    public SEBKortAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        client = setupClient();
        credentials = request.getCredentials();

        String[] providerPayload = request.getProvider().getPayload().split(":");

        this.code = providerPayload[0];
        this.product = providerPayload[1];
    }

    private void abortOnBankIdError(String code, String message) throws BankIdException {
        switch (code.toLowerCase()) {
            case "already_in_progress":
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            case "cancelled":
                // bankid cancelled due to `already_in_progress`
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            case "user_cancel":
                throw BankIdError.CANCELLED.exception();
            case "no_client":
                throw BankIdError.NO_CLIENT.exception();
            case "user_sign":
                // this happens if `BANKID_MAX_ATTEMPTS * SEELP_TIME` is less than SEB's timeout AND
                // the user
                // has not yet signed the authentication (but opened BankId).
                throw BankIdError.TIMEOUT.exception();
            default:
                throw new IllegalStateException(
                        String.format(
                                "#login-refactoring - SEBKort - Mobile Bank ID error, code %s, message %s",
                                code, message));
        }
    }

    /** Authenticate the user using username and Mobile BankID. */
    private boolean authenticateWithMobileBankId() throws BankIdException {
        // Fetch and parse a dynamically created endpoint for making requests to.

        String bankIdMethod = "sbid-remote-seb";

        if (code.equalsIgnoreCase("ecse")) {
            bankIdMethod = "sbid-remote-ec";
        }

        String authenticationUrl =
                Catalog.format(AUTHENTICATION_BANKID_URL, bankIdMethod, code, product);

        String authenticationReponse =
                createClientRequest(authenticationUrl, client, CommonHeaders.DEFAULT_USER_AGENT)
                        .get(String.class);

        String bankIdUrl = SignicatParsingUtils.parseBankIdServiceUrl(authenticationReponse);

        // Initiate a BankId authentication server-side.

        OrderBankIdRequest orderBankIdRequest = new OrderBankIdRequest();
        orderBankIdRequest.setSubject(credentials.getUsername());

        OrderBankIdResponse orderBankIdResponse =
                createClientRequest(bankIdUrl + "/order", client, CommonHeaders.DEFAULT_USER_AGENT)
                        .type(MediaType.APPLICATION_JSON)
                        .post(OrderBankIdResponse.class, orderBankIdRequest);

        // Check for errors during initialization
        if (orderBankIdResponse.getError() != null) {
            abortOnBankIdError(
                    orderBankIdResponse.getError().getCode(),
                    orderBankIdResponse.getError().getMessage());
        }

        // Prompt a BankId authentication client-side.

        credentials.setSupplementalInformation(null);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        supplementalRequester.requestSupplementalInformation(credentials, false);

        // Validate authentication.

        CollectBankIdRequest collectBankIdRequest = new CollectBankIdRequest();
        collectBankIdRequest.setOrderRef(orderBankIdResponse.getOrderRef());

        CollectBankIdResponse collectBankIdResponse = null;

        // Poll BankID status periodically until the process is complete.

        for (int i = 0; i < BANKID_MAX_ATTEMPTS; i++) {
            collectBankIdResponse =
                    createClientRequest(
                                    bankIdUrl + "/collect",
                                    client,
                                    CommonHeaders.DEFAULT_USER_AGENT)
                            .type(MediaType.APPLICATION_JSON)
                            .post(CollectBankIdResponse.class, collectBankIdRequest);

            if (Objects.equal(collectBankIdResponse.getProgressStatus(), "COMPLETE")) {
                break;
            }

            if (collectBankIdResponse.getError() != null) {
                abortOnBankIdError(
                        collectBankIdResponse.getError().getCode(),
                        collectBankIdResponse.getError().getMessage());
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        if (!Objects.equal(collectBankIdResponse.getProgressStatus(), "COMPLETE")) {
            abortOnBankIdError(collectBankIdResponse.getProgressStatus(), "");
        }

        String completeBankIdResponse =
                createClientRequest(
                                collectBankIdResponse.getCompleteUrl(),
                                client,
                                CommonHeaders.DEFAULT_USER_AGENT)
                        .type(MediaType.APPLICATION_JSON)
                        .post(String.class, collectBankIdRequest);

        // Initiate the SAML request.

        Document completeDocument = Jsoup.parse(completeBankIdResponse);
        Element formElement = completeDocument.getElementById("responseForm");

        Preconditions.checkNotNull(formElement);

        String endpoint = fixUrlFromSecureToApplication(formElement.attr("action"));

        String samlResponse =
                createClientRequest(endpoint, client, CommonHeaders.DEFAULT_USER_AGENT)
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .entity(ElementUtils.parseFormParameters(formElement))
                        .post(String.class);

        // Use the SAML created secret key to authenticate the user.

        completeDocument = Jsoup.parse(samlResponse);
        formElement = completeDocument.getElementById("iamForm");

        String authenticationEndpoint = fixUrlFromSecureToApplication(formElement.attr("action"));

        ClientResponse authenticateClientResponse =
                createClientRequest(
                                authenticationEndpoint, client, CommonHeaders.DEFAULT_USER_AGENT)
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .entity(ElementUtils.parseFormParameters(formElement))
                        .post(ClientResponse.class);

        // Check and see if the authentication was successful.

        String loginRedirectUrl = authenticateClientResponse.getHeaders().getFirst("Location");
        authenticateClientResponse.close();

        Preconditions.checkState(
                loginRedirectUrl != null && loginRedirectUrl.endsWith("validateEidLogin"));
        createClientRequest(loginRedirectUrl, client, CommonHeaders.DEFAULT_USER_AGENT)
                .get(String.class);
        return true;
    }

    /**
     * Replaces secure with application in the domain. We were getting the following when running
     * SEBKortAgent: javax.net.ssl.SSLException: hostname in certificate didn't match:
     * <secure.sebkort.com> != <application.sebkort.com> OR <application.sebkort.com>
     *
     * <p>As a hackish fix we go to application.sebkort.com instead of secure.sebkort.com
     *
     * @param url
     * @return
     */
    private String fixUrlFromSecureToApplication(String url) {
        if (url == null) {
            return null;
        }
        return url.replace("//secure.", "//application.");
    }

    private List<ContractEntity> fetchContracts() {
        ContractsResponse response =
                createClientRequest(
                                BASE_URL + code + "/a/contracts",
                                client,
                                CommonHeaders.DEFAULT_USER_AGENT)
                        .get(ContractsResponse.class);

        return response.getBody();
    }

    private List<InvoiceBillingUnitEntity> fetchInvoiceBillingUnits() {
        InvoiceBillingUnitsResponse response =
                createClientRequest(
                                BASE_URL + code + "/a/invoiceBillingUnits",
                                client,
                                CommonHeaders.DEFAULT_USER_AGENT)
                        .type(MediaType.APPLICATION_JSON)
                        .get(InvoiceBillingUnitsResponse.class);

        if (response.getErrorCode() != null) {
            warn(
                    "No invoice billing units: "
                            + response.getErrorCode()
                            + " "
                            + response.getMessage());
        }

        return response.getBody();
    }

    private InvoiceDetailsEntity fetchInvoiceDetails(
            InvoiceBillingUnitEntity invoiceBillingUnit, InvoiceEntity invoice)
            throws RetryableError {
        InvoiceDetailsResponse response =
                createClientRequest(
                                BASE_URL
                                        + code
                                        + "/a/invoices/details/"
                                        + invoiceBillingUnit.getArrangementNumber()
                                        + "/"
                                        + invoice.getInvoiceId(),
                                client,
                                CommonHeaders.DEFAULT_USER_AGENT)
                        .get(InvoiceDetailsResponse.class);

        if ("GENERIC_VALIDATION_ERROR".equals(response.getErrorCode())) {
            throw new RetryableError(response.getErrorCode());
        }

        return response.getBody();
    }

    private List<InvoiceEntity> fetchInvoices(InvoiceBillingUnitEntity invoiceBillingUnit)
            throws RetryableError {
        InvoiceBillingUnitResponse response =
                createClientRequest(
                                BASE_URL
                                        + code
                                        + "/a/invoices/"
                                        + invoiceBillingUnit.getArrangementNumber(),
                                client,
                                CommonHeaders.DEFAULT_USER_AGENT)
                        .get(InvoiceBillingUnitResponse.class);

        if ("GENERIC_TECHNICAL_ERROR".equals(response.getErrorCode())) {
            throw new RetryableError(response.getErrorCode());
        }

        return response.getBody();
    }

    private InvoiceDetailsEntity fetchPengingTransactions(ContractEntity contractEntity) {
        InvoiceDetailsResponse response =
                createClientRequest(
                                BASE_URL
                                        + code
                                        + "/a/pendingTransactions/"
                                        + contractEntity.getContractId(),
                                client,
                                CommonHeaders.DEFAULT_USER_AGENT)
                        .get(InvoiceDetailsResponse.class);

        return response.getBody();
    }

    /**
     * Helper method to determine if we're content with the refresh. We fetch information for
     * multiple accounts in a single stream of invoices, so each we must be content with all the
     * accounts before we're done.
     *
     * @param accountsByAccountNumber
     * @param transactionsByAccountNumber
     * @return
     */
    private boolean isContentWithRefresh(
            Map<String, Account> accountsByAccountNumber,
            Map<String, List<Transaction>> transactionsByAccountNumber) {
        Set<String> accountNumbers = accountsByAccountNumber.keySet();

        if (accountNumbers.isEmpty()) {
            return false;
        }

        for (String accountNumber : accountNumbers) {
            if (!isContentWithRefresh(
                    accountsByAccountNumber.get(accountNumber),
                    transactionsByAccountNumber.get(accountNumber))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void refresh() throws Exception {
        // The refresh command will call refresh multiple times.
        // This check ensures the refresh only runs once.
        if (hasRefreshed) {
            return;
        }
        hasRefreshed = true;

        // We are seeing spurious failures and currently don't know why this happens. Retrying a
        // couple of times yields
        // a lower failure rate.

        final int MAX_TRIES = 4;
        int triesLeft = MAX_TRIES;

        RetryableError lastError = null;

        while (triesLeft-- > 0) {
            try {
                retryableRefreshInformation();
            } catch (RetryableError e) {
                lastError = e;
                continue;
            }

            // Successful execution.
            return;
        }

        throw new RuntimeException("Ran out of retries. Attaching last retry error.", lastError);
    }

    private void retryableRefreshInformation() throws RetryableError, ParseException {
        SEBKortParser parser = new SEBKortParser();

        // Parse all the invoices.
        List<InvoiceBillingUnitEntity> invoiceBillingUnits = fetchInvoiceBillingUnits();

        if (invoiceBillingUnits == null) {
            statusUpdater.updateStatus(
                    CredentialsStatus.UPDATED, "Det finns inga fakturor att hämta.");
            return;
        }

        for (InvoiceBillingUnitEntity invoiceBillingUnit : invoiceBillingUnits) {
            parser.parseBillingUnit(invoiceBillingUnit);

            List<InvoiceEntity> invoices = fetchInvoices(invoiceBillingUnit);
            for (InvoiceEntity invoice : invoices) {
                InvoiceDetailsEntity invoiceDetails =
                        fetchInvoiceDetails(invoiceBillingUnit, invoice);

                parser.parseInvoiceDetails(invoiceDetails);

                if (isContentWithRefresh(
                        parser.getAccountsByAccountNumber(),
                        parser.getTransactionsByAccountNumber())) {
                    break;
                }
            }
        }

        // Parse all the contracts and the un-invoiced transactions.
        List<ContractEntity> contracts = fetchContracts();

        for (ContractEntity contractEntity : contracts) {
            InvoiceDetailsEntity invoiceDetails = fetchPengingTransactions(contractEntity);
            parser.parsePendingInvoiceDetails(contractEntity, invoiceDetails);
        }

        Map<String, Account> accountsByAccountNumber = parser.getAccountsByAccountNumber();

        for (String accountNumber : accountsByAccountNumber.keySet()) {
            Account account = accountsByAccountNumber.get(accountNumber);

            // Important that no RetryableError are thrown after this. Otherwise we risk have the
            // AgentContext in an
            // inconsistent state when we are retrying.

            financialDataCacher.updateTransactions(
                    account, parser.getTransactionsByAccountNumber().get(accountNumber));
        }
    }

    private Client setupClient() {
        Client cookieClient = clientFactory.createCookieClient(context.getLogOutputStream());

        cookieClient.setFollowRedirects(true);

        return cookieClient;
    }

    private void warn(String message) {
        log.warn(message);
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        switch (credentials.getType()) {
            case MOBILE_BANKID:
                return authenticateWithMobileBankId();
            default:
                throw new IllegalStateException("Credentials type not supported.");
        }
    }

    @Override
    public void logout() throws Exception {
        // NOP
    }

    /**
     * Helper method to create a Jersey request.
     *
     * @param url
     * @param client
     * @return
     * @throws Exception
     */
    private static Builder createClientRequest(String url, Client client, String userAgent) {
        return client.resource(url)
                .header("User-Agent", userAgent)
                .accept(MediaType.APPLICATION_JSON);
    }
}
