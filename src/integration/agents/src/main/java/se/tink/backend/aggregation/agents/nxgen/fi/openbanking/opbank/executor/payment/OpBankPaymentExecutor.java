package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment;

import com.google.common.collect.ImmutableList;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import lombok.SneakyThrows;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities.AcrEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities.AuthorizationIdEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities.ClaimEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities.ClaimsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities.TokenBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities.TokenHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.configuration.OpBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.tools.jwt.kid.KeyIdProvider;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.enums.RemittanceInformationType;

public class OpBankPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final OpBankApiClient apiClient;
    private final OpBankConfiguration configuration;
    private final String redirectUrl;
    private final Credentials credentials;
    private final KeyIdProvider keyIdProvider;
    private final PersistentStorage persistentStorage;

    public OpBankPaymentExecutor(
            OpBankApiClient apiClient,
            AgentConfiguration<OpBankConfiguration> agentConfiguration,
            Credentials credentials,
            KeyIdProvider keyIdProvider,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.credentials = credentials;
        this.keyIdProvider = keyIdProvider;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        TokenResponse newToken = this.apiClient.fetchNewToken(OpBankConstants.TokenValues.PAYMENTS);

        Payment payment = paymentRequest.getPayment();

        final String message =
                payment.getRemittanceInformation().getType()
                                == RemittanceInformationType.UNSTRUCTURED
                        ? payment.getRemittanceInformation().getValue()
                        : null;

        CreatePaymentRequest request =
                CreatePaymentRequest.builder()
                        .creditorToPayee(payment.getCreditor())
                        .amount(payment.getExactCurrencyAmount())
                        .message(message)
                        .paymentOrder(payment.getPaymentScheme())
                        .count(payment)
                        .build();

        CreatePaymentResponse createPaymentResponse =
                this.apiClient.createNewPayment(
                        newToken.getAccessToken(), request, keyIdProvider.get());

        credentials.setSessionExpiryDate(
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                        newToken.toTinkToken(),
                        OpBankConstants.RefreshTokenFormKeys.DEFAULT_TOKEN_LIFETIME,
                        OpBankConstants.RefreshTokenFormKeys.DEFAULT_TOKEN_LIFETIME_UNIT));

        TokenBodyEntity tokenBody =
                buildTokenBodyEntity(
                        createPaymentResponse,
                        persistentStorage.get(OpBankConstants.StorageKeys.STATE));
        PaymentResponse paymentResponse = createPaymentResponse.toTinkPayment(paymentRequest);
        persistentStorage.put(
                OpBankConstants.StorageKeys.URL, (buildAuthorizationURL(tokenBody).get()));

        return paymentResponse;
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException, AuthenticationException {
        String code = persistentStorage.get(OpBankConstants.StorageKeys.CODE);
        OAuth2Token oAuth2Token = apiClient.exchangeToken(code).toOauth2Token();
        this.apiClient.submitPayment(
                paymentMultiStepRequest.getPayment().getUniqueId(),
                oAuth2Token,
                paymentMultiStepRequest.getPayment().getId().toString());

        CreatePaymentResponse createPaymentResponse =
                this.apiClient.verifyPayment(
                        oAuth2Token, paymentMultiStepRequest.getPayment().getUniqueId());
        PaymentStatus paymentStatus = createPaymentResponse.getTinkStatus();

        switch (paymentStatus) {
            case SIGNED:
            case PAID:
            case SETTLEMENT_COMPLETED:
                return new PaymentMultiStepResponse(
                        paymentMultiStepRequest, AuthenticationStepConstants.STEP_FINALIZE);
            case REJECTED:
                throw new PaymentRejectedException("Payment rejected by Bank");
            case CANCELLED:
                throw new PaymentCancelledException("Payment Cancelled by PSU");
            default:
                throw new PaymentAuthorizationException();
        }
    }

    private String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        OpBankConstants.ErrorMessages.MISSING_CONFIGURATION));
    }

    private TokenBodyEntity buildTokenBodyEntity(
            CreatePaymentResponse paymentResponse, String state) {
        ClaimsEntity claims =
                new ClaimsEntity(
                        new ClaimEntity(
                                new AuthorizationIdEntity(
                                        paymentResponse.getAuthorizationId(), true),
                                null),
                        new ClaimEntity(
                                new AuthorizationIdEntity(
                                        paymentResponse.getAuthorizationId(), true),
                                new AcrEntity(
                                        true,
                                        ImmutableList.of(OpBankConstants.TokenValues.ARC_VALUES))));

        return TokenBodyEntity.builder()
                .aud(OpBankConstants.Urls.BASE_URL)
                .iss(configuration.getClientId())
                .response_type(OpBankConstants.TokenValues.RESPONSE_TYPE)
                .client_id(configuration.getClientId())
                .redirect_uri(getRedirectUrl())
                .scope(OpBankConstants.TokenValues.SCOPE_PIS)
                .state(state)
                .nonce(UUID.randomUUID().toString())
                .max_age(OpBankConstants.TokenValues.MAX_AGE)
                .iat(OffsetDateTime.now().toEpochSecond())
                .exp(OffsetDateTime.now().plusHours(1).toEpochSecond())
                .claims(claims)
                .build();
    }

    @SneakyThrows
    private URL buildAuthorizationURL(TokenBodyEntity tokenBody) {
        final String tokenBodyJson = SerializationUtils.serializeToString(tokenBody);

        String tokenHeadJson =
                SerializationUtils.serializeToString(new TokenHeaderEntity(keyIdProvider.get()));

        String baseTokenString =
                String.format(
                        "%s.%s",
                        Base64.getUrlEncoder()
                                .encodeToString(tokenHeadJson.getBytes())
                                .replace("=", ""),
                        Base64.getUrlEncoder()
                                .encodeToString(tokenBodyJson.getBytes())
                                .replace("=", ""));
        String signature = apiClient.fetchSignature(baseTokenString);
        String fullToken = String.format("%s.%s", baseTokenString, signature).replace("=", "");
        return new URL(OpBankConstants.Urls.AUTHORIZATION_URL)
                .queryParam(OpBankConstants.AuthorizationKeys.REQUEST, fullToken)
                .queryParam(
                        OpBankConstants.AuthorizationKeys.RESPONSE_TYPE,
                        OpBankConstants.AuthorizationValues.CODE)
                .queryParam(
                        OpBankConstants.AuthorizationKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(
                        OpBankConstants.AuthorizationKeys.SCOPE,
                        OpBankConstants.AuthorizationValues.OPENID_PAYMENTS);
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        return null;
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) throws PaymentException {
        return null;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        return null;
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        return null;
    }
}
