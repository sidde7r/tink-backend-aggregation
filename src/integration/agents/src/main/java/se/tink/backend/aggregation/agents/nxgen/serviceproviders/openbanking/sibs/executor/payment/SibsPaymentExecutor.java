package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc.SibsPaymentInitiationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign.SignPaymentStrategy;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
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
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SibsPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final SibsBaseApiClient apiClient;
    private final SignPaymentStrategy signPaymentStrategy;
    private final StrongAuthenticationState strongAuthenticationState;

    public SibsPaymentExecutor(
            SibsBaseApiClient apiClient,
            SignPaymentStrategy signPaymentStrategy,
            StrongAuthenticationState strongAuthenticationState) {
        this.apiClient = apiClient;
        this.signPaymentStrategy = signPaymentStrategy;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentValidationException {

        SibsPaymentInitiationRequest.Builder builder =
                new SibsPaymentInitiationRequest.Builder()
                        .withCreditorAccount(
                                SibsAccountReferenceEntity.fromCreditor(
                                        paymentRequest.getPayment()))
                        .withDebtorAccount(
                                SibsAccountReferenceEntity.fromDebtor(paymentRequest.getPayment()))
                        .withInstructedAmount(
                                SibsAmountEntity.of(
                                        paymentRequest
                                                .getPayment()
                                                .getExactCurrencyAmountFromField()))
                        /*
                            PIS requirements is to provide payee name during payment initiation. Currently Tink
                            implementation doesn't allow to provide this name from the user. Temporary solution
                            is to default it to "Payment Initiation" but in the future, another field should be
                            provided in the mobile app, to allow user to provide this value which should be placed
                            as a creditorName (payee).
                        */
                        .withCreditorName(SibsConstants.FormValues.PAYMENT_INITIATION_DEFAULT_NAME)
                        /*
                            SIBS Documentation says that this field is optional and default value is SHAR if not
                            provided. However CreditoAgricola validation rules breaks if this value is not provided
                            explicitly in the code. That's why we've added this value for every bank. As a future fix
                            or flow improvement it should be chosen by user from mobile app or by algorithm (depends
                            on tech analysis.
                        */
                        .withChargeBearer(
                                SibsConstants.FormValues.PAYMENT_INITIATION_DEFAULT_CHARGE_BEARER);

        LocalDate paymentExecutionDate =
                getExecutionDate(paymentRequest.getPayment().getExecutionDate());

        if (LocalDate.now().isBefore(paymentExecutionDate)) {
            builder.withRequestedExecutionDate(paymentExecutionDate);
        }

        RemittanceInformation remittanceInformation =
                paymentRequest.getPayment().getRemittanceInformation();

        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);

        builder.withRemittanceInformationUnstructured(remittanceInformation.getValue());

        SibsPaymentInitiationRequest sibsPaymentRequest = builder.build();

        return apiClient
                .createPayment(
                        sibsPaymentRequest,
                        SibsPaymentType.fromDomainPayment(paymentRequest.getPayment()),
                        strongAuthenticationState.getState())
                .toTinkPaymentResponse(paymentRequest, strongAuthenticationState.getState());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        return apiClient
                .getPayment(
                        paymentRequest.getPayment().getUniqueId(),
                        SibsPaymentType.fromDomainPayment(paymentRequest.getPayment()))
                .toTinkPaymentResponse(paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        return signPaymentStrategy.sign(
                paymentMultiStepRequest,
                SibsPaymentType.fromDomainPayment(paymentMultiStepRequest.getPayment()));
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        return apiClient
                .cancelPayment(
                        paymentRequest.getPayment().getUniqueId(),
                        SibsPaymentType.fromDomainPayment(paymentRequest.getPayment()))
                .toTinkResponse();
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        List<PaymentResponse> response = new ArrayList<>();
        for (PaymentRequest request : paymentListRequest.getPaymentRequestList()) {
            response.add(fetch(request));
        }

        return new PaymentListResponse(response);
    }

    private LocalDate getExecutionDate(LocalDate executionDate) {
        if (executionDate == null) {
            CountryDateHelper countryDateHelper =
                    new CountryDateHelper(
                            new Locale(
                                    CountryDateHelper.LANGUAGE_CODE_PORTUGAL,
                                    CountryDateHelper.COUNTRY_CODE_PORTUGAL));
            return countryDateHelper.getNowAsLocalDate();
        } else {
            return executionDate;
        }
    }
}
