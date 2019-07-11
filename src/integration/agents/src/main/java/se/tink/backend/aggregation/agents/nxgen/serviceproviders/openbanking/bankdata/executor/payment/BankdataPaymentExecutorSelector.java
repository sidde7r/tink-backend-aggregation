package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.PaymentRequests;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.CreditorAddressEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.util.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.util.TypePair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class BankdataPaymentExecutorSelector implements PaymentExecutor {

    private BankdataApiClient apiClient;

    public BankdataPaymentExecutorSelector(BankdataApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {

        PaymentType type =
                BankdataConstants.PAYMENT_TYPE_MAPPER
                        .translate(
                                new TypePair(
                                        paymentRequest
                                                .getPayment()
                                                .getCreditorAndDebtorAccountType()))
                        .orElse(PaymentType.UNDEFINED);

        return createPayment(paymentRequest, type);
    }

    private PaymentResponse createPayment(PaymentRequest paymentRequest, PaymentType type)
            throws PaymentException {
        AmountEntity amountEntity =
                new AmountEntity(
                        paymentRequest.getPayment().getAmount().getCurrency(),
                        paymentRequest.getPayment().getAmount().getValue().toString());

        CreditorAddressEntity creditorAddress =
                new CreditorAddressEntity(
                        PaymentRequests.STREET,
                        PaymentRequests.BUILDING,
                        PaymentRequests.CITY,
                        PaymentRequests.POSTAL_CODE,
                        PaymentRequests.COUNTRY);

        DebtorEntity debtorEntity = DebtorEntity.of(paymentRequest);
        CreditorEntity creditorEntity = CreditorEntity.of(paymentRequest);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withCreditor(creditorEntity)
                        .withDebtor(debtorEntity)
                        .withAmount(amountEntity)
                        .withRequestedExecutionDate(
                                DateUtils.convertToDateViaInstant(
                                        paymentRequest.getPayment().getExecutionDate()))
                        .withChargeBearer(PaymentRequests.CHARGE_BEARER)
                        .withCreditorAgent(PaymentRequests.AGENT)
                        .withCreditorAddress(creditorAddress)
                        .withCreditorName(PaymentRequests.CREDITOR_NAME)
                        .withRemittanceInformationUnstructured(PaymentRequests.REMITTANCE)
                        .withEndToEndIdentification(PaymentRequests.IDENTIFICATION)
                        .build();

        return apiClient
                .createPayment(createPaymentRequest, type)
                .toTinkPayment(
                        paymentRequest.getPayment().getDebtor().getAccountNumber(),
                        paymentRequest.getPayment().getCreditor().getAccountNumber(),
                        type);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        String paymentId = paymentRequest.getPayment().getUniqueId();
        PaymentType type = paymentRequest.getPayment().getType();

        return apiClient.fetchPayment(paymentId, type).toTinkPayment(paymentId, type);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        Payment payment = paymentMultiStepRequest.getPayment();

        apiClient.authorizePayment(payment.getUniqueId(), payment.getType());
        apiClient.signPayment(payment.getUniqueId());

        // On their sandbox data is static and can't be changed
        // so the status will always be recieved
        payment.setStatus(PaymentStatus.PAID);

        return new PaymentMultiStepResponse(
                payment, AuthenticationStepConstants.STEP_FINALIZE, new ArrayList<>());
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "Create beneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "Cancel not yet implemented for " + this.getClass().getName());
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
