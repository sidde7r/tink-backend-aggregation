package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreateRecurringPaymentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities.AmountEntity;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@RunWith(JUnitParamsRunner.class)
public class IngPaymentMapperTest {

    private IngPaymentMapper paymentMapper;

    @Before
    public void setup() {
        paymentMapper = new IngPaymentMapper(new BasePaymentMapper());
    }

    @SneakyThrows
    @Test
    @Parameters
    public void shouldMapTinkPaymentRequestToCreatePaymentRequest(
            PaymentScheme paymentScheme, String localInstrumentCode, LocalDate executionDate) {
        // given
        String debtorIban = randomIban(CountryCode.FR);
        String creditorIban = randomIban(CountryCode.FR);

        Payment payment =
                new Payment.Builder()
                        .withDebtor(new Debtor(new IbanIdentifier(debtorIban)))
                        .withCreditor(
                                new Creditor(new IbanIdentifier(creditorIban), "Payment Creditor"))
                        .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                        .withCurrency("EUR")
                        .withRemittanceInformation(
                                unstructuredRemittanceInformation("ReferenceToCreditor"))
                        .withPaymentScheme(paymentScheme)
                        .withExecutionDate(LocalDate.of(2019, 4, 5))
                        .build();

        // when
        IngCreatePaymentRequest result = paymentMapper.toIngCreatePaymentRequest(payment);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(
                        IngCreatePaymentRequest.builder()
                                .debtorAccount(new AccountEntity(debtorIban))
                                .creditorAccount(new AccountEntity(creditorIban))
                                .instructedAmount(new AmountEntity("1.0", "EUR"))
                                .creditorName("Payment Creditor")
                                .remittanceInformationUnstructured("ReferenceToCreditor")
                                .requestedExecutionDate(executionDate)
                                .chargeBearer(IngBaseConstants.PaymentRequest.SLEV)
                                .serviceLevelCode(IngBaseConstants.PaymentRequest.SEPA)
                                .localInstrumentCode(localInstrumentCode)
                                .build());
    }

    @SuppressWarnings("unused")
    private static Object[] parametersForShouldMapTinkPaymentRequestToCreatePaymentRequest() {
        return new Object[][] {
            {PaymentScheme.SEPA_CREDIT_TRANSFER, null, LocalDate.of(2019, 4, 5)},
            {PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER, "INST", null},
        };
    }

    @SneakyThrows
    @Test
    @Parameters
    public void shouldAddExecutionDateTime(
            LocalDate paymentExecutionDate, LocalDate expectedExecutionDate) {
        // given
        String debtorIban = randomIban(CountryCode.DE);
        String creditorIban = randomIban(CountryCode.DE);

        Payment payment =
                new Payment.Builder()
                        .withDebtor(new Debtor(new IbanIdentifier(debtorIban)))
                        .withCreditor(
                                new Creditor(
                                        new IbanIdentifier(creditorIban), "Payment Creditor 1234"))
                        .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(122.1))
                        .withRemittanceInformation(
                                unstructuredRemittanceInformation("SOME_REMITTANCE_VALUE123"))
                        .withExecutionDate(paymentExecutionDate)
                        .build();

        // when
        IngCreatePaymentRequest result = paymentMapper.toIngCreatePaymentRequest(payment);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(
                        IngCreatePaymentRequest.builder()
                                .debtorAccount(new AccountEntity(debtorIban))
                                .creditorAccount(new AccountEntity(creditorIban))
                                .instructedAmount(new AmountEntity("122.1", "EUR"))
                                .creditorName("Payment Creditor 1234")
                                .remittanceInformationUnstructured("SOME_REMITTANCE_VALUE123")
                                .chargeBearer(IngBaseConstants.PaymentRequest.SLEV)
                                .serviceLevelCode(IngBaseConstants.PaymentRequest.SEPA)
                                //
                                .requestedExecutionDate(expectedExecutionDate)
                                .build());
    }

    @SuppressWarnings("unused")
    private static Object[] parametersForShouldAddExecutionDateTime() {
        return new Object[] {
            new Object[] {null, null},
            new Object[] {LocalDate.of(2019, 4, 5), LocalDate.of(2019, 4, 5)},
            new Object[] {LocalDate.of(2020, 5, 30), LocalDate.of(2020, 5, 30)}
        };
    }

    @SneakyThrows
    @Test
    public void shouldMapTinkPaymentRequestToCreateRecurringPaymentRequest() {
        // given
        String debtorIban = randomIban(CountryCode.DE);
        String creditorIban = randomIban(CountryCode.DE);

        Payment payment =
                new Payment.Builder()
                        .withDebtor(new Debtor(new IbanIdentifier(debtorIban)))
                        .withCreditor(
                                new Creditor(
                                        new IbanIdentifier(creditorIban), "Payment Creditor 1234"))
                        .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(122.1))
                        .withRemittanceInformation(
                                unstructuredRemittanceInformation("SOME_REMITTANCE_VALUE123"))
                        .withPaymentServiceType(PaymentServiceType.PERIODIC)
                        .withStartDate(LocalDate.of(2001, 11, 12))
                        .withEndDate(LocalDate.of(2002, 12, 11))
                        .withFrequency(Frequency.DAILY)
                        .build();

        // when
        IngCreateRecurringPaymentRequest result =
                paymentMapper.toIngCreateRecurringPaymentRequest(payment);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(
                        IngCreateRecurringPaymentRequest.builder()
                                .debtorAccount(new AccountEntity(debtorIban))
                                .creditorAccount(new AccountEntity(creditorIban))
                                .instructedAmount(new AmountEntity("122.1", "EUR"))
                                .creditorName("Payment Creditor 1234")
                                .remittanceInformationUnstructured("SOME_REMITTANCE_VALUE123")
                                .chargeBearer(IngBaseConstants.PaymentRequest.SLEV)
                                .serviceLevelCode(IngBaseConstants.PaymentRequest.SEPA)
                                //
                                .startDate(LocalDate.of(2001, 11, 12))
                                .endDate(LocalDate.of(2002, 12, 11))
                                .frequency("DAIL")
                                .build());
    }

    @SneakyThrows
    @Test
    @Parameters
    public void shouldCorrectlyMapTinkFrequencyValues(Frequency frequency, String apiFrequency) {
        // given
        Payment payment =
                new Payment.Builder()
                        .withDebtor(new Debtor(new IbanIdentifier(randomIban(CountryCode.IT))))
                        .withCreditor(new Creditor(new IbanIdentifier(randomIban(CountryCode.IT))))
                        .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(122.1))
                        .withRemittanceInformation(
                                unstructuredRemittanceInformation("SOME_REMITTANCE_VALUE123"))
                        .withPaymentServiceType(PaymentServiceType.PERIODIC)
                        .withStartDate(LocalDate.of(2001, 11, 12))
                        .withEndDate(LocalDate.of(2002, 12, 11))
                        //
                        .withFrequency(frequency)
                        .build();

        // when
        IngCreateRecurringPaymentRequest result =
                paymentMapper.toIngCreateRecurringPaymentRequest(payment);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFrequency()).isEqualTo(apiFrequency);
    }

    @SuppressWarnings("unused")
    private static Object[] parametersForShouldCorrectlyMapTinkFrequencyValues() {
        Map<Frequency, String> testParamsMap =
                ImmutableMap.<Frequency, String>builder()
                        .put(Frequency.DAILY, "DAIL")
                        .put(Frequency.WEEKLY, "WEEK")
                        .put(Frequency.EVERY_TWO_WEEKS, "TOWK")
                        .put(Frequency.MONTHLY, "MNTH")
                        .put(Frequency.EVERY_TWO_MONTHS, "TOMN")
                        .put(Frequency.QUARTERLY, "QUTR")
                        .put(Frequency.SEMI_ANNUAL, "SEMI")
                        .put(Frequency.ANNUAL, "YEAR")
                        .build();
        // sanity check
        assertThat(testParamsMap.keySet()).containsExactlyInAnyOrder(Frequency.values());

        return testParamsMap.entrySet().stream()
                .map(entry -> new Object[] {entry.getKey(), entry.getValue()})
                .toArray();
    }

    @SneakyThrows
    @Test
    public void shouldIgnoreFuturePaymentPropertiesInRecurringPayment() {
        // given
        Payment payment =
                new Payment.Builder()
                        .withDebtor(new Debtor(new IbanIdentifier(randomIban(CountryCode.IT))))
                        .withCreditor(new Creditor(new IbanIdentifier(randomIban(CountryCode.IT))))
                        .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(122.1))
                        .withRemittanceInformation(
                                unstructuredRemittanceInformation("SOME_REMITTANCE_VALUE123"))
                        .withPaymentServiceType(PaymentServiceType.PERIODIC)
                        .withStartDate(LocalDate.of(2001, 11, 12))
                        .withEndDate(LocalDate.of(2002, 12, 11))
                        .withFrequency(Frequency.DAILY)
                        //
                        .withExecutionDate(LocalDate.of(2020, 1, 1))
                        .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                        .build();

        // when
        IngCreateRecurringPaymentRequest result =
                paymentMapper.toIngCreateRecurringPaymentRequest(payment);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRequestedExecutionDate()).isNull();
        assertThat(result.getLocalInstrumentCode()).isNull();
    }

    @SneakyThrows
    @Test
    public void shouldIgnoreCurrentlyUnsupportedDayOfExecutionFieldInRecurringPayment() {
        // given
        Payment payment =
                new Payment.Builder()
                        .withDebtor(new Debtor(new IbanIdentifier(randomIban(CountryCode.IT))))
                        .withCreditor(new Creditor(new IbanIdentifier(randomIban(CountryCode.IT))))
                        .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(122.1))
                        .withRemittanceInformation(
                                unstructuredRemittanceInformation("SOME_REMITTANCE_VALUE123"))
                        .withPaymentServiceType(PaymentServiceType.PERIODIC)
                        .withStartDate(LocalDate.of(2001, 11, 12))
                        .withEndDate(LocalDate.of(2002, 12, 11))
                        .withFrequency(Frequency.DAILY)
                        //
                        .withDayOfMonth(1)
                        .withDayOfWeek(DayOfWeek.FRIDAY)
                        .build();

        // when
        IngCreateRecurringPaymentRequest result =
                paymentMapper.toIngCreateRecurringPaymentRequest(payment);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDayOfExecution()).isNull();
    }

    private static String randomIban(CountryCode countryCode) {
        Iban iban = new Iban.Builder().countryCode(countryCode).buildRandom();
        return iban.toString();
    }

    private static RemittanceInformation unstructuredRemittanceInformation(String value) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue(value);

        return remittanceInformation;
    }

    @Test
    @Parameters
    public void shouldReturnCorrectPaymentStatus(
            String transactionStatus, PaymentStatus expectedPaymentStatus) {
        // when
        PaymentStatus paymentStatus = paymentMapper.getPaymentStatus(transactionStatus);

        // then
        assertThat(paymentStatus).isEqualTo(expectedPaymentStatus);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldReturnCorrectPaymentStatus() {
        Map<String, PaymentStatus> statusMap =
                ImmutableMap.<String, PaymentStatus>builder()
                        .put("RCVD", PaymentStatus.USER_APPROVAL_FAILED)
                        .put("ACTC", PaymentStatus.PENDING)
                        .put("PDNG", PaymentStatus.PENDING)
                        .put("ACCP", PaymentStatus.SIGNED)
                        .put("ACWC", PaymentStatus.SIGNED)
                        .put("ACSP", PaymentStatus.SIGNED)
                        .put("PART", PaymentStatus.PENDING)
                        .put("PATC", PaymentStatus.PENDING)
                        .put("ACSC", PaymentStatus.PAID)
                        .put("CANC", PaymentStatus.CANCELLED)
                        .put("RJCT", PaymentStatus.REJECTED)
                        .put("ACTV", PaymentStatus.SIGNED)
                        .put("EXPI", PaymentStatus.SIGNED)
                        // other
                        .put("ACFC", PaymentStatus.UNDEFINED)
                        .put("ACCC", PaymentStatus.UNDEFINED)
                        .put("ACWP", PaymentStatus.UNDEFINED)
                        .put("Unknown", PaymentStatus.UNDEFINED)
                        .build();

        List<Object[]> params = new ArrayList<>();

        for (Map.Entry<String, PaymentStatus> entry : statusMap.entrySet()) {
            String transactionStatus = entry.getKey();
            PaymentStatus paymentStatus = entry.getValue();

            params.add(new Object[] {transactionStatus.toLowerCase(), paymentStatus});
            params.add(new Object[] {transactionStatus.toUpperCase(), paymentStatus});
        }

        return params.toArray();
    }
}
