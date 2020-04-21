package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.authenticator.BnpParibasFortisPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.configuration.BnpParibasFortisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.enums.BnpParibasFortisPaymentType;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.rpc.CreatePaymentRequest;
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
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

public class BnpParibasFortisPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private final BnpParibasFortisApiClient apiClient;
    private final BnpParibasFortisPaymentAuthenticator paymentAuthenticator;
    private final BnpParibasFortisConfiguration configuration;
    private final StrongAuthenticationState strongAuthenticationState;
    private final List<PaymentResponse> createdPaymentsList;

    public BnpParibasFortisPaymentExecutor(
            BnpParibasFortisApiClient apiClient,
            BnpParibasFortisPaymentAuthenticator paymentAuthenticator,
            BnpParibasFortisConfiguration configuration,
            StrongAuthenticationState strongAuthenticationState) {
        this.apiClient = apiClient;
        this.paymentAuthenticator = paymentAuthenticator;
        this.configuration = configuration;
        this.strongAuthenticationState = strongAuthenticationState;
        createdPaymentsList = new ArrayList<>();
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtor = AccountEntity.debtorOf(paymentRequest);
        AmountEntity amount = AmountEntity.amountOf(paymentRequest);

        Payment payment = paymentRequest.getPayment();

        // Backwards compatibility patch: some agents would break if the dueDate was null, so we
        // defaulted it. This behaviour is no longer true for agents that properly implement the
        // execution of future dueDate. For more info about the fix, check PAY-549; for the support
        // of future dueDate, check PAY1-273.
        if (payment.getExecutionDate() == null) {
            payment.setExecutionDate(LocalDate.now(Clock.systemDefaultZone()));
        }

        BnpParibasFortisPaymentType paymentType = getPaymentType(paymentRequest);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withPaymentType(paymentType)
                        .withAmount(amount)
                        .withCreditorAccount(creditor)
                        .withCreditorName(payment.getCreditor().getName())
                        .withDebtorAccount(debtor)
                        .withExecutionDate(payment.getExecutionDate())
                        .withCreationDateTime(LocalDateTime.now())
                        .withRedirectUrl(
                                new URL(configuration.getRedirectUrl())
                                        .queryParam(
                                                QueryKeys.STATE,
                                                strongAuthenticationState.getState()))
                        .withRemittanceInformation(payment.getReference().getValue())
                        .build();

        PaymentResponse paymentResponse =
                apiClient
                        .createPayment(createPaymentRequest)
                        .toTinkPaymentResponse(creditor, debtor, amount, paymentType);

        createdPaymentsList.add(paymentResponse);

        return paymentResponse;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .getPayment(paymentRequest.getPayment().getUniqueId())
                .toTinkPaymentResponse(paymentRequest.getPayment().getUniqueId());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        Payment payment = paymentMultiStepRequest.getPayment();

        paymentAuthenticator.openThirdPartyApp(
                new URL(configuration.getAuthBaseUrl() + Urls.AUTH_PAYMENT)
                        .parameter(IdTags.PAYMENT_ID, payment.getUniqueId())
                        .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId()));

        PaymentStatus paymentStatus =
                apiClient.getPayment(payment.getUniqueId()).getPaymentStatus();

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

    private BnpParibasFortisPaymentType getPaymentType(PaymentRequest paymentRequest) {
        return BnpParibasFortisPaymentType.SEPA;
    }
}
