package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.GirosCreditorAccountEntity;
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
                new Creditor(AccountIdentifier.create(AccountIdentifierType.SE, "90255481251"));
        Debtor debtor =
                new Debtor(AccountIdentifier.create(AccountIdentifierType.SE, "90255481251"));
        ExactCurrencyAmount amount = ExactCurrencyAmount.inSEK(0.02);
        String currency = "SEK";
        LocalDate executionDate = LocalDate.now().plusDays(7);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("Reference");
        PaymentRequest paymentRequest =
                new PaymentRequest(
                        new Payment.Builder()
                                .withCreditor(creditor)
                                .withDebtor(debtor)
                                .withExactCurrencyAmount(amount)
                                .withCurrency(currency)
                                .withRemittanceInformation(remittanceInformation)
                                .withExecutionDate(executionDate)
                                .build());

        final DomesticPaymentRequest domesticPaymentRequest =
                new DomesticPaymentRequest(
                        new AccountEntity(creditor.getAccountNumber(), currency),
                        new AccountEntity(debtor.getAccountNumber(), currency),
                        new AmountEntity(amount),
                        executionDate.format(DateTimeFormatter.ISO_DATE),
                        remittanceInformation.getValue());

        final DomesticPaymentResponse domesticPaymentResponse = new DomesticPaymentResponse();
        domesticPaymentResponse.setTransactionStatus("RCVD");

        when(apiClient.getAccountNumbers())
                .thenReturn(
                        Optional.of(
                                AccountNumbersUtil.getAccountNumbersResponse("90255481251", "")));

        when(apiClient.createDomesticPayment(eq(domesticPaymentRequest)))
                .thenReturn(domesticPaymentResponse);

        // when
        PaymentResponse paymentResponse = paymentExecutor.create(paymentRequest);

        // then
        Assertions.assertThat(paymentResponse.getPayment().getStatus())
                .isEqualTo(PaymentStatus.PENDING);
        Assertions.assertThat(paymentResponse.getPayment().getCreditor().getAccountNumber())
                .isEqualTo("90255481251");
        Assertions.assertThat(
                        paymentResponse.getPayment().getCreditor().getAccountIdentifier().getType())
                .isEqualTo(AccountIdentifierType.SE);
        Assertions.assertThat(
                        paymentResponse.getPayment().getExactCurrencyAmount().getDoubleValue())
                .isEqualTo(0.02);
        Assertions.assertThat(
                        paymentResponse.getPayment().getExactCurrencyAmount().getCurrencyCode())
                .isEqualTo("SEK");
    }

    @SneakyThrows
    @Test
    public void testCreateDomesticGirosPayment() {
        // given
        Creditor creditor =
                new Creditor(AccountIdentifier.create(AccountIdentifierType.SE_BG, "900-8004"));
        Debtor debtor =
                new Debtor(AccountIdentifier.create(AccountIdentifierType.SE, "90255481251"));
        ExactCurrencyAmount exactCurrencyAmount = ExactCurrencyAmount.inSEK(0.02);
        String currency = "SEK";
        LocalDate executionDate = LocalDate.now().plusDays(7);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("Reference");
        PaymentRequest paymentRequest =
                new PaymentRequest(
                        new Payment.Builder()
                                .withCreditor(creditor)
                                .withDebtor(debtor)
                                .withExactCurrencyAmount(exactCurrencyAmount)
                                .withCurrency(currency)
                                .withRemittanceInformation(remittanceInformation)
                                .withExecutionDate(executionDate)
                                .build());

        final DomesticGirosPaymentRequest domesticGirosPaymentRequest =
                new DomesticGirosPaymentRequest(
                        new GirosCreditorAccountEntity("900-8004", AccountIdentifierType.SE_BG),
                        new AccountEntity(debtor.getAccountNumber(), currency),
                        new AmountEntity(exactCurrencyAmount),
                        executionDate.format(DateTimeFormatter.ISO_DATE),
                        remittanceInformation.getValue(),
                        null);

        final DomesticPaymentResponse domesticPaymentResponse = new DomesticPaymentResponse();
        domesticPaymentResponse.setTransactionStatus("RCVD");

        when(apiClient.getAccountNumbers())
                .thenReturn(
                        Optional.of(
                                AccountNumbersUtil.getAccountNumbersResponse("90255481251", "")));

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
                .isEqualTo("SEK");
    }

    @Test
    public void testUnknownPaymentSchemeForPayment() {
        // given
        Creditor creditor =
                new Creditor(AccountIdentifier.create(AccountIdentifierType.BBAN, "900-8004"));
        Debtor debtor =
                new Debtor(AccountIdentifier.create(AccountIdentifierType.SE, "90255481251"));
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
                                AccountNumbersUtil.getAccountNumbersResponse("90255481251", "")));

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
                        Collections.emptyList(),
                        Collections.emptyList());

        prepareSigning(url, id, scaStatus);

        // when
        PaymentMultiStepResponse response = paymentExecutor.sign(paymentRequest);

        // then
        Assertions.assertThat(response.getStep())
                .isEqualTo(AuthenticationStepConstants.STEP_FINALIZE);

        Assertions.assertThat(response.getPayment().getStatus()).isEqualTo(PaymentStatus.SIGNED);

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
                        Collections.emptyList(),
                        Collections.emptyList());

        prepareSigning(url, id, scaStatus);

        // when
        PaymentMultiStepResponse response = paymentExecutor.sign(paymentRequest);

        // then
        Assertions.assertThat(response.getStep())
                .isEqualTo(AuthenticationStepConstants.STEP_FINALIZE);

        Assertions.assertThat(response.getPayment().getStatus()).isEqualTo(PaymentStatus.SIGNED);
    }

    private void prepareSigning(URL url, String id, String scaStatus) {

        String authUrl = "authUrl";
        String basketId = "basketId";
        String authId = "authId";
        String state = "state";

        Payment finalPayment =
                new Payment.Builder()
                        .withUniqueId(id)
                        .withType(PaymentType.DOMESTIC)
                        .withStatus(PaymentStatus.SIGNED)
                        .build();

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
                mock(GetDomesticPaymentResponse.class);
        when(getDomesticPaymentResponse.toTinkPayment(eq(id)))
                .thenReturn(new PaymentResponse(finalPayment, new Storage()));
        when(apiClient.getDomesticPayment(eq(id))).thenReturn(getDomesticPaymentResponse);
    }
}
