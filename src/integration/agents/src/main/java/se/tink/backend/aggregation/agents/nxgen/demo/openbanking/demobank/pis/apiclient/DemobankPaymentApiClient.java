package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient;

import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.OAuth2Params.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.OAuth2Params.CLIENT_SECRET;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.PAYMENT_CLIENT_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls.BASE_URL;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls.OAUTH_TOKEN;

import java.math.BigDecimal;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.RedirectLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.AccountIdentifierDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.AmountDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.LinksDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.PaymentInitiationDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.PaymentResponseDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.RemittanceInformationStructuredDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.entity.PaymentStatusDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.storage.DemobankStorage;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@RequiredArgsConstructor
public class DemobankPaymentApiClient {

    private static final String CREATE_PAYMENT_URL = "/api/payment/v1/payments/domestic/create";
    private static final String EXECUTE_PAYMENT_URL = "/api/payment/v1/payments/%s/execute";
    private static final String GET_PAYMENT_URL = "/api/payment/v1/payments/";
    private static final String PAYMENT_CLIENT_TOKEN_HEADER = "X-Client-Header";

    private final DemobankPaymentRequestFilter requestFilter;
    private final DemobankStorage storage;
    private final TinkHttpClient client;
    private final String callbackUri;

    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        final PaymentInitiationDto paymentInitiationDto =
                createPaymentInitiationDto(paymentRequest);

        final PaymentResponseDto paymentResponseDto =
                client.request(BASE_URL + CREATE_PAYMENT_URL)
                        .header(PAYMENT_CLIENT_TOKEN_HEADER, PAYMENT_CLIENT_TOKEN)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .post(PaymentResponseDto.class, paymentInitiationDto);

        saveToStorage(paymentResponseDto);

        return convertResponseDtoToPaymentResponse(paymentResponseDto);
    }

    public OAuth2Token exchangeAccessCode(String code) {
        return client.request(BASE_URL + OAUTH_TOKEN)
                .addBasicAuth(CLIENT_ID, CLIENT_SECRET)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(TokenEntity.class, new RedirectLoginRequest(code, callbackUri).toData())
                .toOAuth2Token();
    }

    public PaymentResponse executePayment(String paymentId) {
        final String url = String.format(BASE_URL + EXECUTE_PAYMENT_URL, paymentId);
        final PaymentResponseDto paymentResponseDto =
                client.request(url)
                        .addFilter(requestFilter)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .post(PaymentResponseDto.class);

        return convertResponseDtoToPaymentResponse(paymentResponseDto);
    }

    public PaymentResponse getPayment(String paymentId) {
        final PaymentResponseDto paymentResponseDto =
                client.request(BASE_URL + GET_PAYMENT_URL + paymentId)
                        .addFilter(requestFilter)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(PaymentResponseDto.class);

        return convertResponseDtoToPaymentResponse(paymentResponseDto);
    }

    private void saveToStorage(PaymentResponseDto response) {
        final String authorizeUrl =
                Optional.ofNullable(response.getLinks())
                        .map(LinksDto::getScaRedirect)
                        .map(Href::getHref)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Response does not contain sca redirect link"));

        storage.storePaymentId(response.getId());
        storage.storeAuthorizeUrl(authorizeUrl);
    }

    private static PaymentInitiationDto createPaymentInitiationDto(PaymentRequest paymentRequest) {
        final Payment payment = paymentRequest.getPayment();

        final AccountIdentifierDto debtorAccount = createDebtorAccount(payment);
        final AccountIdentifierDto creditorAccount = createCreditorAccount(payment);
        final AmountDto amountDto = createAmountDto(payment);

        return PaymentInitiationDto.builder()
                .debtorAccount(debtorAccount)
                .creditorAccount(creditorAccount)
                .amount(amountDto)
                .creditorName(payment.getCreditor().getName())
                .remittanceInformationUnstructured(createUnstructuredRemittanceInfo(payment))
                .remittanceInformationStructured(createStructuredRemittanceInfo(payment))
                .initiationDate(payment.getExecutionDate().toString())
                .build();
    }

    private static AccountIdentifierDto createDebtorAccount(Payment payment) {
        final Debtor debtor = payment.getDebtor();

        return AccountIdentifierDto.builder()
                .accountId(debtor.getAccountNumber())
                .identifier(debtor.getAccountIdentifier().getIdentifier())
                .type(debtor.getAccountIdentifierType())
                .build();
    }

    private static AccountIdentifierDto createCreditorAccount(Payment payment) {
        final Creditor creditor = payment.getCreditor();

        return AccountIdentifierDto.builder()
                .accountId(creditor.getAccountNumber())
                .identifier(creditor.getAccountIdentifier().getIdentifier())
                .type(creditor.getAccountIdentifierType())
                .build();
    }

    private static AmountDto createAmountDto(Payment payment) {
        return AmountDto.builder()
                .amountValue(payment.getExactCurrencyAmount().getExactValue().toString())
                .currency(payment.getExactCurrencyAmount().getCurrencyCode())
                .build();
    }

    private static String createUnstructuredRemittanceInfo(Payment payment) {
        return (payment.getRemittanceInformation().getType()
                        == RemittanceInformationType.UNSTRUCTURED)
                ? payment.getRemittanceInformation().getValue()
                : null;
    }

    private static RemittanceInformationStructuredDto createStructuredRemittanceInfo(
            Payment payment) {
        return (payment.getRemittanceInformation().getType() == RemittanceInformationType.REFERENCE)
                ? RemittanceInformationStructuredDto.builder()
                        .reference(payment.getRemittanceInformation().getValue())
                        .referenceType(RemittanceInformationType.REFERENCE.name())
                        .build()
                : null;
    }

    private static PaymentResponse convertResponseDtoToPaymentResponse(
            PaymentResponseDto response) {
        final Payment payment =
                createPayment(
                        response.getPaymentInitiation(),
                        response.getPaymentStatus(),
                        response.getId());

        return new PaymentResponse(payment, new Storage());
    }

    private static Payment createPayment(
            PaymentInitiationDto initiation, String paymentStatus, String paymentId) {
        final RemittanceInformation remittanceInformation = createRemittanceInformation(initiation);
        final ExactCurrencyAmount amount =
                convertAmountDtoToExactCurrencyAmount(initiation.getAmount());

        return new Payment.Builder()
                .withExactCurrencyAmount(amount)
                .withStatus(convertPaymentStatus(paymentStatus))
                .withDebtor(convertDebtorAccountToDebtor(initiation.getDebtorAccount()))
                .withCreditor(
                        convertCreditorAccountToCreditor(
                                initiation.getCreditorAccount(), initiation.getCreditorName()))
                .withCurrency(amount.getCurrencyCode())
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId(paymentId)
                .build();
    }

    private static RemittanceInformation createRemittanceInformation(
            PaymentInitiationDto initiation) {
        final RemittanceInformation remittanceInformation = new RemittanceInformation();

        if (StringUtils.isNotBlank(initiation.getRemittanceInformationUnstructured())) {
            remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
            remittanceInformation.setValue(initiation.getRemittanceInformationUnstructured());
        } else {
            remittanceInformation.setType(RemittanceInformationType.REFERENCE);
            remittanceInformation.setValue(
                    initiation.getRemittanceInformationStructured().getReference());
        }

        return remittanceInformation;
    }

    private static ExactCurrencyAmount convertAmountDtoToExactCurrencyAmount(AmountDto amountDto) {
        return new ExactCurrencyAmount(
                new BigDecimal(amountDto.getAmountValue()), amountDto.getCurrency());
    }

    private static Debtor convertDebtorAccountToDebtor(AccountIdentifierDto accountIdentifier) {
        return Optional.ofNullable(accountIdentifier)
                .map(DemobankPaymentApiClient::createAccountIdentifier)
                .map(Debtor::new)
                .orElse(null);
    }

    private static Creditor convertCreditorAccountToCreditor(
            AccountIdentifierDto accountIdentifier, String creditorName) {
        return new Creditor(createAccountIdentifier(accountIdentifier), creditorName);
    }

    private static AccountIdentifier createAccountIdentifier(
            AccountIdentifierDto accountIdentifier) {
        return new IbanIdentifier(accountIdentifier.getIdentifier());
    }

    private static PaymentStatus convertPaymentStatus(String paymentStatus) {
        final PaymentStatusDto paymentStatusDto =
                PaymentStatusDto.createFromFullName(paymentStatus);

        switch (paymentStatusDto) {
            case RCVD:
                return PaymentStatus.CREATED;
            case ACTC:
                return PaymentStatus.SIGNED;
            case ACSC:
                return PaymentStatus.PAID;
            case CANC:
                return PaymentStatus.CANCELLED;
            case RJCT:
                return PaymentStatus.REJECTED;
            default:
                return PaymentStatus.UNDEFINED;
        }
    }
}
