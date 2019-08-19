package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.SwedbankPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.enums.SwedbankPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc.PaymentAuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.util.AccountTypePair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
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
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

public class SwedbankPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private SwedbankApiClient apiClient;
    private final SwedbankPaymentAuthenticator paymentAuthenticator;
    private List<PaymentResponse> createdPaymentsList;

    public SwedbankPaymentExecutor(
            SwedbankApiClient apiClient, SwedbankPaymentAuthenticator paymentAuthenticator) {
        this.apiClient = apiClient;
        this.paymentAuthenticator = paymentAuthenticator;
        createdPaymentsList = new ArrayList<>();
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtor = AccountEntity.debtorOf(paymentRequest);
        AmountEntity amount = AmountEntity.amountOf(paymentRequest);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest(creditor, debtor, amount);

        AccountTypePair accountTypePair =
                new AccountTypePair(paymentRequest.getPayment().getCreditorAndDebtorAccountType());

        SwedbankPaymentType paymentType = SwedbankPaymentType.getPaymentType(accountTypePair);

        PaymentResponse paymentResponse =
                apiClient
                        .createPayment(createPaymentRequest, paymentType)
                        .toTinkPaymentResponse(
                                creditor, debtor, amount, paymentType, accountTypePair);

        createdPaymentsList.add(paymentResponse);

        return paymentResponse;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .getPayment(paymentRequest.getPayment().getUniqueId())
                .toTinkPaymentResponse(
                        paymentRequest.getPayment(),
                        apiClient
                                .getPaymentStatus(paymentRequest.getPayment().getUniqueId())
                                .getTransactionStatus());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        Payment payment = paymentMultiStepRequest.getPayment();

        PaymentAuthorisationResponse paymentAuthorisationResponse =
                apiClient.startPaymentAuthorisation(payment.getUniqueId());

        paymentAuthenticator.openThirdPartyApp(paymentAuthorisationResponse.getAuthorizationUrl());

        GetPaymentStatusResponse getPaymentStatusResponse =
                apiClient.getPaymentStatus(payment.getUniqueId());

        PaymentStatus paymentStatus =
                SwedbankPaymentStatus.fromString(getPaymentStatusResponse.getTransactionStatus())
                        .getTinkPaymentStatus();

        payment.setStatus(paymentStatus);
        return new PaymentMultiStepResponse(
                payment, AuthenticationStepConstants.STEP_FINALIZE, new ArrayList<>());
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
        return new PaymentListResponse(createdPaymentsList);
    }
}
