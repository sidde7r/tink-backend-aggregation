package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider;

import static se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants.SAVINGSBANKLIST;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants.BankName;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc.LoanOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.MenuItemKey;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.Retry;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.RetryFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.InitAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.InitSecurityTokenChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.SecurityTokenChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.SecurityTokenChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.payment.rpc.RegisterPayeeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.payment.rpc.RegisterPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.payment.rpc.RegisterRecipientResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.CollectBankIdSignResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.InitiateSecurityTokenSignTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.InitiateSignTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.InitiateSignTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.PaymentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.PaymentsConfirmedResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.RegisterTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.RegisteredTransfersResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.transfer.rpc.RegisterTransferRecipientRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.transfer.rpc.RegisterTransferRecipientResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.transfer.rpc.RegisterTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.creditcard.rpc.DetailedCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.filters.SwedbankBaseHttpFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.filters.SwedbankHourRateLimitFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.filters.SwedbankParallelRateLimitFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.filters.SwedbankServiceUnavailableFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.profile.SwedbankProfileSelector;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfileHandler;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.EngagementTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.MenuItemLinkEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.PrivateProfileEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.SelectedProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.TouchResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@Slf4j
public class SwedbankDefaultApiClient {
    @Getter private final String host;
    protected final TinkHttpClient client;
    private final SwedbankConfiguration configuration;
    private final SwedbankProfileSelector profileSelector;
    private final String username;
    private final SwedbankStorage swedbankStorage;
    // only use cached menu items for a profile
    private BankProfileHandler bankProfileHandler;
    @Getter private final boolean refreshOnlyLoanData;

    private enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }

    protected SwedbankDefaultApiClient(
            TinkHttpClient client,
            SwedbankConfiguration configuration,
            SwedbankStorage swedbankStorage,
            SwedbankProfileSelector profileSelector,
            AgentComponentProvider componentProvider) {
        this.client = client;
        this.configuration = configuration;
        this.profileSelector = profileSelector;
        this.username =
                componentProvider.getCredentialsRequest().getCredentials().getField(Key.USERNAME);
        this.swedbankStorage = swedbankStorage;
        this.host = configuration.getHost();
        configureHttpClient();
        this.refreshOnlyLoanData = checkIfRestrictedToLoanData(componentProvider);
    }

    private void configureHttpClient() {
        final String userAgent = configuration.getUserAgent();
        if (!Strings.isNullOrEmpty(userAgent)) {
            client.setUserAgent(userAgent);
        }

        client.addFilter(
                new SwedbankBaseHttpFilter(
                        SwedbankBaseConstants.generateAuthorization(configuration, username)));
        client.addFilter(
                new TimeoutRetryFilter(
                        RetryFilter.NUM_TIMEOUT_RETRIES,
                        RetryFilter.TIMEOUT_RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new SwedbankServiceUnavailableFilter());
        client.addFilter(new SwedbankHourRateLimitFilter());
        client.addFilter(
                new SwedbankParallelRateLimitFilter(
                        RetryFilter.NUM_TIMEOUT_RETRIES,
                        RetryFilter.TIMEOUT_RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new TimeoutFilter());
    }

    protected <T> T makeGetRequest(URL url, Class<T> responseClass, boolean retry) {
        return makeRequest(url, null, responseClass, Method.GET, retry);
    }

    protected HttpResponse makeGetRequest(LinkEntity linkEntity, boolean retry) {
        URL url = SwedbankBaseConstants.Url.createDynamicUrl(host, linkEntity.getUri());
        return makeGetRequest(url, HttpResponse.class, retry);
    }

    protected <T> T makePostRequest(
            URL url, Object requestObject, Class<T> responseClass, boolean retry) {
        return makeRequest(url, requestObject, responseClass, Method.POST, retry);
    }

    protected <T> T makePutRequest(
            URL url, Object requestObject, Class<T> responseClass, boolean retry) {
        return makeRequest(url, requestObject, responseClass, Method.PUT, retry);
    }

    protected <T> T makeRequest(LinkEntity linkEntity, Class<T> responseClass, boolean retry) {
        return makeRequest(linkEntity, null, responseClass, retry);
    }

    protected <T> T makeRequest(
            LinkEntity linkEntity, Object requestObject, Class<T> responseClass, boolean retry) {
        return makeRequest(linkEntity, Collections.emptyMap(), requestObject, responseClass, retry);
    }

    protected <T> T makeRequest(
            LinkEntity linkEntity,
            Map<String, String> queryParams,
            Object requestObject,
            Class<T> responseClass,
            boolean retry) {
        Method method = Enum.valueOf(Method.class, linkEntity.getMethodValue().toString());
        Preconditions.checkState(
                linkEntity.isValid(),
                "Create dynamic request failed - Cannot proceed without valid link entity - Method:[{}], Uri:[{}]",
                method,
                linkEntity.getUri());

        URL url =
                SwedbankBaseConstants.Url.createDynamicUrl(host, linkEntity.getUri(), queryParams);
        return makeRequest(url, requestObject, responseClass, method, retry);
    }

    protected <T> T makeRequest(
            URL url, Object requestObject, Class<T> responseClass, Method method, boolean retry) {
        return makeRequest(url, requestObject, responseClass, method, retry, Retry.FIRST_ATTEMPT);
    }

    protected <T> T makeRequest(
            URL url,
            Object requestObject,
            Class<T> responseClass,
            Method method,
            boolean retry,
            int attempt) {
        try {
            return executeRequest(url, method, requestObject, responseClass);
        } catch (HttpResponseException hre) {
            if (SwedbankApiErrors.isSessionTerminated(hre)) {
                throw BankServiceError.SESSION_TERMINATED.exception(hre);
            } else if (SwedbankApiErrors.isAppTooOld(hre)) {
                // see SwedbankSEConstants
                throw new IllegalStateException("App too old, update API keys.");
            }
            SwedbankApiErrors.handleTokenErrors(hre);
            if (shouldRetry(retry, attempt, hre)) {
                log.info("SwedbankDefaultApiClient: Retrying operation to {}", url, hre);
                return makeRequest(url, requestObject, responseClass, method, true, ++attempt);
            } else {
                SwedbankApiErrors.throwIfPaymentOrTransferAlreadyExists(hre);
                throw hre;
            }
        }
    }

    private boolean shouldRetry(boolean retry, int attempt, HttpResponseException hre) {
        return retry
                && hre.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                && attempt <= Retry.MAX_RETRY_ATTEMPTS;
    }

    private <T> T executeRequest(
            URL url, Method method, Object requestObject, Class<T> responseClass) {
        RequestBuilder requestBuilder = client.request(url);
        switch (method) {
            case GET:
                return requestBuilder.get(responseClass);
            case POST:
                return requestBuilder.post(responseClass, requestObject);
            case PUT:
                return requestBuilder.put(responseClass, requestObject);
            case DELETE:
                return requestBuilder.delete(responseClass, requestObject);
            default:
                log.warn("SwedbankDefaultApiClient: Invalid method - Method:[{}]", method);
                throw new IllegalStateException();
        }
    }

    public InitBankIdResponse initBankId(boolean bankIdOnSameDevice) {
        try {
            return makePostRequest(
                    SwedbankBaseConstants.Url.INIT_BANKID.get(host),
                    InitBankIdRequest.create(bankIdOnSameDevice),
                    InitBankIdResponse.class,
                    true);
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
            throwNotCustomerException(profileResponse);
        }

        setupProfiles(profileResponse);

        return profileResponse;
    }

    private void throwNotCustomerException(ProfileResponse profileResponse) {
        if (configuration.getName().contains(BankName.FALLBACK)) {
            throwFallbackUserError(profileResponse);
        }
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

    private void throwFallbackUserError(ProfileResponse profileResponse) {
        boolean hasValidProfileSwedbank = false;
        boolean hasValidProfileSparbank = false;
        List<PrivateProfileEntity> privateProfileEntityList = getPrivateProfiles(profileResponse);

        for (PrivateProfileEntity profileEntity : privateProfileEntityList) {
            if (isValidSwedbankProfile(profileEntity)) {
                hasValidProfileSwedbank = true;
            }
            if (isValidSavingsbankProfile(profileEntity)) {
                hasValidProfileSparbank = true;
            }
        }

        if (configuration.isSavingsBank() && hasValidProfileSwedbank && !hasValidProfileSparbank) {
            throw LoginError.NOT_CUSTOMER.exception(
                    SwedbankBaseConstants.UserMessage.WRONG_BANK_SAVINGSBANK);

        } else if (configuration.isSwedbank()
                && !hasValidProfileSwedbank
                && hasValidProfileSparbank) {
            throw LoginError.NOT_CUSTOMER.exception(
                    SwedbankBaseConstants.UserMessage.WRONG_BANK_SWEDBANK);
        }
        // In case of only servicePortalProfile
        throw LoginError.NOT_CUSTOMER.exception();
    }

    private boolean isValidSwedbankProfile(PrivateProfileEntity profileEntity) {
        return profileEntity.getBankName().toLowerCase().contains(BankName.SWEDBANK);
    }

    private boolean isValidSavingsbankProfile(PrivateProfileEntity profileEntity) {
        return profileEntity.getBankName().toLowerCase().contains(BankName.SAVINGSBANK)
                || profileEntity.getBankName().toLowerCase().contains(BankName.SPARBANK)
                || profileEntity.getBankName().toLowerCase().contains(BankName.OLAND);
    }

    private List<PrivateProfileEntity> getPrivateProfiles(ProfileResponse profileResponse) {
        return profileResponse.getBanks().stream()
                .filter(bankEntity -> Objects.nonNull(bankEntity.getPrivateProfile()))
                .map(BankEntity::getPrivateProfile)
                .collect(Collectors.toList());
    }

    public EngagementTransactionsResponse engagementTransactions(LinkEntity linkEntity) {
        return makeRequest(linkEntity, EngagementTransactionsResponse.class, true);
    }

    public LoanOverviewResponse loanOverview() {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.LOANS, LoanOverviewResponse.class);
    }

    public LoanDetailsResponse loanDetails(LinkEntity linkEntity) {
        return makeRequest(linkEntity, LoanDetailsResponse.class, true);
    }

    public DetailedCardAccountResponse cardAccountDetails(LinkEntity linkEntity) {
        return makeRequest(linkEntity, DetailedCardAccountResponse.class, true);
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
                throwInvalidDestinationException(hre);
            }

            // unknown error: rethrow
            throw hre;
        }
    }

    private void throwInvalidDestinationException(HttpResponseException hre) {
        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_DESTINATION)
                .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_DESTINATION)
                .setInternalStatus(InternalStatus.INVALID_DESTINATION_ACCOUNT.toString())
                .setException(hre)
                .build();
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

    public Optional<PaymentsConfirmedResponse> paymentsConfirmed() {
        Map<String, MenuItemLinkEntity> menuItems = bankProfileHandler.getMenuItems();

        if (!menuItems.containsKey(SwedbankBaseConstants.MenuItemKey.PAYMENTS_CONFIRMED.getKey())) {
            return Optional.empty();
        }

        return Optional.of(
                makeMenuItemRequest(
                        SwedbankBaseConstants.MenuItemKey.PAYMENTS_CONFIRMED,
                        PaymentsConfirmedResponse.class));
    }

    public HttpResponse deleteTransfer(LinkEntity linkEntity) {
        return makeRequest(linkEntity, HttpResponse.class, false);
    }

    public PaymentDetailsResponse paymentDetails(LinkEntity linkEntity) {
        return makeRequest(linkEntity, PaymentDetailsResponse.class, false);
    }

    public InitiateSignTransferResponse signExternalTransferBankId(LinkEntity linkEntity) {
        return makeRequest(
                linkEntity,
                InitiateSignTransferRequest.create(),
                InitiateSignTransferResponse.class,
                false);
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
        return makeGetRequest(
                SwedbankBaseConstants.Url.TOUCH.get(host), TouchResponse.class, false);
    }

    public boolean logout() {
        try {
            HttpResponse response =
                    makePutRequest(
                            SwedbankBaseConstants.Url.LOGOUT.get(host),
                            null,
                            HttpResponse.class,
                            false);
            return response.getStatus() == HttpStatus.SC_OK;
        } catch (Exception e) {
            return false;
        }
    }

    protected <T> T makeMenuItemRequest(
            SwedbankBaseConstants.MenuItemKey menuItemKey, Class<T> responseClass) {
        return makeMenuItemRequest(menuItemKey, null, responseClass);
    }

    protected <T> T makeMenuItemRequest(
            SwedbankBaseConstants.MenuItemKey menuItemKey,
            Object requestObject,
            Class<T> responseClass) {
        return makeMenuItemRequest(
                menuItemKey, requestObject, responseClass, Collections.emptyMap());
    }

    protected <T> T makeMenuItemRequest(
            SwedbankBaseConstants.MenuItemKey menuItemKey,
            Object requestObject,
            Class<T> responseClass,
            Map<String, String> parameters) {

        Map<String, MenuItemLinkEntity> menuItems = bankProfileHandler.getMenuItems();

        if (!bankProfileHandler.isAuthorizedForAction(menuItemKey)) {
            throwNotAuthorizedForActionException(menuItemKey, menuItems);
        }

        return makeRequest(
                menuItems.get(menuItemKey.getKey()),
                parameters,
                requestObject,
                responseClass,
                false);
    }

    private void throwNotAuthorizedForActionException(
            SwedbankBaseConstants.MenuItemKey menuItemKey,
            Map<String, MenuItemLinkEntity> menuItems) {
        MenuItemLinkEntity menuItem = menuItems.get(menuItemKey.getKey());
        log.warn(
                "User not authorized to perform request with key: [{}], name: [{}], authorization: [{}]",
                menuItemKey,
                menuItem == null ? "" : menuItem.getName(),
                menuItem == null ? "" : menuItem.getAuthorization());
        throw new IllegalStateException();
    }

    public List<BankProfile> getBankProfiles() {
        return getBankProfileHandler().getBankProfiles();
    }

    public BankProfile selectProfile(BankProfile requestedBankProfile) {
        BankProfile activeBankProfile = getBankProfileHandler().getActiveBankProfile();
        // check if we are active
        if (activeBankProfile != null && isSameProfile(requestedBankProfile, activeBankProfile)) {
            return activeBankProfile;
        }

        BankProfile foundBankProfile = getBankProfileHandler().findProfile(requestedBankProfile);

        return activateProfile(foundBankProfile);
    }

    private boolean isSameProfile(BankProfile requestedBankProfile, BankProfile activeBankProfile) {
        return requestedBankProfile
                .getBank()
                .getBankId()
                .equalsIgnoreCase(activeBankProfile.getBank().getBankId());
    }

    // activate a profile at backend
    private BankProfile activateProfile(BankProfile bankProfile) {

        Map<String, MenuItemLinkEntity> profileMenuItems =
                fetchProfile(bankProfile.getProfile().getLinks().getNextOrThrow());
        getBankProfileHandler().setMenuItems(profileMenuItems);
        getBankProfileHandler().setActiveBankProfile(bankProfile);
        swedbankStorage.setBankProfileHandler(getBankProfileHandler());
        return getBankProfileHandler().getActiveBankProfile();
    }

    // setup and store bank profiles
    private void setupProfiles(ProfileResponse profileResponse) {
        bankProfileHandler = new BankProfileHandler();

        // delegate profile selection
        for (Pair<BankEntity, ProfileEntity> pair :
                profileSelector.selectBankProfiles(profileResponse.getBanks())) {

            // we only create profiles for the bank that the user selected
            if ((configuration.isSavingsBank() && pair.first.isSavingsBank())
                    || (configuration.isSwedbank() && pair.first.isSwedbank())) {
                createAndAddProfileToHandler(pair.first, pair.second);
            } else {
                log.info(
                        "Skipping creation of profile for {} bank since user selected another one",
                        pair.first.getName());
            }
        }

        swedbankStorage.setBankProfileHandler(bankProfileHandler);
    }

    private void createAndAddProfileToHandler(BankEntity bank, ProfileEntity profileEntity) {
        // fetch all profile details
        Map<String, MenuItemLinkEntity> menuItems =
                fetchProfile(profileEntity.getLinks().getNextOrThrow());
        bankProfileHandler.setMenuItems(menuItems);
        EngagementOverviewResponse engagementOverViewResponse =
                refreshOnlyLoanData ? null : fetchEngagementOverview();
        PaymentBaseinfoResponse paymentBaseinfoResponse = getPaymentBaseinfoResponse();

        // create and add profile
        BankProfile bankProfile =
                new BankProfile(
                        bank,
                        menuItems,
                        engagementOverViewResponse,
                        paymentBaseinfoResponse,
                        profileEntity.getId());
        bankProfileHandler.addBankProfile(bankProfile);
        // profile is already activated
        bankProfileHandler.setActiveBankProfile(bankProfile);

        // temporary log to track youthProfile
        if (profileEntity.isYouthProfile()) {
            log.info("This profile is youthProfile");
        }
    }

    private PaymentBaseinfoResponse getPaymentBaseinfoResponse() {
        if (refreshOnlyLoanData) {
            return null;
        }
        // there is some limitation for Youth profile, sometimes PAYMENT_BASEINFO is absent. Since
        // it need during creating the profile we skip it to avoid illegal state exception
        if (configuration.hasPayments()
                && bankProfileHandler.isAuthorizedForAction(MenuItemKey.PAYMENT_BASEINFO)) {
            return fetchPaymentBaseinfo();
        } else {
            return null;
        }
    }

    public BankProfileHandler getBankProfileHandler() {
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
        if (configuration.getName().contains(BankName.FALLBACK)) {
            for (BankEntity bankEntity : profileResponse.getBanks()) {
                if (hasValidPrivateProfileForChosenBank(bankEntity)) {
                    return true;
                }
            }
            return false;
        }

        boolean hasValidBank =
                configuration.isSavingsBank()
                        ? profileResponse.isHasSavingbankProfile()
                        : profileResponse.isHasSwedbankProfile();

        return hasValidBank && profileResponse.getBanks().size() > 0;
    }

    private boolean hasValidPrivateProfileForChosenBank(BankEntity bankEntity) {
        return profileBankMatchesChosenBank(bankEntity.getName().toLowerCase())
                && bankEntity.getPrivateProfile() != null;
    }

    private boolean profileBankMatchesChosenBank(String profileBank) {
        List<String> chosenBanks = new ArrayList<>();
        if (configurationNameContains(BankName.SWEDBANK)) {
            chosenBanks.add(BankName.SWEDBANK);
        }
        if (configurationNameContains(BankName.SAVINGSBANK)) {
            chosenBanks.addAll(SAVINGSBANKLIST);
        }
        return chosenBanks.stream().anyMatch(profileBank::contains);
    }

    private boolean configurationNameContains(String bankName) {
        return configuration.getName().toLowerCase().contains(bankName);
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
                    SwedbankBaseConstants.Url.INIT_TOKEN.get(host),
                    InitAuthenticationRequest.createFromUserId(ssn),
                    InitSecurityTokenChallengeResponse.class,
                    true);
        } catch (HttpClientException hce) {
            String errorMessage = Strings.nullToEmpty(hce.getMessage()).toLowerCase();
            if (errorMessage.contains(SwedbankBaseConstants.ErrorMessage.CONNECT_TIMEOUT)) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(hce);
            }

            throw hce;
        }
    }

    public SecurityTokenChallengeResponse sendLoginTokenChallengeResponse(
            LinkEntity linkEntity, String challengeResponse)
            throws SupplementalInfoException, LoginException {
        try {
            return sendTokenChallengeResponse(
                    linkEntity, challengeResponse, SecurityTokenChallengeResponse.class);
        } catch (HttpResponseException hre) {
            if (SwedbankApiErrors.isLoginSecurityTokenInvalid(hre)) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(hre);
            }

            throw hre;
        }
    }

    public <T> T sendTokenChallengeResponse(
            LinkEntity linkEntity, String challengeResponse, Class<T> responseClass)
            throws SupplementalInfoException {
        try {
            return makeRequest(
                    linkEntity,
                    SecurityTokenChallengeRequest.createFromChallengeResponse(challengeResponse),
                    responseClass,
                    true);
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

    private boolean checkIfRestrictedToLoanData(AgentComponentProvider componentProvider) {
        CredentialsRequest credentialsRequest = componentProvider.getCredentialsRequest();
        if (credentialsRequest instanceof RefreshInformationRequest) {
            RefreshInformationRequest refreshInformationRequest =
                    (RefreshInformationRequest) credentialsRequest;
            Set<RefreshableItem> itemsToRefresh = refreshInformationRequest.getItemsToRefresh();

            return itemsToRefresh != null && isOnlyLoanItems(itemsToRefresh);
        }
        return false;
    }

    private boolean isOnlyLoanItems(Set<RefreshableItem> itemsToRefresh) {
        boolean isOneItemLoanAccount =
                itemsToRefresh.size() == 1
                        && itemsToRefresh.contains(RefreshableItem.LOAN_ACCOUNTS);

        boolean isTwoItemsLoanTransactionAndLoanAccount =
                itemsToRefresh.size() == 2
                        && itemsToRefresh.contains(RefreshableItem.LOAN_ACCOUNTS)
                        && itemsToRefresh.contains(RefreshableItem.LOAN_TRANSACTIONS);

        return isOneItemLoanAccount || isTwoItemsLoanTransactionAndLoanAccount;
    }
}
