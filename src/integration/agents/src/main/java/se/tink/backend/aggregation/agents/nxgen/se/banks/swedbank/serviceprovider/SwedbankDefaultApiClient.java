package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javax.ws.rs.core.Cookie;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.LoanOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.Retry;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.InitAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.InitSecurityTokenChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.SecurityTokenChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.SecurityTokenChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.payment.rpc.RegisterPayeeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.payment.rpc.RegisterPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.payment.rpc.RegisterRecipientResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.CollectBankIdSignResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.InitiateSecurityTokenSignTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.InitiateSignTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.InitiateSignTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.RegisterTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.RegisteredTransfersResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.transfer.rpc.RegisterTransferRecipientRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.transfer.rpc.RegisterTransferRecipientResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.transfer.rpc.RegisterTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.updatepayment.rpc.PaymentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.updatepayment.rpc.PaymentsConfirmedResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.creditcard.rpc.DetailedCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.einvoice.rpc.EInvoiceDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.einvoice.rpc.EInvoiceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.einvoice.rpc.IncomingEinvoicesResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.investment.rpc.DetailedPortfolioResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.investment.rpc.FundMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.investment.rpc.PensionPortfoliosResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.investment.rpc.PortfolioHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfileHandler;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.EngagementTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.MenuItemLinkEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.SelectedProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.TouchResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SwedbankDefaultApiClient {
    private static final Logger log = LoggerFactory.getLogger(SwedbankDefaultApiClient.class);
    protected final TinkHttpClient client;
    private final SwedbankConfiguration configuration;
    private final String username;
    private final SwedbankStorage swedbankStorage;
    private final AgentComponentProvider componentProvider;
    // only use cached menu items for a profile
    private BankProfileHandler bankProfileHandler;
    private final String organizationNumber;

    protected SwedbankDefaultApiClient(
            TinkHttpClient client,
            SwedbankConfiguration configuration,
            String username,
            SwedbankStorage swedbankStorage,
            AgentComponentProvider componentProvider) {
        this.client = client;
        this.configuration = configuration;
        this.username = username;
        this.swedbankStorage = swedbankStorage;
        this.componentProvider = componentProvider;
        this.organizationNumber =
                Optional.ofNullable(
                                componentProvider
                                        .getCredentialsRequest()
                                        .getCredentials()
                                        .getField(Key.CORPORATE_ID))
                        .orElse("");
        ensureAuthorizationHeaderIsSet();
    }

    private String generateDSID() {
        Base64 base64 = new Base64(100, null, true);
        return base64.encodeAsString(componentProvider.getRandomValueGenerator().secureRandom(6));
    }

    private <T> T makeGetRequest(URL url, Class<T> responseClass) {
        return buildAbstractRequest(url).get(responseClass);
    }

    private HttpResponse makeGetRequest(LinkEntity linkEntity, boolean retry) {
        return makeRequest(linkEntity, HttpResponse.class, retry);
    }

    private <T> T makePostRequest(URL url, Object requestObject, Class<T> responseClass) {
        return buildAbstractRequest(url).post(responseClass, requestObject);
    }

    private <T> T makePutRequest(URL url, Object requestObject, Class<T> responseClass) {
        return buildAbstractRequest(url).put(responseClass, requestObject);
    }

    private <T> T makeDeleteRequest(URL url, Object requestObject, Class<T> responseClass) {
        return buildAbstractRequest(url).delete(responseClass, requestObject);
    }

    protected <T> T makeRequest(LinkEntity linkEntity, Class<T> responseClass, boolean retry) {
        return makeRequest(linkEntity, null, responseClass, retry, Retry.FIRST_ATTEMPT);
    }

    private <T> T makeRequest(
            LinkEntity linkEntity,
            Object requestObject,
            Class<T> responseClass,
            boolean retry,
            int attempt) {
        try {
            return makeRequest(linkEntity, requestObject, responseClass, Collections.emptyMap());
        } catch (HttpResponseException hre) {
            if (SwedbankApiErrors.isSessionTerminated(hre)) {
                throw BankServiceError.SESSION_TERMINATED.exception(hre);
            }

            if (retry) {
                return makeRequestWithRetry(hre, linkEntity, requestObject, responseClass, attempt);
            } else {
                throw hre;
            }
        }
    }

    private <T> T makeRequest(
            LinkEntity linkEntity,
            Object requestObject,
            Class<T> responseClass,
            Map<String, String> parameters) {
        SwedbankBaseConstants.LinkMethod method = linkEntity.getMethodValue();
        Preconditions.checkState(
                linkEntity.isValid(),
                "Create dynamic request failed - Cannot proceed without valid link entity - Method:[{}], Uri:[{}]",
                method,
                linkEntity.getUri());

        switch (method) {
            case POST:
                return makePostRequest(
                        SwedbankBaseConstants.Url.createDynamicUrl(linkEntity.getUri(), parameters),
                        requestObject,
                        responseClass);
            case GET:
                return makeGetRequest(
                        SwedbankBaseConstants.Url.createDynamicUrl(linkEntity.getUri(), parameters),
                        responseClass);
            case PUT:
                return makePutRequest(
                        SwedbankBaseConstants.Url.createDynamicUrl(linkEntity.getUri(), parameters),
                        requestObject,
                        responseClass);
            case DELETE:
                return makeDeleteRequest(
                        SwedbankBaseConstants.Url.createDynamicUrl(linkEntity.getUri(), parameters),
                        requestObject,
                        responseClass);
            default:
                log.warn(
                        "Create dynamic request failed - Not implemented method - Method:[{}]",
                        method);
                throw new IllegalStateException();
        }
    }

    private RequestBuilder buildAbstractRequest(URL url) {
        String dsid = generateDSID();

        return client.request(url)
                .header(
                        SwedbankBaseConstants.Headers.AUTHORIZATION_KEY,
                        SwedbankBaseConstants.generateAuthorization(configuration, username))
                .queryParam(SwedbankBaseConstants.Url.DSID_KEY, dsid)
                .cookie(new Cookie(SwedbankBaseConstants.Url.DSID_KEY, dsid));
    }

    private <T> T makeRequestWithRetry(
            HttpResponseException hre,
            LinkEntity linkEntity,
            Object requestObject,
            Class<T> responseClass,
            int attempt) {

        if (hre.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            if (attempt <= Retry.MAX_RETRY_ATTEMPTS) {
                log.info("Retrying fetching {}", responseClass, hre);
                return makeRequest(linkEntity, requestObject, responseClass, true, ++attempt);
            }
        }

        throw hre;
    }

    public InitBankIdResponse initBankId(String ssn) {
        try {
            return makePostRequest(
                    SwedbankBaseConstants.Url.INIT_BANKID.get(),
                    InitAuthenticationRequest.createFromUserId(ssn),
                    InitBankIdResponse.class);
        } catch (HttpClientException hce) {
            String errorMessage = Strings.nullToEmpty(hce.getMessage()).toLowerCase();
            if (errorMessage.contains(SwedbankBaseConstants.ErrorMessage.CONNECT_TIMEOUT)) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(hce);
            }

            throw hce;
        }
    }

    public CollectBankIdResponse collectBankId(LinkEntity linkEntity) {
        return makeRequest(linkEntity, CollectBankIdResponse.class, false);
    }

    public PaymentBaseinfoResponse confirmSignNewRecipient(LinkEntity linkEntity) {
        return makeRequest(linkEntity, PaymentBaseinfoResponse.class, false);
    }

    // this is where we handle the profiles, fetch all and store store in session storage
    // never assume anything in session storage is usable when authenticating, it is setup
    // after login
    public ProfileResponse completeAuthentication(LinkEntity linkEntity)
            throws AuthenticationException {
        ProfileResponse profileResponse;
        try {
            profileResponse = makeRequest(linkEntity, ProfileResponse.class, false);
        } catch (HttpResponseException hre) {
            if (SwedbankApiErrors.isUserNotACustomer(hre)) {
                throw LoginError.NOT_CUSTOMER.exception(hre);
            }
            // unknown error: rethrow
            throw hre;
        }
        if (!hasValidProfile(profileResponse)) {
            if (modeAndProfileMismatch(profileResponse)) {
                if (!configuration.isSavingsBank()) {
                    throw LoginError.NOT_CUSTOMER.exception(
                            SwedbankBaseConstants.UserMessage.WRONG_BANK_SWEDBANK);
                } else {
                    throw LoginError.NOT_CUSTOMER.exception(
                            SwedbankBaseConstants.UserMessage.WRONG_BANK_SAVINGSBANK);
                }
            }

            throw LoginError.NOT_CUSTOMER.exception();
        }

        setupProfiles(profileResponse);

        return profileResponse;
    }

    public EngagementOverviewResponse engagementOverview() {
        return fetchEngagementOverview();
    }

    public EngagementTransactionsResponse engagementTransactions(LinkEntity linkEntity) {
        return makeRequest(linkEntity, EngagementTransactionsResponse.class, true);
    }

    public LoanOverviewResponse loanOverview() {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.LOANS, LoanOverviewResponse.class);
    }

    public String loanOverviewAsString() {
        return makeMenuItemRequest(SwedbankBaseConstants.MenuItemKey.LOANS, String.class);
    }

    public String optionalRequest(LinkEntity linkEntity) {
        return makeRequest(linkEntity, String.class, true);
    }

    public LoanDetailsResponse loanDetails(LinkEntity linkEntity) {
        return makeRequest(linkEntity, LoanDetailsResponse.class, true);
    }

    public DetailedCardAccountResponse cardAccountDetails(LinkEntity linkEntity) {
        return makeRequest(linkEntity, DetailedCardAccountResponse.class, true);
    }

    public PortfolioHoldingsResponse portfolioHoldings() {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.PORTFOLIOS, PortfolioHoldingsResponse.class);
    }

    public PensionPortfoliosResponse getPensionPortfolios() {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.PENSION_PORTFOLIOS,
                PensionPortfoliosResponse.class);
    }

    public DetailedPortfolioResponse detailedPortfolioInfo(LinkEntity linkEntity) {
        return makeRequest(linkEntity, DetailedPortfolioResponse.class, true);
    }

    public FundMarketInfoResponse fundMarketInfo(LinkEntity linkEntity) {
        return makeRequest(linkEntity, FundMarketInfoResponse.class, true);
    }

    public FundMarketInfoResponse fundMarketInfo(String fundCode) {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.FUND_MARKET_INFO,
                null,
                FundMarketInfoResponse.class,
                ImmutableMap.of(SwedbankBaseConstants.ParameterKey.FUND_CODE, fundCode));
    }

    public List<EInvoiceEntity> incomingEInvoices() {
        IncomingEinvoicesResponse incomingEinvoicesResponse =
                makeMenuItemRequest(
                        SwedbankBaseConstants.MenuItemKey.EINVOICES,
                        IncomingEinvoicesResponse.class);

        return incomingEinvoicesResponse.getEinvoices();
    }

    public EInvoiceDetailsResponse eInvoiceDetails(LinkEntity linkEntity) {
        return makeRequest(linkEntity, EInvoiceDetailsResponse.class, false);
    }

    public PaymentBaseinfoResponse paymentBaseinfo() {
        return fetchPaymentBaseinfo();
    }

    public RegisterRecipientResponse registerPayee(RegisterPayeeRequest registerPayeeRequest)
            throws TransferExecutionException {

        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.REGISTER_PAYEE,
                registerPayeeRequest,
                RegisterRecipientResponse.class);
    }

    public RegisterTransferRecipientResponse registerTransferRecipient(
            RegisterTransferRecipientRequest request) throws TransferExecutionException {
        try {
            return makeMenuItemRequest(
                    SwedbankBaseConstants.MenuItemKey.REGISTER_EXTERNAL_TRANSFER_RECIPIENT,
                    request,
                    RegisterTransferRecipientResponse.class);
        } catch (HttpResponseException hre) {
            if (SwedbankApiErrors.isAccountNumberInvalid(hre)) {
                throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setEndUserMessage(
                                TransferExecutionException.EndUserMessage.INVALID_DESTINATION)
                        .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_DESTINATION)
                        .setException(hre)
                        .build();
            }

            // unknown error: rethrow
            throw hre;
        }
    }

    public RegisterTransferResponse registerTransfer(
            double amount,
            String destinationAccountId,
            String remittanceInformationValue,
            String sourceAccountId,
            Date transferDueDate) {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.REGISTER_TRANSFER,
                RegisterTransferRequest.create(
                        amount,
                        destinationAccountId,
                        remittanceInformationValue,
                        sourceAccountId,
                        transferDueDate),
                RegisterTransferResponse.class);
    }

    public RegisterTransferResponse registerPayment(
            double amount,
            RemittanceInformation remittanceInformation,
            Date date,
            String destinationAccountId,
            String sourceAccountId) {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.REGISTER_PAYMENT,
                RegisterPaymentRequest.createPayment(
                        amount, remittanceInformation, date, destinationAccountId, sourceAccountId),
                RegisterTransferResponse.class);
    }

    public RegisterTransferResponse registerEInvoice(
            double amount,
            RemittanceInformation remittanceInformation,
            Date date,
            String eInvoiceId,
            String destinationAccountId,
            String sourceAccountId) {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.REGISTER_PAYMENT,
                RegisterPaymentRequest.createEinvoicePayment(
                        amount,
                        remittanceInformation,
                        date,
                        destinationAccountId,
                        sourceAccountId,
                        eInvoiceId),
                RegisterTransferResponse.class);
    }

    public RegisteredTransfersResponse registeredTransfers() {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.PAYMENT_REGISTERED,
                RegisteredTransfersResponse.class);
    }

    public RegisteredTransfersResponse registeredTransfers(LinkEntity linkEntity) {
        return makeRequest(linkEntity, RegisteredTransfersResponse.class, false);
    }

    public ConfirmTransferResponse confirmTransfer(LinkEntity linkEntity) {
        return makeRequest(linkEntity, ConfirmTransferResponse.class, false);
    }

    public PaymentsConfirmedResponse paymentsConfirmed() {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.PAYMENTS_CONFIRMED,
                PaymentsConfirmedResponse.class);
    }

    public HttpResponse deleteTransfer(LinkEntity linkEntity) {
        return makeRequest(linkEntity, HttpResponse.class, false);
    }

    public PaymentDetailsResponse paymentDetails(LinkEntity linkEntity) {
        return makeRequest(linkEntity, PaymentDetailsResponse.class, false);
    }

    public RegisterTransferResponse updatePayment(
            LinkEntity linkEntity,
            double amount,
            RemittanceInformation remittanceInformation,
            Date date,
            String recipientId,
            String fromAccountId) {
        return makeRequest(
                linkEntity,
                RegisterPaymentRequest.createPayment(
                        amount, remittanceInformation, date, recipientId, fromAccountId),
                RegisterTransferResponse.class,
                false,
                Retry.FIRST_ATTEMPT);
    }

    public InitiateSignTransferResponse signExternalTransferBankId(LinkEntity linkEntity) {
        return makeRequest(
                linkEntity,
                InitiateSignTransferRequest.create(),
                InitiateSignTransferResponse.class,
                false,
                Retry.FIRST_ATTEMPT);
    }

    public InitiateSecurityTokenSignTransferResponse signExternalTransferSecurityToken(
            LinkEntity linkEntity) {
        return makeRequest(linkEntity, InitiateSecurityTokenSignTransferResponse.class, false);
    }

    public String getQrCodeImageAsBase64EncodedString(final LinkEntity linkEntity) {
        final HttpResponse response = makeGetRequest(linkEntity, false);
        try {
            byte[] bytes = IOUtils.toByteArray(response.getBodyInputStream());
            return Base64.encodeBase64String(bytes);
        } catch (IOException e) {
            log.warn("Could not download QR code.", e);
            throw new IllegalStateException(e);
        }
    }

    public CollectBankIdSignResponse collectSignBankId(LinkEntity linkEntity) {
        return makeRequest(linkEntity, CollectBankIdSignResponse.class, false);
    }

    public TouchResponse touch() {
        return makeGetRequest(SwedbankBaseConstants.Url.TOUCH.get(), TouchResponse.class);
    }

    public boolean logout() {
        try {
            HttpResponse response =
                    makePutRequest(
                            SwedbankBaseConstants.Url.LOGOUT.get(), null, HttpResponse.class);
            return response.getStatus() == HttpStatus.SC_OK;
        } catch (Exception e) {
            return false;
        }
    }

    private <T> T makeMenuItemRequest(
            SwedbankBaseConstants.MenuItemKey menuItemKey, Class<T> responseClass) {
        return makeMenuItemRequest(menuItemKey, null, responseClass);
    }

    private <T> T makeMenuItemRequest(
            SwedbankBaseConstants.MenuItemKey menuItemKey,
            Object requestObject,
            Class<T> responseClass) {
        return makeMenuItemRequest(
                menuItemKey, requestObject, responseClass, Collections.emptyMap());
    }

    private <T> T makeMenuItemRequest(
            SwedbankBaseConstants.MenuItemKey menuItemKey,
            Object requestObject,
            Class<T> responseClass,
            Map<String, String> parameters) {

        Map<String, MenuItemLinkEntity> menuItems = bankProfileHandler.getMenuItems();

        if (!bankProfileHandler.isAuthorizedForAction(menuItemKey)) {
            MenuItemLinkEntity menuItem = menuItems.get(menuItemKey.getKey());
            log.warn(
                    "User not authorized to perform request with key: [{}], name: [{}], authorization: [{}]",
                    menuItemKey,
                    menuItem.getName(),
                    menuItem.getAuthorization());
            throw new IllegalStateException();
        }

        return makeRequest(
                menuItems.get(menuItemKey.getKey()), requestObject, responseClass, parameters);
    }

    private void ensureAuthorizationHeaderIsSet() {
        if (client.isPersistentHeaderPresent(SwedbankBaseConstants.Headers.AUTHORIZATION_KEY)) {
            return;
        }

        client.addPersistentHeader(
                SwedbankBaseConstants.Headers.AUTHORIZATION_KEY,
                SwedbankBaseConstants.generateAuthorization(configuration, username));
    }

    public List<BankProfile> getBankProfiles() {
        return getBankProfileHandler().getBankProfiles();
    }

    public BankProfile selectProfile(BankProfile requestedBankProfile) {
        BankProfile activeBankProfile = getBankProfileHandler().getActiveBankProfile();
        // check if we are active
        if (activeBankProfile != null
                && requestedBankProfile
                        .getBank()
                        .getBankId()
                        .equalsIgnoreCase(activeBankProfile.getBank().getBankId())) {
            return activeBankProfile;
        }

        BankProfile foundBankProfile = getBankProfileHandler().findProfile(requestedBankProfile);

        return activateProfile(foundBankProfile);
    }

    public void selectTransferProfile() {
        BankProfile transferProfile = getBankProfileHandler().findTransferProfile();

        selectProfile(transferProfile);
    }

    // activate a profile at backend
    private BankProfile activateProfile(BankProfile bankProfile) {

        Map<String, MenuItemLinkEntity> profileMenuItems =
                fetchProfile(bankProfile.getBank().getProfile().getLinks().getNextOrThrow());

        getBankProfileHandler().setActiveBankProfile(bankProfile);

        return getBankProfileHandler().getActiveBankProfile();
    }

    // setup and store bank profiles
    private void setupProfiles(ProfileResponse profileResponse) {
        bankProfileHandler = new BankProfileHandler();

        for (BankEntity bank : profileResponse.getBanks()) {
            bank.setOrgNumber(organizationNumber);
            // fetch all profile details
            Map<String, MenuItemLinkEntity> menuItems =
                    fetchProfile(bank.getProfile().getLinks().getNextOrThrow());
            bankProfileHandler.setMenuItems(menuItems);
            EngagementOverviewResponse engagementOverViewResponse = fetchEngagementOverview();
            PaymentBaseinfoResponse paymentBaseinfoResponse = fetchPaymentBaseinfo();
            // create and add profile
            BankProfile bankProfile =
                    new BankProfile(
                            bank, menuItems, engagementOverViewResponse, paymentBaseinfoResponse);
            bankProfileHandler.addBankProfile(bankProfile);
            // profile is already activated
            bankProfileHandler.setActiveBankProfile(bankProfile);
        }

        swedbankStorage.setBankProfileHandler(bankProfileHandler);
    }

    private BankProfileHandler getBankProfileHandler() {
        if (bankProfileHandler == null) {
            bankProfileHandler = swedbankStorage.getBankProfileHandler();
            bankProfileHandler.setActiveBankProfile(bankProfileHandler.findTransferProfile());
        }

        return bankProfileHandler;
    }

    private Map<String, MenuItemLinkEntity> fetchProfile(LinkEntity linkEntity) {
        SelectedProfileResponse selectedProfileResponse =
                makeRequest(linkEntity, SelectedProfileResponse.class, false);

        Map<String, MenuItemLinkEntity> menuItemsMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        menuItemsMap.putAll(
                Optional.ofNullable(selectedProfileResponse.getMenuItems())
                        .orElseThrow(IllegalStateException::new));

        return menuItemsMap;
    }

    private EngagementOverviewResponse fetchEngagementOverview() {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.ACCOUNTS, EngagementOverviewResponse.class);
    }

    private PaymentBaseinfoResponse fetchPaymentBaseinfo() {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.PAYMENT_BASEINFO, PaymentBaseinfoResponse.class);
    }

    private boolean hasValidProfile(ProfileResponse profileResponse) {
        boolean hasValidBank =
                configuration.isSavingsBank()
                        ? profileResponse.isHasSavingbankProfile()
                        : profileResponse.isHasSwedbankProfile();

        return hasValidBank && profileResponse.getBanks().size() > 0;
    }

    private boolean modeAndProfileMismatch(ProfileResponse profileResponse) {
        if (!configuration.isSavingsBank()) {
            return !profileResponse.isHasSwedbankProfile()
                    && profileResponse.isHasSavingbankProfile();
        } else {
            return !profileResponse.isHasSavingbankProfile()
                    && profileResponse.isHasSwedbankProfile();
        }
    }

    public InitSecurityTokenChallengeResponse initTokenGenerator(String ssn) {
        try {
            return makePostRequest(
                    SwedbankBaseConstants.Url.INIT_TOKEN.get(),
                    InitAuthenticationRequest.createFromUserId(ssn),
                    InitSecurityTokenChallengeResponse.class);
        } catch (HttpClientException hce) {
            String errorMessage = Strings.nullToEmpty(hce.getMessage()).toLowerCase();
            if (errorMessage.contains(SwedbankBaseConstants.ErrorMessage.CONNECT_TIMEOUT)) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(hce);
            }

            throw hce;
        }
    }

    public SecurityTokenChallengeResponse sendLoginChallenge(LinksEntity links, String challenge)
            throws SupplementalInfoException {
        try {
            return makeRequest(
                    links.getNext(),
                    SecurityTokenChallengeRequest.createFromChallenge(challenge),
                    SecurityTokenChallengeResponse.class,
                    false,
                    Retry.FIRST_ATTEMPT);
        } catch (HttpResponseException hre) {
            SwedbankApiErrors.handleTokenErrors(hre);
            // unknown error: rethrow
            throw hre;
        } catch (HttpClientException hce) {
            String errorMessage = Strings.nullToEmpty(hce.getMessage()).toLowerCase();
            if (errorMessage.contains(SwedbankBaseConstants.ErrorMessage.CONNECT_TIMEOUT)) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(hce);
            }
            throw hce;
        }
    }

    public RegisterTransferResponse sendTransferChallenge(LinkEntity linkEntity, String challenge)
            throws SupplementalInfoException {
        try {
            return makeRequest(
                    linkEntity,
                    SecurityTokenChallengeRequest.createFromChallenge(challenge),
                    RegisterTransferResponse.class,
                    false,
                    Retry.FIRST_ATTEMPT);
        } catch (HttpResponseException hre) {
            SwedbankApiErrors.handleTokenErrors(hre);
            // unknown error: rethrow
            throw hre;
        } catch (HttpClientException hce) {
            String errorMessage = Strings.nullToEmpty(hce.getMessage()).toLowerCase();
            if (errorMessage.contains(SwedbankBaseConstants.ErrorMessage.CONNECT_TIMEOUT)) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(hce);
            }

            throw hce;
        }
    }

    public RegisterTransferResponse sendNewRecipientChallenge(
            LinkEntity linkEntity, String challenge) throws SupplementalInfoException {
        try {
            return makeRequest(
                    linkEntity,
                    SecurityTokenChallengeRequest.createFromChallenge(challenge),
                    RegisterTransferResponse.class,
                    false,
                    Retry.FIRST_ATTEMPT);
        } catch (HttpResponseException hre) {
            SwedbankApiErrors.handleTokenErrors(hre);
            // unknown error: rethrow
            throw hre;
        } catch (HttpClientException hce) {
            String errorMessage = Strings.nullToEmpty(hce.getMessage()).toLowerCase();
            if (errorMessage.contains(SwedbankBaseConstants.ErrorMessage.CONNECT_TIMEOUT)) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(hce);
            }

            throw hce;
        }
    }
}
