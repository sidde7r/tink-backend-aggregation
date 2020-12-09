package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.enums.UnicreditPaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
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
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@Slf4j
@AllArgsConstructor
public class UnicreditPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final UnicreditBaseApiClient apiClient;

    private final SessionStorage sessionStorage;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        sessionStorage.put(HeaderKeys.PSU_IP_ADDRESS, paymentRequest.getOriginatingUserIp());
        if (PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER
                == paymentRequest.getPayment().getPaymentScheme()) {
            this.sessionStorage.put(
                    UnicreditConstants.PathParameters.PAYMENT_PRODUCT,
                    UnicreditPaymentProduct.INSTANT_SEPA_CREDIT_TRANSFERS.toString());
        }
        PaymentType type =
                UnicreditConstants.PAYMENT_TYPE_MAPPER
                        .translate(paymentRequest.getPayment().getCreditorAndDebtorAccountType())
                        .orElse(PaymentType.UNDEFINED);

        Payment payment = paymentRequest.getPayment();

        AmountEntity amount =
                new AmountEntity(
                        paymentRequest.getPayment().getAmount().getValue().toString(),
                        paymentRequest.getPayment().getAmount().getCurrency());

        AccountEntity debtor = new AccountEntity(payment.getDebtor().getAccountNumber());
        AccountEntity creditor = new AccountEntity(payment.getCreditor().getAccountNumber());
        RemittanceInformation remittanceInformation =
                paymentRequest.getPayment().getRemittanceInformation();

        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);

        String unstructuredRemittance =
                Optional.ofNullable(remittanceInformation.getValue()).orElse("");
        Date executionDate =
                Optional.ofNullable(payment.getExecutionDate())
                        .map(
                                localDate ->
                                        Date.from(
                                                localDate
                                                        .atStartOfDay()
                                                        .atZone(ZoneId.of("CET"))
                                                        .toInstant()))
                        .orElse(new Date());
        CreatePaymentRequest request =
                new CreatePaymentRequest.Builder()
                        .withCreditor(creditor)
                        .withDebtor(debtor)
                        .withAmount(amount)
                        .withCreditorName(payment.getCreditor().getName())
                        .withUnstructuredRemittance(unstructuredRemittance)
                        .withRequestedExecutionDate(executionDate)
                        .build();

        return apiClient
                .createSepaPayment(request)
                .toTinkPayment(
                        paymentRequest.getPayment().getDebtor().getAccountNumber(),
                        paymentRequest.getPayment().getCreditor().getAccountNumber(),
                        type);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentAuthorizationException {
        Payment payment = paymentMultiStepRequest.getPayment();

        PaymentMultiStepResponse paymentMultiStepResponse = null;
        PaymentResponse paymentResponse =
                fetchPaymentWithId(
                        paymentMultiStepRequest.getPayment().getUniqueId(),
                        paymentMultiStepRequest.getPayment().getType());
        PaymentStatus paymentStatus = paymentResponse.getPayment().getStatus();
        log.info("Payment id={} sign status={}", payment.getId(), paymentStatus);

        if (PaymentStatus.SIGNED.equals(paymentStatus)) {
            paymentMultiStepResponse =
                    new PaymentMultiStepResponse(
                            paymentResponse,
                            AuthenticationStepConstants.STEP_FINALIZE,
                            new ArrayList<>());
        } else {
            throw new PaymentAuthorizationException();
        }

        return paymentMultiStepResponse;
    }

    private PaymentResponse fetchPaymentWithId(String paymentId, PaymentType type) {
        return apiClient.fetchPayment(paymentId).toTinkPayment(paymentId, type);
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "Create beneficiary not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "Cancel not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        String paymentId = paymentRequest.getPayment().getUniqueId();
        PaymentType type = paymentRequest.getPayment().getType();

        return apiClient.fetchPayment(paymentId).toTinkPayment(paymentId, type);
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {

        return new PaymentListResponse(
                Optional.ofNullable(paymentListRequest)
                        .map(PaymentListRequest::getPaymentRequestList)
                        .map(Collection::stream)
                        .orElseGet(Stream::empty)
                        .map(this::fetch)
                        .collect(Collectors.toList()));
    }
}
