package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient;

import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.PAYMENT_CLIENT_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.PAYMENT_CLIENT_TOKEN_HEADER;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls.BASE_URL;

import java.time.LocalDate;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.DemobankDtoMappers;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.AccountIdentifierDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.AmountDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.PaymentInitiationDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.PaymentResponseDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.PaymentStatusResponseDto;
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
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DemobankSinglePaymentApiClient extends DemobankPaymentApiClient {
    private static final String CREATE_PAYMENT_URL = "/api/payment/v1/payments/%s/create";
    private static final String GET_PAYMENT_URL = "/api/payment/v1/payments/";
    private static final String GET_PAYMENT_STATUS_URL = "/api/payment/v1/status/";

    public DemobankSinglePaymentApiClient(
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
        final PaymentInitiationDto paymentInitiationDto =
                createPaymentInitiationDto(paymentRequest);

        try {
            final String paymentProduct = getPaymentScheme(paymentRequest);
            final PaymentResponseDto paymentResponseDto =
                    client.request(BASE_URL + String.format(CREATE_PAYMENT_URL, paymentProduct))
                            .header(PAYMENT_CLIENT_TOKEN_HEADER, PAYMENT_CLIENT_TOKEN)
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .accept(MediaType.APPLICATION_JSON_TYPE)
                            .post(PaymentResponseDto.class, paymentInitiationDto);

            saveLinksToStorage(paymentResponseDto.getId(), paymentResponseDto.getLinks());

            return convertResponseDtoToPaymentResponse(paymentResponseDto);
        } catch (HttpResponseException e) {
            errorHandler.remapException(e);
            throw e;
        }
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        final PaymentResponseDto paymentResponseDto =
                client.request(BASE_URL + GET_PAYMENT_URL + paymentId)
                        .addFilter(requestFilter)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(PaymentResponseDto.class);

        return convertResponseDtoToPaymentResponse(paymentResponseDto);
    }

    @Override
    public PaymentStatus getPaymentStatus(String paymentId) {
        final PaymentStatusResponseDto paymentStatusResponseDto =
                client.request(BASE_URL + GET_PAYMENT_STATUS_URL + paymentId)
                        .addFilter(requestFilter)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(PaymentStatusResponseDto.class);

        return mappers.convertPaymentStatus(paymentStatusResponseDto.getStatus());
    }

    private Payment createPayment(
            PaymentInitiationDto initiation, String paymentStatus, String paymentId) {
        final RemittanceInformation remittanceInformation =
                mappers.createRemittanceInformation(
                        initiation.getRemittanceInformationUnstructured(),
                        initiation.getRemittanceInformationStructured());
        final ExactCurrencyAmount amount =
                mappers.convertAmountDtoToExactCurrencyAmount(initiation.getAmount());

        return new Payment.Builder()
                .withExactCurrencyAmount(amount)
                .withStatus(mappers.convertPaymentStatus(paymentStatus))
                .withDebtor(mappers.convertDebtorAccountToDebtor(initiation.getDebtorAccount()))
                .withCreditor(
                        mappers.convertCreditorAccountToCreditor(
                                initiation.getCreditorAccount(), initiation.getCreditorName()))
                .withCurrency(amount.getCurrencyCode())
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId(paymentId)
                .build();
    }

    private PaymentInitiationDto createPaymentInitiationDto(PaymentRequest paymentRequest) {
        final Payment payment = paymentRequest.getPayment();

        final Optional<AccountIdentifierDto> maybeDebtorAccount =
                mappers.createDebtorAccount(payment);
        final AccountIdentifierDto creditorAccount = mappers.createCreditorAccount(payment);
        final AmountDto amountDto = mappers.createAmountDto(payment);

        PaymentInitiationDto.PaymentInitiationDtoBuilder builder =
                PaymentInitiationDto.builder()
                        .creditorAccount(creditorAccount)
                        .amount(amountDto)
                        .creditorName(payment.getCreditor().getName())
                        .remittanceInformationUnstructured(
                                mappers.createUnstructuredRemittanceInfo(payment))
                        .remittanceInformationStructured(
                                mappers.createStructuredRemittanceInfo(payment))
                        .initiationDate(getNonNullExecutionDate(payment.getExecutionDate()));

        maybeDebtorAccount.ifPresent(builder::debtorAccount);

        return builder.build();
    }

    private PaymentResponse convertResponseDtoToPaymentResponse(PaymentResponseDto response) {
        final Payment payment =
                createPayment(
                        response.getPaymentInitiation(),
                        response.getPaymentStatus(),
                        response.getId());

        return new PaymentResponse(payment, new Storage());
    }

    private String getNonNullExecutionDate(LocalDate date) {
        return date != null ? date.toString() : LocalDate.now().toString();
    }
}
