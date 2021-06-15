package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient;

import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.OAuth2Params.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.OAuth2Params.CLIENT_SECRET;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.PAYMENT_CLIENT_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.PAYMENT_CLIENT_TOKEN_HEADER;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls.BASE_URL;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls.OAUTH_TOKEN;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.RedirectLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.DemobankDtoMappers;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.AccountIdentifierDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.AmountDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.LinksDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.PaymentStatusResponseDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.RecurringPaymentInitiationDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.RecurringPaymentResponseDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.error.DemobankErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.storage.DemobankStorage;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@RequiredArgsConstructor
public class DemobankRecurringPaymentApiClient implements DemobankPaymentApiClient {
    private static final String GET_RECURRING_PAYMENT_URL = "/api/payment/v1/recurring-payment/";
    private static final String CREATE_RECURRING_PAYMENT_URL =
            GET_RECURRING_PAYMENT_URL + "domestic/create";
    private static final String GET_RECURRING_PAYMENT_STATUS_URL =
            GET_RECURRING_PAYMENT_URL + "status/";

    private final DemobankDtoMappers mappers;
    private final DemobankErrorHandler errorHandler;
    private final DemobankPaymentRequestFilter requestFilter;
    private final DemobankStorage storage;
    private final TinkHttpClient client;
    private final String callbackUri;

    @Override
    public PaymentResponse createPayment(PaymentRequest paymentRequest) throws PaymentException {
        final RecurringPaymentInitiationDto paymentInitiationDto =
                createRecurringPaymentInitiationDto(paymentRequest);

        try {
            final RecurringPaymentResponseDto paymentResponseDto =
                    client.request(BASE_URL + CREATE_RECURRING_PAYMENT_URL)
                            .header(PAYMENT_CLIENT_TOKEN_HEADER, PAYMENT_CLIENT_TOKEN)
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .accept(MediaType.APPLICATION_JSON_TYPE)
                            .post(RecurringPaymentResponseDto.class, paymentInitiationDto);

            saveToStorage(paymentResponseDto.getId(), paymentResponseDto.getLinks());

            return convertResponseDtoToPaymentResponse(paymentResponseDto);
        } catch (HttpResponseException e) {
            errorHandler.remapException(e);
            throw e;
        }
    }

    @Override
    public OAuth2Token exchangeAccessCode(String code) {
        return client.request(BASE_URL + OAUTH_TOKEN)
                .addBasicAuth(CLIENT_ID, CLIENT_SECRET)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(TokenEntity.class, new RedirectLoginRequest(code, callbackUri).toData())
                .toOAuth2Token();
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        final RecurringPaymentResponseDto paymentResponseDto =
                client.request(BASE_URL + GET_RECURRING_PAYMENT_URL + paymentId)
                        .addFilter(requestFilter)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(RecurringPaymentResponseDto.class);

        return convertResponseDtoToPaymentResponse(paymentResponseDto);
    }

    @Override
    public PaymentStatus getPaymentStatus(String paymentId) {
        final PaymentStatusResponseDto paymentStatusResponseDto =
                client.request(BASE_URL + GET_RECURRING_PAYMENT_STATUS_URL + paymentId)
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

    private void saveToStorage(String id, LinksDto links) {
        final String authorizeUrl =
                Optional.ofNullable(links)
                        .map(LinksDto::getScaRedirect)
                        .map(Href::getHref)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Response does not contain sca redirect link"));

        storage.storePaymentId(id);
        storage.storeAuthorizeUrl(authorizeUrl);
    }
}
