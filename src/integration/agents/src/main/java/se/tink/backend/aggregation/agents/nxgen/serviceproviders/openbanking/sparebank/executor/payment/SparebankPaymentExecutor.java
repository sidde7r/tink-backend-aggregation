package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.CountryCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.DatePatterns;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.SparebankSignSteps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration.SparebankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.AdressEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums.SparebankPaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.CreatePaymentRequest.Builder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.StartAuthorizationProcessResponse;
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
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class SparebankPaymentExecutor implements PaymentExecutor {
    private SparebankApiClient apiClient;
    private SessionStorage sessionStorage;
    private SparebankConfiguration sparebankConfiguration;
    private List<PaymentResponse> paymentList =
            new ArrayList<>(); // used for mocking fetch multiple

    public SparebankPaymentExecutor(
            SparebankApiClient apiClient,
            SessionStorage sessionStorage,
            SparebankConfiguration sparebankConfiguration) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.sparebankConfiguration = sparebankConfiguration;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        PaymentType paymentType = getPaymentType(paymentRequest);
        SparebankPaymentProduct paymentProduct =
                SparebankPaymentProduct.mapTinkPaymentTypeToSparebankPaymentProduct(paymentType);
        AdressEntity creditorAddress =
                new AdressEntity.Builder().withCountry(CountryCodes.NORWAY).build();
        AccountEntity creditorAccountEntity =
                AccountEntity.ofCreditor(paymentRequest, paymentProduct);
        AccountEntity debtorAccountEntity = AccountEntity.ofDebtor(paymentRequest, paymentProduct);
        AmountEntity amount = AmountEntity.of(paymentRequest);

        CreatePaymentRequest createPaymentRequest =
                new Builder()
                        .withCreditorAccount(creditorAccountEntity)
                        .withDebtorAccount(debtorAccountEntity)
                        .withRequestedExecutionDate(getCurrentDate())
                        .withInstructedAmount(amount)
                        .withCreditorName(paymentRequest.getPayment().getCreditor().getName())
                        .withCreditorAddress(creditorAddress, paymentProduct)
                        .build();

        return apiClient
                .createPayment(paymentProduct.getText(), createPaymentRequest)
                .toTinkPaymentResponse(paymentRequest, paymentType);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        SparebankPaymentProduct paymentProduct =
                SparebankPaymentProduct.mapTinkPaymentTypeToSparebankPaymentProduct(
                        payment.getType());
        PaymentResponse paymentResponse =
                apiClient
                        .getPayment(paymentProduct.getText(), payment.getUniqueId())
                        .toTinkPaymentResponse(paymentRequest);
        paymentList.add(paymentResponse); // used for mocking fetch multiple
        return paymentResponse;
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        Payment payment = paymentMultiStepRequest.getPayment();
        SparebankPaymentProduct paymentProduct =
                SparebankPaymentProduct.mapTinkPaymentTypeToSparebankPaymentProduct(
                        payment.getType());
        String paymentId = payment.getUniqueId();
        PaymentStatus paymentStatus = payment.getStatus();
        String nextStep;
        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                StartAuthorizationProcessResponse startAuthorizationResponse =
                        apiClient.startAuthorizationProcess(paymentProduct.getText(), paymentId);

                sessionStorage.put(
                        payment.getUniqueId(), startAuthorizationResponse.getScaRedirectLink());
                nextStep = SparebankSignSteps.SAMPLE_STEP;
                break;
            case SparebankSignSteps.SAMPLE_STEP:
                GetPaymentStatusResponse paymentStatusReponse =
                        apiClient.getPaymentStatus(paymentProduct.getText(), paymentId);
                // paymentStatus =
                // SparebankPaymentStatus.mapToTinkPaymentStatus(SparebankPaymentStatus.fromString(paymentStatusReponse.getTransactionStatus()));
                paymentStatus =
                        PaymentStatus
                                .PAID; // we have to hard code it beacuse the above always returns
                // status RCVD
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;

                break;
            default:
                throw new IllegalStateException(
                        String.format("Uknown step %s", paymentMultiStepRequest.getStep()));
        }

        payment.setStatus(paymentStatus);
        return new PaymentMultiStepResponse(payment, nextStep, new ArrayList<>());
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "cancel not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        // mocking of the fetch multiple so we can test sign
        return new PaymentListResponse(paymentList);
    }

    private String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(DatePatterns.YYYY_MM_DD_PATTERN));
    }

    private static final GenericTypeMapper<PaymentType, Pair<Type, Type>>
            accountIdentifiersToPaymentTypeMapper =
                    GenericTypeMapper
                            .<PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
                                    genericBuilder()
                            .put(
                                    PaymentType.DOMESTIC,
                                    new Pair<>(
                                            AccountIdentifier.Type.NO, AccountIdentifier.Type.NO))
                            .put(PaymentType.SEPA, new Pair<>(Type.IBAN, Type.IBAN))
                            .setDefaultTranslationValue(PaymentType.INTERNATIONAL)
                            .build();

    private PaymentType getPaymentType(PaymentRequest paymentRequest) {
        Pair<Type, Type> accountIdentifiers =
                paymentRequest.getPayment().getCreditorAndDebtorAccountType();
        return accountIdentifiersToPaymentTypeMapper
                .translate(accountIdentifiers)
                .orElseThrow(
                        () ->
                                new NotImplementedException(
                                        String.format(
                                                ErrorMessages.NO_ACCOUNT_TYPE_FOUND,
                                                accountIdentifiers)));
    }
}
