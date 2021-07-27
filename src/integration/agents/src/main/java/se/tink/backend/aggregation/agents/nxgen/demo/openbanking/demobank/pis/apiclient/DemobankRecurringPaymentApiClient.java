package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient;

import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.PAYMENT_CLIENT_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.PAYMENT_CLIENT_TOKEN_HEADER;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.QueryParams.PAYMENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.QueryParams.PAYMENT_SCHEME;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls.CREATE_RECURRING_PAYMENT;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls.GET_RECURRING_PAYMENT;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls.RECURRING_PAYMENT_STATUS;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.DemobankDtoMappers;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.AccountIdentifierDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.AmountDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.PaymentStatusResponseDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.RecurringPaymentInitiationDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.RecurringPaymentResponseDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.error.DemobankErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.storage.DemobankStorage;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DemobankRecurringPaymentApiClient extends DemobankPaymentApiClient {

    public DemobankRecurringPaymentApiClient(
            DemobankDtoMappers mappers,
            DemobankErrorHandler errorHandler,
            DemobankPaymentRequestFilter requestFilter,
            DemobankStorage storage,
            TinkHttpClient client,
            String callbackUri) {
        super(mappers, errorHandler, requestFilter, storage, client, callbackUri);
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest paymentRequest) throws PaymentException {
        final RecurringPaymentInitiationDto paymentInitiationDto =
                createRecurringPaymentInitiationDto(paymentRequest);

        try {

            final String paymentScheme = getPaymentScheme(paymentRequest);
            final RecurringPaymentResponseDto paymentResponseDto =
                    client.request(
                                    CREATE_RECURRING_PAYMENT.parameter(
                                            PAYMENT_SCHEME, paymentScheme))
                            .header(PAYMENT_CLIENT_TOKEN_HEADER, PAYMENT_CLIENT_TOKEN)
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .accept(MediaType.APPLICATION_JSON_TYPE)
                            .post(RecurringPaymentResponseDto.class, paymentInitiationDto);

            saveLinksToStorage(paymentResponseDto.getId(), paymentResponseDto.getLinks());

            return convertResponseDtoToPaymentResponse(paymentResponseDto);
        } catch (HttpResponseException e) {
            errorHandler.remapException(e);
            throw e;
        }
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        final RecurringPaymentResponseDto paymentResponseDto =
                client.request(GET_RECURRING_PAYMENT.parameter(PAYMENT_ID, paymentId))
                        .addFilter(requestFilter)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(RecurringPaymentResponseDto.class);

        return convertResponseDtoToPaymentResponse(paymentResponseDto);
    }

    @Override
    public PaymentStatus getPaymentStatus(String paymentId) {
        final PaymentStatusResponseDto paymentStatusResponseDto =
                client.request(RECURRING_PAYMENT_STATUS.parameter(PAYMENT_ID, paymentId))
                        .addFilter(requestFilter)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(PaymentStatusResponseDto.class);

        return mappers.convertPaymentStatus(paymentStatusResponseDto.getStatus());
    }

    private Payment createPayment(
            RecurringPaymentInitiationDto initiation, String paymentStatus, String paymentId) {
        final RemittanceInformation remittanceInformation =
                mappers.createRemittanceInformation(
                        initiation.getRemittanceInformationUnstructured(),
                        initiation.getRemittanceInformationStructured());
        final ExactCurrencyAmount amount =
                mappers.convertAmountDtoToExactCurrencyAmount(initiation.getAmount());

        Builder builder =
                new Builder()
                        .withExactCurrencyAmount(amount)
                        .withStatus(mappers.convertPaymentStatus(paymentStatus))
                        .withDebtor(
                                mappers.convertDebtorAccountToDebtor(initiation.getDebtorAccount()))
                        .withCreditor(
                                mappers.convertCreditorAccountToCreditor(
                                        initiation.getCreditorAccount(),
                                        initiation.getCreditorName()))
                        .withCurrency(amount.getCurrencyCode())
                        .withRemittanceInformation(remittanceInformation)
                        .withUniqueId(paymentId)
                        .withStartDate(LocalDate.parse(initiation.getStartDate()))
                        .withEndDate(LocalDate.parse(initiation.getEndDate()))
                        .withFrequency(Frequency.valueOf(initiation.getFrequency().toUpperCase()));

        switch (Frequency.valueOf(initiation.getFrequency().toUpperCase())) {
            case WEEKLY:
                builder.withDayOfWeek(DayOfWeek.of(initiation.getDayOfExecution()));
                break;
            case MONTHLY:
                builder.withDayOfMonth(initiation.getDayOfExecution());
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported frequecy " + initiation.getFrequency());
        }
        return builder.build();
    }

    private RecurringPaymentInitiationDto createRecurringPaymentInitiationDto(
            PaymentRequest paymentRequest) {
        final Payment payment = paymentRequest.getPayment();

        final Optional<AccountIdentifierDto> maybeDebtorAccount =
                mappers.createDebtorAccount(payment);
        final AccountIdentifierDto creditorAccount = mappers.createCreditorAccount(payment);
        final AmountDto amountDto = mappers.createAmountDto(payment);

        RecurringPaymentInitiationDto.RecurringPaymentInitiationDtoBuilder builder =
                RecurringPaymentInitiationDto.builder()
                        .creditorAccount(creditorAccount)
                        .amount(amountDto)
                        .creditorName(payment.getCreditor().getName())
                        .remittanceInformationUnstructured(
                                mappers.createUnstructuredRemittanceInfo(payment))
                        .remittanceInformationStructured(
                                mappers.createStructuredRemittanceInfo(payment))
                        .startDate(payment.getStartDate().toString())
                        .endDate(payment.getEndDate().toString())
                        .frequency(payment.getFrequency().toString())
                        .dayOfExecution(getDayOfExecution(payment));

        maybeDebtorAccount.ifPresent(builder::debtorAccount);

        return builder.build();
    }

    private int getDayOfExecution(Payment payment) {
        switch (payment.getFrequency()) {
            case WEEKLY:
                return payment.getDayOfWeek().getValue();
            case MONTHLY:
                return payment.getDayOfMonth();
            default:
                throw new IllegalArgumentException(
                        "Frequency is not supported: " + payment.getFrequency());
        }
    }

    private PaymentResponse convertResponseDtoToPaymentResponse(
            RecurringPaymentResponseDto response) {
        final Payment payment =
                createPayment(
                        response.getRecurringPaymentInitiation(),
                        response.getPaymentStatus(),
                        response.getId());

        return new PaymentResponse(payment, new Storage());
    }
}
