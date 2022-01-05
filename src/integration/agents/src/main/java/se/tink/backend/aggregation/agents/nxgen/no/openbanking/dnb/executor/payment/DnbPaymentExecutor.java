package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment;

import static se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums.DnbPaymentType.NORWEGIAN_DOMESTIC_CREDIT_TRANSFERS_PERIODIC;

import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums.DnbPaymentType;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc.CancelPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc.RemittanceInformationStructured;
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
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DnbPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private final DnbApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final StrongAuthenticationState strongAuthenticationState;
    private final DnbPaymentSigner dnbPaymentSigner;

    public DnbPaymentExecutor(
            DnbApiClient apiClient,
            SessionStorage sessionStorage,
            StrongAuthenticationState strongAuthenticationState,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.strongAuthenticationState = strongAuthenticationState;
        this.dnbPaymentSigner =
                new DnbPaymentSigner(this, sessionStorage, supplementalInformationHelper);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        DnbPaymentType dnbPaymentType = DnbPaymentType.getDnbPaymentType(paymentRequest);
        CreatePaymentRequest createPaymentRequest =
                getCreatePaymentRequest(paymentRequest, dnbPaymentType);

        CreatePaymentResponse createPaymentResponse =
                apiClient.createPayment(
                        createPaymentRequest, dnbPaymentType, strongAuthenticationState.getState());

        sessionStorage.put(
                createPaymentResponse.getPaymentId(),
                createPaymentResponse.getLinks().getScaRedirect().getHref());

        return createPaymentResponse.toTinkPaymentResponse(
                createPaymentRequest,
                dnbPaymentType,
                paymentRequest.getPayment().getPaymentServiceType(),
                paymentRequest.getPayment().getPaymentScheme());
    }

    private CreatePaymentRequest getCreatePaymentRequest(
            PaymentRequest paymentRequest, DnbPaymentType dnbPaymentType) {
        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtor = AccountEntity.debtorOf(paymentRequest);

        CreatePaymentRequest.CreatePaymentRequestBuilder createPaymentRequestBuilder =
                CreatePaymentRequest.builder()
                        .dnbPaymentType(dnbPaymentType)
                        .creditor(creditor)
                        .debtor(debtor)
                        .amount(AmountEntity.amountOf(paymentRequest))
                        .requestedExecutionDate(
                                paymentRequest.getPayment().getExecutionDate().toString())
                        .creditorName(paymentRequest.getPayment().getCreditor().getName());

        if (dnbPaymentType == NORWEGIAN_DOMESTIC_CREDIT_TRANSFERS_PERIODIC) {
            createPaymentRequestBuilder
                    // recurring payments endpoint accepts creditor and debtor only in BBAN form
                    .creditor(creditor.toBban())
                    .debtor(debtor.toBban())
                    .frequency(paymentRequest.getPayment().getFrequency().toString())
                    .startDate(paymentRequest.getPayment().getStartDate().toString())
                    .endDate(paymentRequest.getPayment().getEndDate().toString());
        }

        RemittanceInformation remittanceInformation =
                paymentRequest.getPayment().getRemittanceInformation();
        if (remittanceInformation.getType() == RemittanceInformationType.UNSTRUCTURED) {
            createPaymentRequestBuilder.remittanceInformationUnstructured(
                    remittanceInformation.getValue());
        } else if (remittanceInformation.getType() == RemittanceInformationType.REFERENCE) {
            createPaymentRequestBuilder.remittanceInformationStructured(
                    RemittanceInformationStructured.builder()
                            .reference(remittanceInformation.getValue())
                            .build());
        }

        return createPaymentRequestBuilder.build();
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        DnbPaymentType dnbPaymentType = DnbPaymentType.getDnbPaymentType(paymentRequest);

        return apiClient
                .getPayment(dnbPaymentType, paymentRequest.getPayment().getUniqueId())
                .toTinkPaymentResponse(paymentRequest.getPayment(), dnbPaymentType);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        dnbPaymentSigner.sign(paymentMultiStepRequest);

        final Payment payment = paymentMultiStepRequest.getPayment();
        payment.setStatus(PaymentStatus.PAID);

        return new PaymentMultiStepResponse(payment, SigningStepConstants.STEP_FINALIZE);
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        DnbPaymentType dnbPaymentType = DnbPaymentType.getDnbPaymentType(paymentRequest);

        CancelPaymentResponse paymentResponse =
                apiClient.cancelPayment(
                        dnbPaymentType,
                        paymentRequest.getPayment().getUniqueId(),
                        strongAuthenticationState.getState());
        URL authorizationUrl = new URL(paymentResponse.getLinks().getScaRedirect().getHref());

        return dnbPaymentSigner.signCancelPayment(paymentRequest, authorizationUrl);
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return new PaymentListResponse(
                paymentListRequest.getPaymentRequestList().stream()
                        .map(req -> new PaymentResponse(req.getPayment()))
                        .collect(Collectors.toList()));
    }
}
