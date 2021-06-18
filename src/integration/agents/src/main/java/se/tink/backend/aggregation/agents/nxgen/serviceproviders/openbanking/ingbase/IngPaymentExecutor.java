package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.SimpleAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
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
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@Slf4j
@RequiredArgsConstructor
public class IngPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    public static final String PAYMENT_AUTHORIZATION_URL = "payment_authorization_url";
    public static final String VALIDATE_PAYMENT = "confirm_payment";
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");

    private static final long WAIT_FOR_MINUTES = 9L;

    private final IngBaseApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SupplementalInformationHelper supplementalInformationHelper;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {

        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);
        AmountEntity amount = AmountEntity.amountOf(paymentRequest);
        AccountEntity debtor = AccountEntity.debtorOf(paymentRequest);

        CreatePaymentRequest createPaymentRequest = createPaymentRequest(paymentRequest);

        CreatePaymentResponse paymentResponse = apiClient.createPayment(createPaymentRequest);

        String authorizationUrl = paymentResponse.getLinks().getAuthorizationUrl();
        sessionStorage.put(PAYMENT_AUTHORIZATION_URL, authorizationUrl);

        return new PaymentResponse(
                new Payment.Builder()
                        .withUniqueId(paymentResponse.getPaymentId())
                        .withType(PaymentType.SEPA)
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
                        Optional.ofNullable(sessionStorage.get(PAYMENT_AUTHORIZATION_URL))
                                .orElseThrow(
                                        () ->
                                                new PaymentAuthenticationException(
                                                        "Payment authentication failed. There is no authorization url!",
                                                        new PaymentRejectedException()));
                openThirdPartyApp(new URL(authorizationUrl));
                nextStep = VALIDATE_PAYMENT;
                break;
            case VALIDATE_PAYMENT:
                PaymentStatus paymentStatus = getAndVerifyStatus(payment.getUniqueId());
                payment.setStatus(paymentStatus);
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                break;
            default:
                throw new PaymentException(
                        "Unknown step " + paymentMultiStepRequest.getStep() + " for payment sign.");
        }
        return new PaymentMultiStepResponse(payment, nextStep, Collections.emptyList());
    }

    private PaymentStatus getAndVerifyStatus(String paymentId) throws PaymentException {

        PaymentStatus paymentStatus = apiClient.getPayment(paymentId).getPaymentStatus();

        if (paymentStatus == PaymentStatus.PENDING) {
            throw new PaymentAuthenticationException(
                    "Payment authentication failed.", new PaymentRejectedException());
        }

        if (paymentStatus != PaymentStatus.SIGNED) {
            throw new PaymentRejectedException("Unexpected payment status: " + paymentStatus);
        }

        return paymentStatus;
    }

    public CreatePaymentRequest createPaymentRequest(PaymentRequest paymentRequest) {

        Payment payment = paymentRequest.getPayment();

        SimpleAccountEntity creditor =
                new SimpleAccountEntity(
                        payment.getCreditor().getAccountNumber(), payment.getCurrency());

        SimpleAccountEntity debtor =
                new SimpleAccountEntity(
                        payment.getDebtor().getAccountNumber(), payment.getCurrency());

        RemittanceInformation remittanceInformation = payment.getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);

        LocalDate executionDate =
                Optional.ofNullable(payment.getExecutionDate())
                        .orElse(LocalDate.now((DEFAULT_ZONE_ID)));

        return CreatePaymentRequest.builder()
                .endToEndIdentification(RandomStringUtils.random(35, true, true))
                .instructedAmount(AmountEntity.amountOf(paymentRequest))
                .debtorAccount(debtor)
                .creditorAccount(creditor)
                .creditorAgent(IngBaseConstants.PaymentRequest.CREDITOR_AGENT)
                .creditorName(IngBaseConstants.PaymentRequest.PAYMENT_CREDITOR)
                .chargeBearer(IngBaseConstants.PaymentRequest.SLEV)
                .remittanceInformationUnstructured(remittanceInformation.getValue())
                .serviceLevelCode(IngBaseConstants.PaymentRequest.SEPA)
                .requestedExecutionDate(
                        executionDate.format(
                                DateTimeFormatter.ofPattern(
                                        IngBaseConstants.PaymentRequest.EXECUTION_DATE_FORMAT)))
                .localInstrumentCode(
                        PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER == payment.getPaymentScheme()
                                ? IngBaseConstants.PaymentRequest.INST
                                : null)
                .build();
    }

    private void openThirdPartyApp(URL authorizationUrl) throws PaymentException {
        this.supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(authorizationUrl));
        Optional<Map<String, String>> queryParameters =
                supplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);

        queryParameters.orElseThrow(
                () ->
                        new PaymentAuthorizationException(
                                "SCA time-out.", ThirdPartyAppError.TIMED_OUT.exception()));
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
