package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Optional;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.LoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.MobileHelloResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.PrepareEnrollResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.AppCredentialsBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.AppCredentialsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.BaseBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ConfirmEnrollRequestBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.InitEnrollRequestBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.LoginRequestBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.MobileHelloRequestBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.MobileHelloResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.PrepareEnrollResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.TrustBuilderRequestBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.BaseMobileResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.entities.ValidateExternalTransferResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc.ExecuteExternalTransferBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc.ExecuteInternalTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc.ValidateInternalTransferBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc.ValidateInternalTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc.ValidateThirdPartyTransferBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc.ValidateThirdPartyTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc.ValidateTrustedTransferBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc.ValidateTrustedTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.PendingPaymentsResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc.PendingPaymentsRequestBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc.PendingPaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc.TransactionsRequestBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.TrustedBeneficiariesResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class IngApiClient {
    private final TinkHttpClient client;

    public IngApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public MobileHelloResponseEntity mobileHello() {
        MobileHelloRequestBody mobileHelloRequestBody = new MobileHelloRequestBody();
        URL url = getUrlWithQueryParams(IngConstants.Urls.MOBILE_HELLO);

        return this.client
                .request(url)
                .post(MobileHelloResponse.class, mobileHelloRequestBody)
                .getMobileResponse();
    }

    public void trustBuilderEnroll(
            String url, String username, String cardNumber, String otp, String deviceId) {
        TrustBuilderRequestBody trustBuilderRequestBody =
                new TrustBuilderRequestBody(username, cardNumber, otp, deviceId, "", true);
        URL trustBuilderUrl = getUrlWithQueryParams(new URL(IngConstants.Urls.HOST + url));

        this.client.request(trustBuilderUrl).post(HttpResponse.class, trustBuilderRequestBody);
    }

    public HttpResponse trustBuilderLogin(
            String url,
            String ingId,
            String virtualCardNumber,
            int otp,
            String deviceId,
            String psn) {
        TrustBuilderRequestBody trustBuilderRequestBody =
                new TrustBuilderRequestBody(
                        ingId, virtualCardNumber, Integer.toString(otp), deviceId, psn, false);
        URL trustBuilderUrl = getUrlWithQueryParams(new URL(IngConstants.Urls.HOST + url));

        return this.client
                .request(trustBuilderUrl)
                .post(HttpResponse.class, trustBuilderRequestBody);
    }

    public HttpResponse initEnroll(
            String url, String username, String cardNumber, String deviceId) {
        InitEnrollRequestBody initEnrollRequestBody =
                new InitEnrollRequestBody(username, cardNumber, deviceId);
        URL initEnrollUrl = getUrlWithQueryParams(new URL(IngConstants.Urls.HOST + url));

        return this.client.request(initEnrollUrl).post(HttpResponse.class, initEnrollRequestBody);
    }

    public AppCredentialsResponse getAppCredentials(String url, byte[] encryptedQueryData) {
        AppCredentialsBody appCredentialsBody = new AppCredentialsBody(encryptedQueryData);
        URL getAppCredentialsUrl = new URL(IngConstants.Urls.HOST + url);

        return this.client
                .request(getAppCredentialsUrl)
                .post(AppCredentialsResponse.class, appCredentialsBody);
    }

    public PrepareEnrollResponseEntity prepareEnroll(String url) {
        BaseBody prepareEnrollBody = new BaseBody();
        URL prepareEnrollUrl = getUrlWithQueryParams(new URL(IngConstants.Urls.HOST + url));

        return this.client
                .request(prepareEnrollUrl)
                .post(PrepareEnrollResponse.class, prepareEnrollBody)
                .getMobileResponse();
    }

    public BaseMobileResponseEntity confirmEnroll(
            String url,
            String ingId,
            String signingId,
            String challengeResponse,
            int otpSystem,
            String deviceId) {
        ConfirmEnrollRequestBody confirmEnrollRequestBody =
                new ConfirmEnrollRequestBody(
                        ingId, signingId, challengeResponse, Integer.toString(otpSystem), deviceId);
        URL confirmEnrollUrl = getUrlWithQueryParams(new URL(IngConstants.Urls.HOST + url));

        return this.client
                .request(confirmEnrollUrl)
                .post(BaseResponse.class, confirmEnrollRequestBody)
                .getMobileResponse();
    }

    public void logout(String url) {
        BaseBody logoutRequestBody = new BaseBody();
        URL logoutUrl = getUrlWithQueryParams(new URL(IngConstants.Urls.HOST + url));

        this.client.request(logoutUrl).post(HttpResponse.class, logoutRequestBody);
    }

    public LoginResponseEntity login(
            String url, String ingId, String virtualCardNumber, String deviceId) {
        LoginRequestBody loginRequestBody =
                new LoginRequestBody(ingId, virtualCardNumber, deviceId);
        URL loginUrl = getUrlWithQueryParams(new URL(IngConstants.Urls.HOST + url));

        return this.client
                .request(loginUrl)
                .post(LoginResponse.class, loginRequestBody)
                .getMobileResponse();
    }

    public HttpResponse getMenuItems() {
        return this.client.request(IngConstants.Urls.MENU_ITEMS).get(HttpResponse.class);
    }

    public Optional<AccountsResponse> fetchAccounts(LoginResponseEntity loginResponse) {
        return loginResponse
                .findAccountRequest()
                .map(
                        accountsUrl ->
                                this.client
                                        .request(getUrlWithQueryParams(accountsUrl))
                                        .post(AccountsResponse.class, new BaseBody()));
    }

    public TransactionsResponse getTransactions(
            String url, String bankIdentifier, int startIndex, int endIndex) {
        TransactionsRequestBody transactionsRequestBody =
                new TransactionsRequestBody(
                        bankIdentifier, Integer.toString(startIndex), Integer.toString(endIndex));

        URL concatenatedUrl = new URL(IngConstants.Urls.BASE_SSO_REQUEST + url);
        URL transactionsUrl = getUrlWithQueryParams(concatenatedUrl);

        return this.client
                .request(transactionsUrl)
                .post(TransactionsResponse.class, transactionsRequestBody);
    }

    public PendingPaymentsResponseEntity getPendingPayments(
            LoginResponseEntity loginResponse, String bankIdentifier) {
        PendingPaymentsRequestBody body = new PendingPaymentsRequestBody(bankIdentifier);

        return loginResponse
                .findPendingPaymentsRequest()
                .map(
                        url ->
                                client.request(getUrlWithQueryParams(url))
                                        .post(PendingPaymentsResponse.class, body)
                                        .getMobileResponse())
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not find pending payments request in list of requests."));
    }

    public Optional<TrustedBeneficiariesResponse> getBeneficiaries(
            LoginResponseEntity loginResponse) {
        return loginResponse
                .findTrustedBeneficiariesRequest()
                .map(
                        url ->
                                client.request(getUrlWithQueryParams(url))
                                        .post(TrustedBeneficiariesResponse.class, new BaseBody()));
    }

    public ValidateInternalTransferResponse validateInternalTransfer(
            LoginResponseEntity loginResponse,
            String sourceAccount,
            String destinationAccount,
            Transfer transfer) {
        Preconditions.checkNotNull(
                sourceAccount, "Validate internal transfer: Source account can't be null");
        Preconditions.checkNotNull(
                destinationAccount,
                "Validate internal transfer: Destination account can't be null");

        ValidateInternalTransferBody body =
                new ValidateInternalTransferBody(transfer, sourceAccount, destinationAccount);

        return loginResponse
                .findValidateTransferRequest()
                .map(
                        url ->
                                client.request(getUrlWithQueryParams(url))
                                        .post(ValidateInternalTransferResponse.class, body))
                .orElseThrow(
                        () ->
                                TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                        .setMessage(
                                                "Could not find the validate transfer request url.")
                                        .build());
    }

    public ValidateExternalTransferResponseEntity validateTrustedTransfer(
            LoginResponseEntity loginResponse,
            Transfer transfer,
            AccountEntity sourceAccount,
            String destinationAccountNumber) {

        ValidateTrustedTransferBody body =
                new ValidateTrustedTransferBody(transfer, sourceAccount, destinationAccountNumber);

        return loginResponse
                .findValidateTrustedTransferRequest()
                .map(
                        url ->
                                client.request(getUrlWithQueryParams(url))
                                        .post(ValidateTrustedTransferResponse.class, body)
                                        .getMobileResponse())
                .orElseThrow(
                        () ->
                                TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                        .setMessage(
                                                "Could not find the validate trusted transfer request url.")
                                        .build());
    }

    public ValidateExternalTransferResponseEntity validateThirdPartyTransfer(
            LoginResponseEntity loginResponse,
            Transfer transfer,
            AccountEntity sourceAccount,
            String destinationAccountNumber,
            String destinationName) {

        ValidateThirdPartyTransferBody body =
                new ValidateThirdPartyTransferBody(
                        transfer, sourceAccount, destinationAccountNumber, destinationName);

        return loginResponse
                .findValidateThirdTransferRequest()
                .map(
                        url ->
                                client.request(getUrlWithQueryParams(url))
                                        .post(ValidateThirdPartyTransferResponse.class, body)
                                        .getMobileResponse())
                .orElseThrow(
                        () ->
                                TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                        .setMessage(
                                                "Could not find the validate third party transfer request url.")
                                        .build());
    }

    public ExecuteInternalTransferResponse executeInternalTransfer(
            ValidateInternalTransferResponse validateTransferResponse) {

        return validateTransferResponse
                .getRequests()
                .getExecuteTransferRequest()
                .map(
                        url ->
                                client.request(getUrlWithQueryParams(url))
                                        .post(
                                                ExecuteInternalTransferResponse.class,
                                                new BaseBody()))
                .orElseThrow(
                        () ->
                                TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                        .setMessage(
                                                "Could not find the execute transfer request url.")
                                        .build());
    }

    public BaseMobileResponseEntity executeTrustedTransfer(
            ValidateExternalTransferResponseEntity validateExternalTransferResponseEntity,
            int otp) {

        ExecuteExternalTransferBody body = new ExecuteExternalTransferBody(Integer.toString(otp));

        return validateExternalTransferResponseEntity
                .findExecuteTrustedTransferRequest()
                .map(
                        url ->
                                client.request(getUrlWithQueryParams(url))
                                        .post(BaseResponse.class, body)
                                        .getMobileResponse())
                .orElseThrow(
                        () ->
                                TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                        .setMessage(
                                                "Could not find the execute trusted transfer request url.")
                                        .build());
    }

    public BaseMobileResponseEntity executeThirdPartyTransfer(
            ValidateExternalTransferResponseEntity validateExternalTransferResponseEntity,
            int otp) {

        ExecuteExternalTransferBody body = new ExecuteExternalTransferBody(Integer.toString(otp));

        return validateExternalTransferResponseEntity
                .findExecuteThirdPartyTransferRequest()
                .map(
                        url -> {
                            addQueryParamsToBody(body, url);
                            return client.request(
                                            getUrlWithQueryParams(
                                                    new URL(IngConstants.Urls.BASE_SSO_REQUEST)))
                                    .post(BaseResponse.class, body)
                                    .getMobileResponse();
                        })
                .orElseThrow(
                        () ->
                                TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                        .setMessage(
                                                "Could not find the execute third party transfer request url.")
                                        .build());
    }

    private void addQueryParamsToBody(ExecuteExternalTransferBody body, URL url) {
        List<NameValuePair> queryParams = URLEncodedUtils.parse(url.toUri(), "UTF-8");
        for (NameValuePair param : queryParams) {
            body.add(param.getName(), param.getValue());
        }
    }

    private URL getUrlWithQueryParams(URL url) {
        return url.queryParam(
                        IngConstants.Session.ValuePairs.APP_NAME.getKey(),
                        IngConstants.Session.ValuePairs.APP_NAME.getValue())
                .queryParam(
                        IngConstants.Session.ValuePairs.USER_PROFILE.getKey(),
                        IngConstants.Session.ValuePairs.USER_PROFILE.getValue());
    }
}
