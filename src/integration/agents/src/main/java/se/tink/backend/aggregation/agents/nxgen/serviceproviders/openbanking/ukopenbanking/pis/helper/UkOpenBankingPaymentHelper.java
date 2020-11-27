package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingV31PaymentConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.common.FundsConfirmationResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@RequiredArgsConstructor
public class UkOpenBankingPaymentHelper {

    // TODO: add all possible permutations
    @SuppressWarnings("unchecked")
    private static final GenericTypeMapper<
                    PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
            PAYMENT_TYPE_MAPPER =
                    GenericTypeMapper
                            .<PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
                                    genericBuilder()
                            .put(
                                    PaymentType.DOMESTIC,
                                    new Pair<>(null, AccountIdentifier.Type.SORT_CODE),
                                    new Pair<>(
                                            AccountIdentifier.Type.SORT_CODE,
                                            AccountIdentifier.Type.SORT_CODE),
                                    new Pair<>(
                                            AccountIdentifier.Type.PAYM_PHONE_NUMBER,
                                            AccountIdentifier.Type.PAYM_PHONE_NUMBER))
                            .put(
                                    PaymentType.SEPA,
                                    new Pair<>(null, AccountIdentifier.Type.IBAN),
                                    new Pair<>(
                                            AccountIdentifier.Type.IBAN,
                                            AccountIdentifier.Type.IBAN))
                            .build();

    private final Map<PaymentType, ApiClientWrapper> apiClientWrapperMap;
    private final Clock clock;

    public PaymentResponse createConsent(PaymentRequest paymentRequest) {
        final ApiClientWrapper apiClientWrapper = getApiClientWrapper(paymentRequest.getPayment());

        return apiClientWrapper.createPaymentConsent(paymentRequest);
    }

    public PaymentResponse fetchPaymentIfAlreadyExecutedOrGetConsent(
            PaymentRequest paymentRequest) {
        final ApiClientWrapper apiClientWrapper = getApiClientWrapper(paymentRequest.getPayment());

        return getPaymentId(paymentRequest)
                .map(apiClientWrapper::getPayment)
                .orElseGet(() -> apiClientWrapper.getPaymentConsent(getConsentId(paymentRequest)));
    }

    public Optional<FundsConfirmationResponse> fetchFundsConfirmation(
            PaymentRequest paymentRequest) {
        final ApiClientWrapper apiClientWrapper = getApiClientWrapper(paymentRequest.getPayment());

        return apiClientWrapper.getFundsConfirmation(getConsentId(paymentRequest));
    }

    public PaymentResponse executePayment(
            PaymentRequest paymentRequest,
            String endToEndIdentification,
            String instructionIdentification) {

        final ApiClientWrapper apiClientWrapper = getApiClientWrapper(paymentRequest.getPayment());

        return apiClientWrapper.executePayment(
                paymentRequest,
                getConsentId(paymentRequest),
                endToEndIdentification,
                instructionIdentification);
    }

    private static Optional<String> getPaymentId(PaymentRequest paymentRequest) {
        return Optional.ofNullable(
                paymentRequest
                        .getStorage()
                        .get(UkOpenBankingV31PaymentConstants.Storage.PAYMENT_ID));
    }

    private static String getConsentId(PaymentRequest paymentRequest) {
        final Optional<String> maybeConsentId =
                Optional.ofNullable(
                        paymentRequest
                                .getStorage()
                                .get(UkOpenBankingV31PaymentConstants.Storage.CONSENT_ID));

        return maybeConsentId.orElseThrow(
                () -> new IllegalArgumentException(("consentId cannot be null or empty!")));
    }

    private ApiClientWrapper getApiClientWrapper(Payment payment) {
        final PaymentType paymentType = getPaymentType(payment);

        return Optional.ofNullable(this.apiClientWrapperMap.get(paymentType))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        (String.format("Unknown type: %s", paymentType))));
    }

    private PaymentType getPaymentType(Payment payment) {
        final PaymentType translatedPaymentType =
                PAYMENT_TYPE_MAPPER
                        .translate(payment.getCreditorAndDebtorAccountType())
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                String.format(
                                                        "Cannot map Identifiers, first: %s second: %s",
                                                        payment.getCreditorAndDebtorAccountType()
                                                                .first
                                                                .toString(),
                                                        payment.getCreditorAndDebtorAccountType()
                                                                .second
                                                                .toString())));

        if (isFutureDatePayment(payment)) {
            return PaymentType.DOMESTIC_FUTURE;
        }

        return translatedPaymentType;
    }

    private boolean isFutureDatePayment(Payment payment) {
        final LocalDate currentDate = LocalDate.now(this.clock);

        return Objects.nonNull(payment.getExecutionDate())
                && payment.getExecutionDate().isAfter(currentDate);
    }
}
