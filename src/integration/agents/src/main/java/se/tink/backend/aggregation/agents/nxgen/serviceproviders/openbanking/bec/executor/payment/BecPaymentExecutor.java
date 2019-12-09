package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.PaymentProducts;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.entities.PaymentRedirectInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.utils.OAuthUtils;
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
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class BecPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private final BecApiClient apiClient;
    private final SessionStorage sessionStorage;

    public BecPaymentExecutor(BecApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();

        String paymentProduct = getPaymentProduct(payment);

        PaymentType paymentType =
                BecConstants.PAYMENT_TYPE_MAPPER
                        .translate(paymentProduct)
                        .orElse(PaymentType.UNDEFINED);

        AccountEntity creditor;
        AccountEntity debtor;

        if (paymentType.equals(PaymentType.DOMESTIC)) {
            creditor =
                    new AccountEntity.Builder()
                            .setBban(payment.getCreditor().getAccountNumber())
                            .build();
            debtor =
                    new AccountEntity.Builder()
                            .setBban(payment.getDebtor().getAccountNumber())
                            .build();
        } else {
            creditor =
                    new AccountEntity.Builder()
                            .setIban(payment.getCreditor().getAccountNumber())
                            .build();
            debtor =
                    new AccountEntity.Builder()
                            .setIban(payment.getDebtor().getAccountNumber())
                            .build();
        }

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest(
                        creditor,
                        payment.getCreditor().getName(),
                        debtor,
                        new AmountEntity(
                                payment.getAmount().getCurrency(), payment.getAmount().getValue()),
                        payment.getExecutionDate()
                                .format(DateTimeFormatter.ofPattern(FormValues.DATE_FORMAT)));

        String state = OAuthUtils.generateNonce();
        CreatePaymentResponse createPaymentResponse =
                apiClient.createPayment(createPaymentRequest, state);

        String paymentId = createPaymentResponse.getPaymentId();
        sessionStorage.put(
                paymentId,
                new PaymentRedirectInfoEntity(state, createPaymentResponse.getScaRedirect()));

        return apiClient.getPayment(paymentId).toTinkPayment(paymentId, paymentType);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();

        return apiClient
                .getPayment(payment.getUniqueId())
                .toTinkPayment(payment.getUniqueId(), payment.getType());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        Payment payment = paymentMultiStepRequest.getPayment();

        return new PaymentMultiStepResponse(
                fetch(new PaymentRequest(payment)),
                AuthenticationStepConstants.STEP_FINALIZE,
                new ArrayList<>());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return paymentListRequest.getPaymentRequestList().stream()
                .map(this::fetch)
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(), PaymentListResponse::new));
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

    private String getPaymentProduct(Payment payment) {
        return payment.getCreditor().getAccountIdentifierType().equals(Type.IBAN)
                ? PaymentProducts.SEPA_CREDIT_TRANSFERS
                : PaymentProducts.DOMESTIC_CREDIT_TRANSFER;
    }
}
