package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;
import lombok.SneakyThrows;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.entities.PaymentsLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.entities.SimpleAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class IngPaymentMapperTest {

    private static final String AUTHORIZATION_URL = "http://something.com/redirect?id=123";

    private IngPaymentMapper paymentMapper;

    @Before
    public void setup() {
        paymentMapper = new IngPaymentMapper();
    }

    @Test
    public void shouldMapTinkPaymentRequestToCreatePaymentRequest() {
        // given
        Iban sourceIban = new Iban.Builder().countryCode(CountryCode.FR).buildRandom();
        Iban destIban = new Iban.Builder().countryCode(CountryCode.FR).buildRandom();
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("ReferenceToCreditor");
        PaymentRequest paymentRequest =
                new PaymentRequest(
                        new Payment.Builder()
                                .withCreditor(
                                        new Creditor(new IbanIdentifier(sourceIban.toString())))
                                .withDebtor(new Debtor(new IbanIdentifier(destIban.toString())))
                                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                                .withCurrency("EUR")
                                .withRemittanceInformation(remittanceInformation)
                                .withUniqueId(UUID.randomUUID().toString())
                                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                                .withExecutionDate(LocalDate.of(2019, 4, 5))
                                .build());

        SimpleAccountEntity creditor =
                new SimpleAccountEntity(
                        paymentRequest.getPayment().getCreditor().getAccountNumber(),
                        paymentRequest.getPayment().getCurrency());
        SimpleAccountEntity debtor =
                new SimpleAccountEntity(
                        paymentRequest.getPayment().getDebtor().getAccountNumber(),
                        paymentRequest.getPayment().getCurrency());

        // when
        CreatePaymentRequest result = paymentMapper.toIngPaymentRequest(paymentRequest);

        // then
        assertThat(result.getCreditorAgent()).isEqualTo("INGBFRPP");
        assertThat(result.getCreditorName()).isEqualTo("Payment Creditor");
        assertThat(result.getCreditorAccount()).isEqualTo(creditor);
        assertThat(result.getDebtorAccount()).isEqualTo(debtor);
        assertThat(result.getInstructedAmount().getAmount()).isEqualTo("1.0");
        assertThat(result.getInstructedAmount().getCurrency()).isEqualTo("EUR");
        assertThat(result.getRemittanceInformationUnstructured()).isEqualTo("ReferenceToCreditor");
        assertThat(result.getChargeBearer()).isEqualTo("SLEV");
        assertThat(result.getLocalInstrumentCode()).isNull();
        assertThat(result.getServiceLevelCode()).isEqualTo("SEPA");
        assertThat(result.getRequestedExecutionDate()).isEqualTo("2019-04-05");
    }

    @SneakyThrows
    @Test
    public void shouldMapCreatePaymentResponseToTinkPaymentResponse() {
        // given
        IbanIdentifier cred = new IbanIdentifier("FR383390733324Z58PF2RSRNB11");
        IbanIdentifier deb = new IbanIdentifier("FR385390733324Z58PF2RSRNB11");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("ReferenceToCreditor");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        PaymentRequest paymentRequest =
                new PaymentRequest(
                        new Payment.Builder()
                                .withCreditor(new Creditor(cred))
                                .withDebtor(new Debtor(deb))
                                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                                .withCurrency("EUR")
                                .withRemittanceInformation(remittanceInformation)
                                .withUniqueId(UUID.randomUUID().toString())
                                .build());

        String paymentId = "paymentId";
        CreatePaymentResponse createPaymentResponse =
                new CreatePaymentResponse(paymentId, new PaymentsLinksEntity(AUTHORIZATION_URL));

        // when
        PaymentResponse paymentResponse =
                paymentMapper.toTinkPaymentResponse(paymentRequest, createPaymentResponse);

        // then
        assertThat(paymentResponse.getPayment().getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(paymentResponse.getPayment().getUniqueId()).isEqualTo(paymentId);
        assertThat(paymentResponse.getPayment().getType()).isEqualTo(PaymentType.SEPA);
        assertThat(paymentResponse.getPayment().getCurrency()).isEqualTo("EUR");
        assertThat(paymentResponse.getPayment().getExactCurrencyAmount().getDoubleValue())
                .isEqualTo(1.0);
        assertThat(paymentResponse.getPayment().getCreditor().getAccountNumber())
                .isEqualTo("FR383390733324Z58PF2RSRNB11");
        assertThat(
                        paymentResponse
                                .getPayment()
                                .getCreditor()
                                .getAccountIdentifier()
                                .getIdentifier())
                .isEqualTo("FR383390733324Z58PF2RSRNB11");
        assertThat(paymentResponse.getPayment().getCreditor().getAccountIdentifier().getType())
                .isEqualTo(AccountIdentifierType.IBAN);
        assertThat(paymentResponse.getPayment().getDebtor()).isNull();
    }
}
