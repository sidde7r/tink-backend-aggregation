package se.tink.backend.aggregation.agents.banks.crosskey.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.ParseException;
import java.util.Date;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import se.tink.libraries.social.security.time.SwedishTimeRule;
import se.tink.backend.system.rpc.Transaction;
import static org.assertj.core.api.Assertions.assertThat;

public class TransactionResponseTest {

    @Rule
    public SwedishTimeRule timeRule = new SwedishTimeRule();

    @Test
    public void toTinkTransaction_setsRequiredFields() throws Exception {
        TransactionResponse response = createTransactionResponse("20160309", 10.00, "A text code", "A receiver");

        Transaction transaction = response.toTinkTransaction();

        Date expectedDate = createDate(2016, 3, 9, 12);

        assertThat(transaction.getAmount()).isEqualTo(10.00);
        assertThat(transaction.getDate()).isEqualTo(expectedDate);
        assertThat(transaction.getDescription()).isNotEmpty(); // Tested in detail in other tests for description
    }

    @Test
    public void whenIncoming_ownNoteNull_tinkTransactionDescriptionIsTextCode() throws Exception {
        TransactionResponse response = createTransactionResponse("20160309", 10.00, "A text code", "A receiver");
        response.setIncoming(true);
        response.setOwnNote(null);

        Transaction transaction = response.toTinkTransaction();

        assertThat(transaction.getDescription()).isEqualTo("A text code");
    }

    @Test
    public void whenOutgoing_ownNoteNull_tinkTransactionDescriptionIsReceiverName() throws Exception {
        TransactionResponse response = createTransactionResponse("20160309", -10.00, "A text code", "A receiver");
        response.setIncoming(false);
        response.setOwnNote(null);

        Transaction transaction = response.toTinkTransaction();

        assertThat(transaction.getDescription()).isEqualTo("A receiver");
    }

    @Test
    public void whenIncoming_ownNoteIsSet_tinkTransactionDescriptionIsOwnNote() throws Exception {
        TransactionResponse response = createTransactionResponse("20160309", 10.00, "A text code", "A receiver");
        response.setIncoming(true);
        response.setOwnNote("A note");

        Transaction transaction = response.toTinkTransaction();

        assertThat(transaction.getDescription()).isEqualTo("A note");
    }

    @Test
    public void whenOutgoing_ownNoteIsSet_tinkTransactionDescriptionIsOwnNote() throws Exception {
        TransactionResponse response = createTransactionResponse("20160309", -10.00, "A text code", "A receiver");
        response.setIncoming(false);
        response.setOwnNote("A note");

        Transaction transaction = response.toTinkTransaction();

        assertThat(transaction.getDescription()).isEqualTo("A note");
    }

    @Test
    public void deserialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonModel = "{\n"
                + "      \"transactionId\": \"227357d275aab143bf113f2722f8654b1f0d0622:160413000000396:1:20160413\",\n"
                + "      \"dueDate\": \"20160413\",\n"
                + "      \"bookingDate\": \"20160413\",\n"
                + "      \"amount\": 10.00,\n"
                + "      \"currency\": \"SEK\",\n"
                + "      \"originalAmount\": 10.00,\n"
                + "      \"originalCurrency\": \"SEK\",\n"
                + "      \"accountNumber\": \"SE8653680239572\",\n"
                + "      \"recieverName\": \"Daniel SEB\",\n"
                + "      \"reference\": \"\",\n"
                + "      \"textCode\": \"VÄLGÖRENHET\",\n"
                + "      \"ownRegistered\": true,\n"
                + "      \"copyable\": true,\n"
                + "      \"sepaTransaction\": false,\n"
                + "      \"incoming\": true,\n"
                + "      \"periodicity\": \"ONE_TIME\",\n"
                + "      \"endDate\": \"20160413\",\n"
                + "      \"ownNote\": \"an own note\"\n"
                + "    }";

        TransactionResponse transactionResponse = mapper.readValue(jsonModel, TransactionResponse.class);

        assertThat(transactionResponse.getTransactionId()).isEqualTo("227357d275aab143bf113f2722f8654b1f0d0622:160413000000396:1:20160413");
        assertThat(transactionResponse.getDueDate()).isEqualTo(createDate(2016, 4, 13, 12));
        assertThat(transactionResponse.getAmount()).isEqualTo(10.00);
        assertThat(transactionResponse.getCurrency()).isEqualTo("SEK");
        assertThat(transactionResponse.getReceiverName()).isEqualTo("Daniel SEB");
        assertThat(transactionResponse.getTextCode()).isEqualTo("VÄLGÖRENHET");
        assertThat(transactionResponse.isIncoming()).isEqualTo(true);
        assertThat(transactionResponse.getOwnNote()).isEqualTo("an own note");
    }

    private TransactionResponse createTransactionResponse(String date, double amount, String textCode,
            String receiverName) throws ParseException {
        TransactionResponse transactionResponse = new TransactionResponse();

        transactionResponse.setAmount(amount);
        transactionResponse.setDueDate(date);
        transactionResponse.setReceiverName(receiverName);
        transactionResponse.setTextCode(textCode);

        return transactionResponse;
    }

    private static Date createDate(int year, int month, int day, int hour) {
        return new DateTime()
                    .withMillis(0)
                    .withYear(year)
                    .withMonthOfYear(month)
                    .withDayOfMonth(day)
                    .withHourOfDay(hour)
                    .toDate();
    }
}
