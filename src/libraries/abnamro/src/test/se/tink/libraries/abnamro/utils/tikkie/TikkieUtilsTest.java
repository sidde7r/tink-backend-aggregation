package se.tink.libraries.abnamro.utils.tikkie;

import org.junit.Test;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import static org.assertj.core.api.Assertions.assertThat;

public class TikkieUtilsTest {

    @Test
    public void shouldNotBeTikkieIfNoPayload() {

        Transaction transaction = new Transaction();

        assertThat(TikkieUtils.isTikkieTransaction(transaction)).isFalse();
    }

    @Test
    public void shouldNotBeTikkieIfWrongDescription() {

        Transaction transaction = new Transaction();
        transaction.setDescription("Foo");

        assertThat(TikkieUtils.isTikkieTransaction(transaction)).isFalse();
    }

    @Test
    public void shouldNotBeTikkieIfTikkieInsideDescription() {

        Transaction transaction = new Transaction();
        transaction.setDescription("Something and then Tikkie and other");

        assertThat(TikkieUtils.isTikkieTransaction(transaction)).isFalse();
    }

    @Test
    public void shouldBeTikkieIfMessageStartsWithTikkie() {

        Transaction transaction = new Transaction();
        transaction.setDescription("Tikkie");

        assertThat(TikkieUtils.isTikkieTransaction(transaction)).isTrue();

        transaction.setDescription("TIKKIE");

        assertThat(TikkieUtils.isTikkieTransaction(transaction)).isTrue();

        transaction.setDescription("ABN AMRO BANK INZ TIKKIE");

        assertThat(TikkieUtils.isTikkieTransaction(transaction)).isTrue();
    }

    @Test
    public void incomeMessageAndSenderShouldBeParsable() {

        Transaction income = new Transaction();
        income.setAmount(100.);
        income.setPayload(TransactionPayloadTypes.MESSAGE, "Tikkie ID 00000000 111, My Message, Frank Sinatra");

        TikkieDetails details = TikkieUtils.parseTransactionDetails(income);

        assertThat(details).isNotNull();
        assertThat(details.getName()).isEqualTo("Frank Sinatra");
        assertThat(details.getMessage()).isEqualTo("My Message");
    }

    /**
     * Don't know if we can have cases with empty message and/or receiver/sender so will not parse it. /ErikP
     */
    @Test
    public void incomeEmptyMessageShouldNotBeParsable() {

        Transaction income = new Transaction();
        income.setAmount(100.);
        income.setPayload(TransactionPayloadTypes.MESSAGE, "Tikkie ID 00000000 0697, , Donald Trump");

        TikkieDetails details = TikkieUtils.parseTransactionDetails(income);

        assertThat(details).isNull();
    }

    @Test
    public void expenseSenderShouldBeParsable() {
        TikkieDetails details = TikkieUtils.parseTransactionDetails(
                createExpenseTransaction("90234234 234234234 4234 Ralf NL75ABNA342324234"));

        assertThat(details).isNotNull();
        assertThat(details.getName()).isEqualTo("Ralf");
        assertThat(details.getMessage()).isNull();
    }

    @Test
    public void expenseSenderFrommBunqShouldBeParsable() {
        TikkieDetails details = TikkieUtils.parseTransactionDetails(
                createExpenseTransaction("000004215381 00300 0216032 Catch Up NL33BUNQ211 1111111"));

        assertThat(details).isNotNull();
        assertThat(details.getName()).isEqualTo("Catch Up");
        assertThat(details.getMessage()).isNull();
    }

    @Test
    public void expenseSenderWithSpaceShouldBeParsable() {
        TikkieDetails details = TikkieUtils.parseTransactionDetails(
                createExpenseTransaction("000000503822 00300 Erik Pettersson NL71A BNA1111111111"));

        assertThat(details).isNotNull();
        assertThat(details.getName()).isEqualTo("Erik Pettersson");
        assertThat(details.getMessage()).isNull();
    }

    private static Transaction createExpenseTransaction(String description) {
        Transaction expense = new Transaction();
        expense.setAmount(-100.);
        expense.setPayload(TransactionPayloadTypes.MESSAGE, description);

        return expense;
    }
}
