package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.assertj.core.util.VisibleForTesting;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.CreditorAgentConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.MinimumValues;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.LaBanquePostalePaymentSigner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.rpc.ConfirmPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities.CreditorAgentEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities.RemittanceInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums.BerlinGroupPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils.FrOpenBankingDateUtil;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@Slf4j
public class LaBanquePostalePaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    public static final String PAYMENT_AUTHORIZATION_URL = "payment_authorization_url";
    public static final String CONFIRM_PAYMENT = "confirm_payment";
    public static final String PSU_AUTHORIZATION_FACTOR = "psu_authorization_factor";
    public static final String PSU_AUTHORIZATION_FACTOR_KEY = "psuAuthenticationFactor";
    private static final String STATE = "state";
    PaymentType paymentType = PaymentType.SEPA;

    private static final long WAIT_FOR_MINUTES = 9L;

    private final LaBanquePostalePaymentApiClient apiClient;
    private final String redirectUrl;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final LaBanquePostalePaymentSigner laBanquePostalePaymentSigner =
            new LaBanquePostalePaymentSigner();

    public LaBanquePostalePaymentExecutor(
            LaBanquePostalePaymentApiClient apiClient,
            String redirectUrl,
            StrongAuthenticationState strongAuthenticationState,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.redirectUrl = redirectUrl;
        this.strongAuthenticationState = strongAuthenticationState;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        apiClient.fetchToken();

        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);
        AmountEntity amount = AmountEntity.amountOf(paymentRequest);
        AccountEntity debtor = AccountEntity.debtorOf(paymentRequest);

        validatePayment(paymentRequest, amount);

        CreatePaymentRequest createPaymentRequest = getCreatePaymentRequest(paymentRequest);

        CreatePaymentResponse paymentResponse = apiClient.createPayment(createPaymentRequest);

        String authorizationUrl = paymentResponse.getLinks().getAuthorizationUrl();
        laBanquePostalePaymentSigner.setPaymentAuthorizationUrl(authorizationUrl);

        String paymentId = apiClient.findPaymentId(authorizationUrl);
        return new PaymentResponse(
                new Payment.Builder()
                        .withUniqueId(paymentId)
                        .withType(paymentType)
                        .withCurrency(amount.getCurrency())
                        .withExactCurrencyAmount(amount.toTinkAmount())
                        .withCreditor(creditor.toTinkCreditor())
                        .withDebtor(debtor.toTinkDebtor())
                        .build());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        String nextStep;
        Payment payment = paymentMultiStepRequest.getPayment();

        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                String authorizationUrl =
                        laBanquePostalePaymentSigner.getPaymentAuthorizationUrlOrThrow();

                Map<String, String> queryParametersMap =
                        new CaseInsensitiveMap<>(openThirdPartyApp(new URL(authorizationUrl)));
                laBanquePostalePaymentSigner.setPsuAuthenticationFactorOrThrow(queryParametersMap);

                nextStep = CONFIRM_PAYMENT;
                break;
            case CONFIRM_PAYMENT:
                PaymentStatus paymentStatus = confirmAndVerifyStatus(payment.getUniqueId());
                payment.setStatus(paymentStatus);
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                break;
            default:
                throw new PaymentException(
                        "Unknown step " + paymentMultiStepRequest.getStep() + " for payment sign.",
                        InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }
        return new PaymentMultiStepResponse(payment, nextStep, Collections.emptyList());
    }

    public CreatePaymentRequest getCreatePaymentRequest(PaymentRequest paymentRequest) {

        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);
        AmountEntity amount = AmountEntity.amountOf(paymentRequest);
        CreditorAgentEntity creditorAgent =
                new CreditorAgentEntity(CreditorAgentConstants.BICFI, CreditorAgentConstants.NAME);

        Payment payment = paymentRequest.getPayment();

        RemittanceInformation remittanceInformation = payment.getRemittanceInformation();
        RemittanceInformationEntity remittanceInformationEntity =
                remittanceInformation.getType().equals(RemittanceInformationType.UNSTRUCTURED)
                        ? new RemittanceInformationEntity(
                                null, Collections.singletonList(remittanceInformation.getValue()))
                        : new RemittanceInformationEntity(
                                Collections.singletonList(remittanceInformation.getValue()), null);

        return new CreatePaymentRequest.Builder()
                .withPaymentType(paymentType)
                .withAmount(amount)
                .withCreditorAgentEntity(creditorAgent)
                .withCreditorAccount(creditor)
                .withCreditorName(new CreditorEntity(payment.getCreditor().getName()))
                .withExecutionDate(
                        FrOpenBankingDateUtil.getExecutionDate(payment.getExecutionDate()))
                .withCreationDateTime(FrOpenBankingDateUtil.getCreationDate())
                .withRedirectUrl(
                        new URL(redirectUrl)
                                .queryParam(STATE, strongAuthenticationState.getState()))
                .withRemittanceInformation(remittanceInformationEntity)
                .withPaymentScheme(payment.getPaymentScheme())
                .build();
    }

    private void validatePayment(PaymentRequest paymentRequest, AmountEntity amount)
            throws PaymentValidationException {
        if (paymentRequest.getPayment().getPaymentScheme()
                        != PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER
                && amount.toTinkAmount()
                                .getExactValue()
                                .compareTo(new BigDecimal(MinimumValues.MINIMUM_AMOUNT_FOR_SEPA))
                        < 0) {
            throw new PaymentValidationException(
                    "Transfer amount can't be less than 1.5 EUR.",
                    InternalStatus.INVALID_MINIMUM_AMOUNT);
        }
    }

    private PaymentStatus confirmAndVerifyStatus(String paymentId) throws PaymentException {

        ConfirmPaymentResponse confirmPaymentResponse =
                apiClient.confirmPayment(
                        paymentId, laBanquePostalePaymentSigner.getPsuAuthenticationFactor());

        BerlinGroupPaymentStatus berlinPaymentStatus =
                confirmPaymentResponse.getPaymentRequest().getPaymentStatus();

        PaymentStatus paymentStatus =
                berlinPaymentStatus.getTinkPaymentStatus().equals(PaymentStatus.PAID)
                        ? PaymentStatus.SIGNED
                        : berlinPaymentStatus.getTinkPaymentStatus();

        if (paymentStatus == PaymentStatus.PENDING) {
            throw new PaymentRejectedException();
        }

        if (paymentStatus != PaymentStatus.SIGNED) {
            throw new PaymentRejectedException(
                    "Unexpected payment status: " + paymentStatus,
                    InternalStatus.PAYMENT_REJECTED_BY_BANK_NO_DESCRIPTION);
        }

        return paymentStatus;
    }

    private Map<String, String> openThirdPartyApp(URL authorizationUrl) throws PaymentException {
        this.supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(authorizationUrl));
        Optional<Map<String, String>> queryParameters =
                supplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);

        return queryParameters.orElseThrow(
                () ->
                        new PaymentAuthorizationException(
                                "SCA time-out.",
                                InternalStatus.PAYMENT_AUTHORIZATION_TIMEOUT,
                                ThirdPartyAppError.TIMED_OUT.exception()));
    }

    @VisibleForTesting
    LaBanquePostalePaymentSigner getLaBanquePostalePaymentSigner() {
        return this.laBanquePostalePaymentSigner;
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "cancel not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "fetch not yet implemented for " + this.getClass().getName());
    }
}
