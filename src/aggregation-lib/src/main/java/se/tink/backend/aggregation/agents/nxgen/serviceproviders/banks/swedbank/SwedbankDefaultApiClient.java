package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javax.ws.rs.core.Cookie;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.payment.rpc.RegisterPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.CollectBankIdSignResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.InitiateSignTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.InitiateSignTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.rpc.RegisterTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.RegisterTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.RegisteredTransfersResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc.PaymentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc.PaymentsConfirmedResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.creditcard.rpc.DetailedCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.einvoice.rpc.EInvoiceDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.einvoice.rpc.EInvoiceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.einvoice.rpc.IncomingEinvoicesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc.FundMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.EngagementTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.MenuItemLinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.ProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.SelectedProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.UpcomingTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class SwedbankDefaultApiClient {
    private static final Logger log = LoggerFactory.getLogger(SwedbankDefaultApiClient.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    protected final TinkHttpClient client;
    private final SwedbankConfiguration configuration;
    private final String username;
    private Map<String, MenuItemLinkEntity> menuItems;
    private EngagementOverviewResponse engagementOverviewResponse;
    private PaymentBaseinfoResponse paymentBaseinfoResponse;

    SwedbankDefaultApiClient(TinkHttpClient client, SwedbankConfiguration configuration, String username) {
        this.client = client;
        this.configuration = configuration;
        this.username = username;
        ensureAuthorizationHeaderIsSet();
    }

    private <T> T makeGetRequest(URL url, Class<T> responseClass) {
        return buildAbstractRequest(url).get(responseClass);
    }

    private <T> T makePostRequest(URL url, Object requestObject, Class<T> responseClass) {
        return buildAbstractRequest(url).post(responseClass, requestObject);
    }

    private <T> T makePutRequest(URL url, Object requestObject, Class<T> responseClass) {
        return buildAbstractRequest(url).put(responseClass, requestObject);
    }

    private <T> T makeRequest(LinkEntity linkEntity, Class<T> responseClass) {
        return makeRequest(linkEntity, null, responseClass);
    }

    private <T> T makeRequest(LinkEntity linkEntity, Object requestObject, Class<T> responseClass) {
        return makeRequest(linkEntity, requestObject, responseClass, Collections.emptyMap());
    }

    private <T> T makeRequest(LinkEntity linkEntity, Object requestObject, Class<T> responseClass,
            Map<String, String> parameters) {
        LinkEntity.LinkMethod method = linkEntity.getMethod();
        Preconditions.checkState(linkEntity.isValid(),
                "Create dynamic request failed - Cannot proceed without valid link entity - Method:[{}], Uri:[{}]",
                method, linkEntity.getUri());

        switch (method) {
        case POST:
            return makePostRequest(
                    SwedbankBaseConstants.Url.createDynamicUrl(linkEntity.getUri(), parameters), requestObject,
                    responseClass);
        case GET:
            return makeGetRequest(
                    SwedbankBaseConstants.Url.createDynamicUrl(linkEntity.getUri(), parameters), responseClass);
        case PUT:
            return makePutRequest(
                    SwedbankBaseConstants.Url.createDynamicUrl(linkEntity.getUri(), parameters), requestObject,
                    responseClass);
        default:
            log.warn("Create dynamic request failed - Not implemented method - Method:[{}]", method);
            throw new IllegalStateException();
        }
    }

    private RequestBuilder buildAbstractRequest(URL url) {
        String dsid = generateDSID();

        return client.request(url)
                .header(SwedbankBaseConstants.Headers.AUTHORIZATION_KEY,
                        SwedbankBaseConstants.generateAuthorization(configuration, username))
                .queryParam(SwedbankBaseConstants.Url.DSID_KEY, dsid)
                .cookie(new Cookie(SwedbankBaseConstants.Url.DSID_KEY, dsid));
    }

    private static String generateDSID() {
        byte bytes[] = new byte[6];
        Base64 base64 = new Base64(100, null, true);
        RANDOM.nextBytes(bytes);
        return base64.encodeAsString(bytes);
    }

    public InitBankIdResponse initBankId(String ssn) {
        return makePostRequest(
                SwedbankBaseConstants.Url.INIT_BANKID.get(),
                InitBankIdRequest.createFromUserId(ssn),
                InitBankIdResponse.class);
    }

    public CollectBankIdResponse collectBankId(LinkEntity linkEntity) {
        return makeRequest(linkEntity, CollectBankIdResponse.class);
    }

    public ProfileResponse completeBankId(LinkEntity linkEntity) {
        return makeRequest(linkEntity, ProfileResponse.class);
    }

    public void selectProfile(LinkEntity linkEntity) {
        SelectedProfileResponse selectedProfileResponse = makeRequest(linkEntity,
                SelectedProfileResponse.class);

        Map<String, MenuItemLinkEntity> menuItemsMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        menuItemsMap.putAll(
                Optional.ofNullable(selectedProfileResponse.getMenuItems())
                        .orElseThrow(IllegalStateException::new));

        this.menuItems = menuItemsMap;
    }

    public EngagementOverviewResponse engagementOverview() {
        if (engagementOverviewResponse == null) {
            engagementOverviewResponse = makeMenuItemRequest(
                    SwedbankBaseConstants.MenuItemKey.ACCOUNTS, EngagementOverviewResponse.class);
        }

        return engagementOverviewResponse;
    }

    public EngagementTransactionsResponse engagementTransactions(LinkEntity linkEntity) {
        return makeRequest(linkEntity, EngagementTransactionsResponse.class);
    }

    public UpcomingTransactionsResponse upcomingTransactions() {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.UPCOMING_TRANSACTIONS, UpcomingTransactionsResponse.class);
    }

    public String loanOverview() {
        return makeMenuItemRequest(SwedbankBaseConstants.MenuItemKey.LOANS, String.class);
    }

    public String optionalRequest(LinkEntity linkEntity) {
        return makeRequest(linkEntity, String.class);
    }

    public LoanDetailsResponse loanDetails(LinkEntity linkEntity) {
        return makeRequest(linkEntity, LoanDetailsResponse.class);
    }

    public DetailedCardAccountResponse cardAccountDetails(LinkEntity linkEntity) {
        return makeRequest(linkEntity, DetailedCardAccountResponse.class);
    }

    public String portfolioHoldings() {
        return makeMenuItemRequest(SwedbankBaseConstants.MenuItemKey.PORTFOLIOS, String.class);
    }

    public String detailedPortfolioInfo(LinkEntity linkEntity) {
        return makeRequest(linkEntity, String.class);
    }

    public FundMarketInfoResponse fundMarketInfo(LinkEntity linkEntity) {
        return makeRequest(linkEntity, FundMarketInfoResponse.class);
    }

    public FundMarketInfoResponse fundMarketInfo(String fundCode) {
        return makeMenuItemRequest(SwedbankBaseConstants.MenuItemKey.FUND_MARKET_INFO, null,
                FundMarketInfoResponse.class, ImmutableMap.of(SwedbankBaseConstants.ParameterKey.FUND_CODE, fundCode));
    }

    public List<EInvoiceEntity> incomingEInvoices() {
        IncomingEinvoicesResponse incomingEinvoicesResponse = makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.EINVOICES, IncomingEinvoicesResponse.class);

        return incomingEinvoicesResponse.getEinvoices();
    }

    public EInvoiceDetailsResponse eInvoiceDetails(LinkEntity linkEntity) {
        return makeRequest(linkEntity, EInvoiceDetailsResponse.class);
    }

    public PaymentBaseinfoResponse paymentBaseinfo() {
        if (this.paymentBaseinfoResponse == null) {
            this.paymentBaseinfoResponse = makeMenuItemRequest(SwedbankBaseConstants.MenuItemKey.PAYMENT_BASEINFO,
                    PaymentBaseinfoResponse.class);
        }

        return this.paymentBaseinfoResponse;
    }

    public RegisterTransferResponse registerTransfer(double amount, String destinationAccountId,
            String sourceAccountId) {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.REGISTER_TRANSFER,
                RegisterTransferRequest.create(amount, destinationAccountId, sourceAccountId),
                RegisterTransferResponse.class);
    }

    public RegisterTransferResponse registerPayment(double amount, String message,
            SwedbankTransferHelper.ReferenceType referenceType, Date date, String destinationAccountId,
            String sourceAccountId) {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.REGISTER_PAYMENT,
                RegisterPaymentRequest.create(amount, message, referenceType, date, destinationAccountId,
                        sourceAccountId),
                RegisterTransferResponse.class);
    }

    public RegisterTransferResponse registerEInvoice(double amount, String message,
            SwedbankTransferHelper.ReferenceType referenceType, Date date, String eInvoiceId,
            String destinationAccountId, String sourceAccountId) {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.REGISTER_PAYMENT,
                RegisterPaymentRequest.create(amount, message, referenceType, date, destinationAccountId,
                        sourceAccountId),
                RegisterTransferResponse.class);
    }

    public RegisteredTransfersResponse registeredTransfers() {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.PAYMENT_REGISTERED,
                RegisteredTransfersResponse.class);
    }

    public RegisteredTransfersResponse registeredTransfers(LinkEntity linkEntity) {
        return makeRequest(linkEntity, RegisteredTransfersResponse.class);
    }

    public ConfirmTransferResponse confirmTransfer(LinkEntity linkEntity) {
        return makeRequest(linkEntity, ConfirmTransferResponse.class);
    }

    public PaymentsConfirmedResponse paymentsConfirmed() {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.PAYMENTS_CONFIRMED,
                PaymentsConfirmedResponse.class);
    }

    public PaymentDetailsResponse paymentDetails(LinkEntity linkEntity) {
        return makeRequest(linkEntity, PaymentDetailsResponse.class);
    }

    public RegisterTransferResponse updatePayment(LinkEntity linkEntity, double amount, String message,
            SwedbankTransferHelper.ReferenceType referenceType, Date date, String recipientId, String fromAccountId) {
        return makeRequest(linkEntity,
                RegisterPaymentRequest.create(amount, message, referenceType, date, recipientId, fromAccountId),
                RegisterTransferResponse.class);
    }

    public InitiateSignTransferResponse signExternalTransfer(LinkEntity linkEntity) {
        return makeRequest(linkEntity, InitiateSignTransferRequest.create(), InitiateSignTransferResponse.class);
    }

    public CollectBankIdSignResponse collectSignBankId(LinkEntity linkEntity) {
        return makeRequest(linkEntity, CollectBankIdSignResponse.class);
    }

    private <T> T makeMenuItemRequest(SwedbankBaseConstants.MenuItemKey menuItemKey, Class<T> responseClass) {
        return makeMenuItemRequest(menuItemKey, null, responseClass);
    }

    private <T> T makeMenuItemRequest(SwedbankBaseConstants.MenuItemKey menuItemKey, Object requestObject,
            Class<T> responseClass) {
        return makeMenuItemRequest(menuItemKey, requestObject, responseClass, Collections.emptyMap());
    }

    private <T> T makeMenuItemRequest(SwedbankBaseConstants.MenuItemKey menuItemKey, Object requestObject,
            Class<T> responseClass, Map<String, String> parameters) {
        Preconditions.checkNotNull(menuItemKey);
        Preconditions.checkNotNull(menuItems);
        Preconditions.checkState(menuItems.containsKey(menuItemKey.getKey()));
        MenuItemLinkEntity menuItem = menuItems.get(menuItemKey.getKey());

        if (!menuItem.isAuthorized()) {
            log.warn("User not authorized to perform request with key: [{}], name: [{}], authorization: [{}]",
                    menuItemKey, menuItem.getName(), menuItem.getAuthorization());
            throw new IllegalStateException();
        }

        return makeRequest(menuItem, requestObject, responseClass, parameters);
    }

    private void ensureAuthorizationHeaderIsSet() {
        if (client.isPersistentHeaderPresent(SwedbankBaseConstants.Headers.AUTHORIZATION_KEY)) {
            return;
        }

        client.addPersistentHeader(SwedbankBaseConstants.Headers.AUTHORIZATION_KEY,
                SwedbankBaseConstants.generateAuthorization(configuration, username));
    }
}
