package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

/**
 * Parsing of one PaymentEntity in the response list is different from parsing multiple since Nordea
 * sends back an array of entities if multiple in JSON format, if only one they send back a JSON
 * object instead of an array on the key.
 *
 * <p>Since we use these entities (at the time of writing this) to check that we don't sign other
 * than our own transfers in the Nordea outbox we want to make sure we parse all info needed.
 */
public class UnconfirmedPaymentListResponseTest {
    @Test
    public void convertsSinglePayment() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        UnconfirmedPaymentListResponse unconfirmedPaymentListResponse =
                objectMapper.readValue(
                        RESPONSE_SINGLE_PAYMENT, UnconfirmedPaymentListResponse.class);

        assertThat(unconfirmedPaymentListResponse).isNotNull();

        PaymentsOut paymentsOut = unconfirmedPaymentListResponse.getGetUnconfirmedPaymentsOut();
        assertThat(paymentsOut).isNotNull();

        List<PaymentEntity> payments = paymentsOut.getPayments();
        assertThat(payments).hasSize(1);

        // Make sure we parse all stuff that's essential for mapping our request/response payment to
        // a PaymentEntity
        assertThat(Double.parseDouble(payments.get(0).getAmount())).isEqualTo(10.0);
        assertThat(payments.get(0).getBeneficiaryName()).isEqualTo("Some-Account Name");
        assertThat(payments.get(0).getCurrency()).isEqualTo("SEK");
        assertThat(payments.get(0).getFromAccountId()).isEqualTo("NDEASESSXXX-SE1-SEK-1212121212");
        assertThat(payments.get(0).getPaymentDate()).isEqualTo("2016-03-10T12:00:00.163+01:00");
        assertThat(payments.get(0).getPaymentSubType()).isEqualTo("ThirdParty");
        assertThat(payments.get(0).getToAccountId()).isEqualTo("1200112233");
    }

    @Test
    public void convertsMultiplePayments() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        UnconfirmedPaymentListResponse unconfirmedPaymentListResponse =
                objectMapper.readValue(
                        RESPONSE_MULTIPLE_PAYMENTS, UnconfirmedPaymentListResponse.class);

        assertThat(unconfirmedPaymentListResponse).isNotNull();

        PaymentsOut paymentsOut = unconfirmedPaymentListResponse.getGetUnconfirmedPaymentsOut();
        assertThat(paymentsOut).isNotNull();

        List<PaymentEntity> payments = paymentsOut.getPayments();
        assertThat(payments).hasSize(2);

        // Make sure we parse all stuff that's essential for mapping our request/response payment to
        // a PaymentEntity
        assertThat(Double.parseDouble(payments.get(0).getAmount())).isEqualTo(10.0);
        assertThat(payments.get(0).getBeneficiaryName()).isEqualTo("Some-Account Name");
        assertThat(payments.get(0).getCurrency()).isEqualTo("SEK");
        assertThat(payments.get(0).getFromAccountId()).isEqualTo("NDEASESSXXX-SE1-SEK-1212121212");
        assertThat(payments.get(0).getPaymentDate()).isEqualTo("2016-03-10T12:00:00.163+01:00");
        assertThat(payments.get(0).getPaymentSubType()).isEqualTo("ThirdParty");
        assertThat(payments.get(0).getToAccountId()).isEqualTo("1200112233");

        // Don't need to check every value, just know that we are parsing stuff since it's same
        // entity type as first
        assertThat(Double.parseDouble(payments.get(1).getAmount())).isEqualTo(2.0);
        assertThat(payments.get(1).getToAccountId()).isEqualTo("1200332211");
    }

    // Notice "payment" is a JSON Object {…}
    private static final String RESPONSE_SINGLE_PAYMENT =
            "{\n"
                    + "\t\"getUnconfirmedPaymentsOut\": {\n"
                    + "\t\t\"payment\": {\n"
                    + "\t\t\t\"paymentId\": {\n"
                    + "\t\t\t\t\"$\": \"first-payment-id-1234\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"paymentType\": {\n"
                    + "\t\t\t\t\"$\": \"Domestic\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"paymentSubType\": {\n"
                    + "\t\t\t\t\"$\": \"ThirdParty\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"paymentSubTypeExtension\": {},\n"
                    + "\t\t\t\"beneficiaryNickName\": {},\n"
                    + "\t\t\t\"beneficiaryName\": {\n"
                    + "\t\t\t\t\"$\": \"Some-Account Name\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"fromAccountId\": {\n"
                    + "\t\t\t\t\"$\": \"NDEASESSXXX-SE1-SEK-1212121212\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"toAccountId\": {\n"
                    + "\t\t\t\t\"$\": \"1200112233\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"currency\": {\n"
                    + "\t\t\t\t\"$\": \"SEK\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"amount\": {\n"
                    + "\t\t\t\t\"$\": 10.00\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"dueDate\": {\n"
                    + "\t\t\t\t\"$\": \"2016-03-10T12:00:00.163+01:00\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"paymentDate\": {\n"
                    + "\t\t\t\t\"$\": \"2016-03-10T12:00:00.163+01:00\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"statusCode\": {\n"
                    + "\t\t\t\t\"$\": \"Unconfirmed\"\n"
                    + "\t\t\t}\n"
                    + "\t\t}\n"
                    + "\t}\n"
                    + "}";

    // Notice "payment" is a JSON array [{…}, {…}]
    private static final String RESPONSE_MULTIPLE_PAYMENTS =
            "{\n"
                    + "\t\"getUnconfirmedPaymentsOut\": {\n"
                    + "\t\t\"payment\": [{\n"
                    + "\t\t\t\"paymentId\": {\n"
                    + "\t\t\t\t\"$\": \"first-payment-id-1234\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"paymentType\": {\n"
                    + "\t\t\t\t\"$\": \"Domestic\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"paymentSubType\": {\n"
                    + "\t\t\t\t\"$\": \"ThirdParty\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"paymentSubTypeExtension\": {},\n"
                    + "\t\t\t\"beneficiaryNickName\": {},\n"
                    + "\t\t\t\"beneficiaryName\": {\n"
                    + "\t\t\t\t\"$\": \"Some-Account Name\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"fromAccountId\": {\n"
                    + "\t\t\t\t\"$\": \"NDEASESSXXX-SE1-SEK-1212121212\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"toAccountId\": {\n"
                    + "\t\t\t\t\"$\": \"1200112233\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"currency\": {\n"
                    + "\t\t\t\t\"$\": \"SEK\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"amount\": {\n"
                    + "\t\t\t\t\"$\": 10.00\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"dueDate\": {\n"
                    + "\t\t\t\t\"$\": \"2016-03-10T12:00:00.163+01:00\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"paymentDate\": {\n"
                    + "\t\t\t\t\"$\": \"2016-03-10T12:00:00.163+01:00\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"statusCode\": {\n"
                    + "\t\t\t\t\"$\": \"Unconfirmed\"\n"
                    + "\t\t\t}\n"
                    + "\t\t}, {\n"
                    + "\t\t\t\"paymentId\": {\n"
                    + "\t\t\t\t\"$\": \"second-payment-id-1234\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"paymentType\": {\n"
                    + "\t\t\t\t\"$\": \"Domestic\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"paymentSubType\": {\n"
                    + "\t\t\t\t\"$\": \"ThirdParty\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"paymentSubTypeExtension\": {},\n"
                    + "\t\t\t\"beneficiaryNickName\": {},\n"
                    + "\t\t\t\"beneficiaryName\": {\n"
                    + "\t\t\t\t\"$\": \"Calle P\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"fromAccountId\": {\n"
                    + "\t\t\t\t\"$\": \"NDEASESSXXX-SE1-SEK-1212121212\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"toAccountId\": {\n"
                    + "\t\t\t\t\"$\": \"1200332211\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"currency\": {\n"
                    + "\t\t\t\t\"$\": \"SEK\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"amount\": {\n"
                    + "\t\t\t\t\"$\": 2.00\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"dueDate\": {\n"
                    + "\t\t\t\t\"$\": \"2016-03-10T12:00:00.163+01:00\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"paymentDate\": {\n"
                    + "\t\t\t\t\"$\": \"2016-03-10T12:00:00.163+01:00\"\n"
                    + "\t\t\t},\n"
                    + "\t\t\t\"statusCode\": {\n"
                    + "\t\t\t\t\"$\": \"Unconfirmed\"\n"
                    + "\t\t\t}\n"
                    + "\t\t}]\n"
                    + "\t}\n"
                    + "}";
}
