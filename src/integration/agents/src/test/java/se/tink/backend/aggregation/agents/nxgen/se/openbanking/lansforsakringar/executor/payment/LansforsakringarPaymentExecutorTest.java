package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.SignBasketResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.BasketLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.DomesticTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.GirosCreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.ResponseAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.CreateBasketResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.DomesticGirosPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.DomesticPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.DomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.GetDomesticPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class LansforsakringarPaymentExecutorTest {

    public static final String CREDITOR_ACCOUNT_NUMBER = "33820000000";
    public static final String DEBTOR_ACCOUNT_NUMBER = "33820000001";
    public static final String GIRO_CREDITOR_ACCOUNT_NUMBER = "900-8004";
    public static final String CURRENCY_CODE = "SEK";
    public static final String SIGNED_TRANSACTION_STATUS = "ACTC";
    public static final String RECEIVED_TRANSACTION_STATUS = "RCVD";
    private LansforsakringarPaymentExecutor paymentExecutor;
    private LansforsakringarApiClient apiClient;
    private SupplementalInformationHelper supplementalInformationHelper;
    private StrongAuthenticationState strongAuthenticationState;

    @Before
    public void setup() {
        apiClient = mock(LansforsakringarApiClient.class);
        strongAuthenticationState = mock(StrongAuthenticationState.class);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);

        paymentExecutor =
                new LansforsakringarPaymentExecutor(
                        apiClient, supplementalInformationHelper, strongAuthenticationState);
    }

    @SneakyThrows
    @Test
    public void testCreateDomesticPayment() {
        // given
        Creditor creditor =
                new Creditor(
                        AccountIdentifier.create(
                                AccountIdentifierType.SE, CREDITOR_ACCOUNT_NUMBER));
        Debtor debtor =
                new Debtor(
                        AccountIdentifier.create(AccountIdentifierType.SE, DEBTOR_ACCOUNT_NUMBER));
        ExactCurrencyAmount amount = ExactCurrencyAmount.inSEK(0.02);
        LocalDate executionDate = LocalDate.of(2021, 2, 17);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("Reference");
        PaymentRequest paymentRequest =
                new PaymentRequest(
                        new Payment.Builder()
                                .withCreditor(creditor)
                                .withDebtor(debtor)
                                .withExactCurrencyAmount(amount)
                                .withCurrency(CURRENCY_CODE)
                                .withRemittanceInformation(remittanceInformation)
                                .withExecutionDate(executionDate)
                                .build());

        final DomesticPaymentRequest domesticPaymentRequest =
                new DomesticPaymentRequest(
                        new AccountEntity(creditor.getAccountNumber(), CURRENCY_CODE),
                        new AccountEntity(debtor.getAccountNumber(), CURRENCY_CODE),
                        new AmountEntity(amount),
                        executionDate.format(DateTimeFormatter.ISO_DATE),
                        remittanceInformation.getValue());

        final DomesticPaymentResponse domesticPaymentResponse = new DomesticPaymentResponse();
        domesticPaymentResponse.setTransactionStatus(RECEIVED_TRANSACTION_STATUS);

        when(apiClient.getAccountNumbers())
                .thenReturn(
                        Optional.of(
                                AccountNumbersUtil.getAccountNumbersResponse(
                                        DEBTOR_ACCOUNT_NUMBER, "")));

        when(apiClient.createDomesticPayment(eq(domesticPaymentRequest)))
                .thenReturn(domesticPaymentResponse);

        // when
        PaymentResponse paymentResponse = paymentExecutor.create(paymentRequest);

        // then
        Assertions.assertThat(paymentResponse.getPayment().getStatus())
                .isEqualTo(PaymentStatus.PENDING);
        Assertions.assertThat(paymentResponse.getPayment().getCreditor().getAccountNumber())
                .isEqualTo(CREDITOR_ACCOUNT_NUMBER);
        Assertions.assertThat(
                        paymentResponse.getPayment().getCreditor().getAccountIdentifier().getType())
                .isEqualTo(AccountIdentifierType.SE);
        Assertions.assertThat(
                        paymentResponse.getPayment().getExactCurrencyAmount().getDoubleValue())
                .isEqualTo(0.02);
        Assertions.assertThat(
                        paymentResponse.getPayment().getExactCurrencyAmount().getCurrencyCode())
                .isEqualTo(CURRENCY_CODE);
    }

    @SneakyThrows
    @Test
    public void testCreateDomesticGirosPayment() {
        // given
        Creditor creditor =
                new Creditor(
                        AccountIdentifier.create(
                                AccountIdentifierType.SE_BG, GIRO_CREDITOR_ACCOUNT_NUMBER));
        Debtor debtor =
                new Debtor(
                        AccountIdentifier.create(AccountIdentifierType.SE, DEBTOR_ACCOUNT_NUMBER));
        ExactCurrencyAmount exactCurrencyAmount = ExactCurrencyAmount.inSEK(0.02);
        LocalDate executionDate = LocalDate.of(2021, 2, 17);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("Reference");
        PaymentRequest paymentRequest =
                new PaymentRequest(
                        new Payment.Builder()
                                .withCreditor(creditor)
                                .withDebtor(debtor)
                                .withExactCurrencyAmount(exactCurrencyAmount)
                                .withCurrency(CURRENCY_CODE)
                                .withRemittanceInformation(remittanceInformation)
                                .withExecutionDate(executionDate)
                                .build());

        final DomesticGirosPaymentRequest domesticGirosPaymentRequest =
                new DomesticGirosPaymentRequest(
                        new GirosCreditorAccountEntity(
                                GIRO_CREDITOR_ACCOUNT_NUMBER, AccountIdentifierType.SE_BG),
                        new AccountEntity(debtor.getAccountNumber(), CURRENCY_CODE),
                        new AmountEntity(exactCurrencyAmount),
                        executionDate.format(DateTimeFormatter.ISO_DATE),
                        remittanceInformation.getValue(),
                        null);

        final DomesticPaymentResponse domesticPaymentResponse = new DomesticPaymentResponse();
        domesticPaymentResponse.setTransactionStatus(RECEIVED_TRANSACTION_STATUS);

        when(apiClient.getAccountNumbers())
                .thenReturn(
                        Optional.of(
                                AccountNumbersUtil.getAccountNumbersResponse(
                                        DEBTOR_ACCOUNT_NUMBER, "")));

        when(apiClient.createDomesticGirosPayment(eq(domesticGirosPaymentRequest)))
                .thenReturn(domesticPaymentResponse);

        // when
        PaymentResponse paymentResponse = paymentExecutor.create(paymentRequest);

        // then
        Assertions.assertThat(paymentResponse.getPayment().getStatus())
                .isEqualTo(PaymentStatus.PENDING);
        Assertions.assertThat(paymentResponse.getPayment().getCreditor().getAccountNumber())
                .isEqualTo("9008004");
        Assertions.assertThat(
                        paymentResponse.getPayment().getCreditor().getAccountIdentifier().getType())
                .isEqualTo(AccountIdentifierType.SE_BG);
        Assertions.assertThat(
                        paymentResponse.getPayment().getExactCurrencyAmount().getDoubleValue())
                .isEqualTo(0.02);
        Assertions.assertThat(
                        paymentResponse.getPayment().getExactCurrencyAmount().getCurrencyCode())
                .isEqualTo(CURRENCY_CODE);
    }

    @Test
    public void testUnknownPaymentSchemeForPayment() {
        // given
        Creditor creditor =
                new Creditor(
                        AccountIdentifier.create(
                                AccountIdentifierType.BBAN, GIRO_CREDITOR_ACCOUNT_NUMBER));
        Debtor debtor =
                new Debtor(
                        AccountIdentifier.create(AccountIdentifierType.SE, DEBTOR_ACCOUNT_NUMBER));
        ExactCurrencyAmount amount = ExactCurrencyAmount.inSEK(0.02);
        PaymentRequest paymentRequest =
                new PaymentRequest(
                        new Payment.Builder()
                                .withCreditor(creditor)
                                .withDebtor(debtor)
                                .withExactCurrencyAmount(amount)
                                .build());

        when(apiClient.getAccountNumbers())
                .thenReturn(
                        Optional.of(
                                AccountNumbersUtil.getAccountNumbersResponse(
                                        DEBTOR_ACCOUNT_NUMBER, "")));

        // when
        Throwable thrown = catchThrowable(() -> paymentExecutor.create(paymentRequest));

        // then
        Assertions.assertThat(thrown).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void signShouldSignAndAuthenticateExternalPayment() throws PaymentException {
        // given
        String scaStatus = "RECEIVED";
        String id = "1234";
        String urlString = "url";
        URL url = new URL(urlString);

        Payment initialPayment =
                new Payment.Builder().withUniqueId(id).withType(PaymentType.DOMESTIC).build();
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        initialPayment,
                        new Storage(),
                        AuthenticationStepConstants.STEP_INIT,
                        Collections.emptyList());

        prepareSigning(url, id, scaStatus);

        // when
        PaymentMultiStepResponse response = paymentExecutor.sign(paymentRequest);

        // then
        Assertions.assertThat(response.getStep())
                .isEqualTo(AuthenticationStepConstants.STEP_FINALIZE);

        Assertions.assertThat(response.getPayment().getStatus()).isEqualTo(PaymentStatus.SIGNED);
        Assertions.assertThat(response.getPayment().getCreditor().getAccountNumber())
                .isEqualTo(CREDITOR_ACCOUNT_NUMBER);
        Assertions.assertThat(response.getPayment().getCreditor().getAccountIdentifierType())
                .isEqualTo(AccountIdentifierType.SE);
        Assertions.assertThat(response.getPayment().getDebtor().getAccountNumber())
                .isEqualTo(DEBTOR_ACCOUNT_NUMBER);
        Assertions.assertThat(response.getPayment().getDebtor().getAccountIdentifierType())
                .isEqualTo(AccountIdentifierType.SE);
        Assertions.assertThat(response.getPayment().getExactCurrencyAmount().getExactValue())
                .isEqualTo(BigDecimal.valueOf(1.0));
        Assertions.assertThat(response.getPayment().getExactCurrencyAmount().getCurrencyCode())
                .isEqualTo(CURRENCY_CODE);

        verify(supplementalInformationHelper)
                .openThirdPartyApp(eq(ThirdPartyAppAuthenticationPayload.of(url)));
    }

    @Test
    public void signShouldSignInternalPayment() throws PaymentException {
        // given
        String scaStatus = "EXEMPTED";
        String id = "1234";
        String urlString = "url";
        URL url = new URL(urlString);

        Payment initialPayment =
                new Payment.Builder().withUniqueId(id).withType(PaymentType.DOMESTIC).build();
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        initialPayment,
                        new Storage(),
                        AuthenticationStepConstants.STEP_INIT,
                        Collections.emptyList());

        prepareSigning(url, id, scaStatus);

        // when
        PaymentMultiStepResponse response = paymentExecutor.sign(paymentRequest);

        // then
        Assertions.assertThat(response.getStep())
                .isEqualTo(AuthenticationStepConstants.STEP_FINALIZE);

        Assertions.assertThat(response.getPayment().getStatus()).isEqualTo(PaymentStatus.SIGNED);
        Assertions.assertThat(response.getPayment().getCreditor().getAccountNumber())
                .isEqualTo(CREDITOR_ACCOUNT_NUMBER);
        Assertions.assertThat(response.getPayment().getCreditor().getAccountIdentifierType())
                .isEqualTo(AccountIdentifierType.SE);
        Assertions.assertThat(response.getPayment().getDebtor().getAccountNumber())
                .isEqualTo(DEBTOR_ACCOUNT_NUMBER);
        Assertions.assertThat(response.getPayment().getDebtor().getAccountIdentifierType())
                .isEqualTo(AccountIdentifierType.SE);
        Assertions.assertThat(response.getPayment().getExactCurrencyAmount().getExactValue())
                .isEqualTo(BigDecimal.valueOf(1.0));
        Assertions.assertThat(response.getPayment().getExactCurrencyAmount().getCurrencyCode())
                .isEqualTo(CURRENCY_CODE);

        Assertions.assertThat(response.getPayment().getStatus()).isEqualTo(PaymentStatus.SIGNED);
    }

    private void prepareSigning(URL url, String id, String scaStatus) {

        String authUrl = "authUrl";
        String basketId = "basketId";
        String authId = "authId";
        String state = "state";

        CreateBasketResponse createBasketResponse = mock(CreateBasketResponse.class);
        BasketLinksEntity basketLinksEntity = mock(BasketLinksEntity.class);

        when(strongAuthenticationState.getState()).thenReturn(state);

        when(createBasketResponse.getLinks()).thenReturn(basketLinksEntity);
        when(createBasketResponse.getBasketId()).thenReturn(basketId);
        when(basketLinksEntity.getAuthorizationUrl()).thenReturn(authUrl);

        when(apiClient.createSigningBasket(eq(id))).thenReturn(createBasketResponse);

        SignBasketResponse signBasketResponse = mock(SignBasketResponse.class);
        when(signBasketResponse.getScaStatus()).thenReturn(scaStatus);
        when(signBasketResponse.getAuthorisationId()).thenReturn(authId);

        when(apiClient.signBasket(eq(authUrl), eq(basketId))).thenReturn(signBasketResponse);

        when(apiClient.buildAuthorizeUrl(eq(state), eq(authId))).thenReturn(url);

        when(strongAuthenticationState.getSupplementalKey()).thenReturn(state);

        GetDomesticPaymentResponse getDomesticPaymentResponse =
                new GetDomesticPaymentResponse(
                        DomesticTransactionEntity.builder()
                                .amount(
                                        new AmountEntity(
                                                new ExactCurrencyAmount(
                                                        BigDecimal.valueOf(1.0), CURRENCY_CODE)))
                                .creditorAccount(
                                        new ResponseAccountEntity(
                                                CREDITOR_ACCOUNT_NUMBER, CURRENCY_CODE))
                                .debtorAccount(
                                        new ResponseAccountEntity(
                                                DEBTOR_ACCOUNT_NUMBER, CURRENCY_CODE))
                                .transactionStatus(SIGNED_TRANSACTION_STATUS)
                                .build());
        when(apiClient.getDomesticPayment(eq(id))).thenReturn(getDomesticPaymentResponse);
    }
}
