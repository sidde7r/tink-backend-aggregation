package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.engagements;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.utils.IkanoParser;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.date.DateUtils;

public class TransactionEntityTest {
    private static final String AMOUNT = "3217,2";
    private static final String ORIGINAL_AMOUNT = "3217,2";
    private static final String DATE = "2016-03-11";
    private static final String PENDING_TRANSACTION_NAME = "Skyddat belopp";

    @Test
    public void toTinkTransactionsTest() throws ParseException {
        TransactionEntity transactionEntity =
                createTransaction(PENDING_TRANSACTION_NAME, "Mastercard");
        Transaction transaction = transactionEntity.toTinkTransaction();

        assertThat(transaction.getAmount()).isEqualTo(-IkanoParser.stringToDouble(AMOUNT));
        assertThat(transaction.getOriginalAmount())
                .isEqualTo(-IkanoParser.stringToDouble(ORIGINAL_AMOUNT));
        assertThat(transaction.getDate())
                .isEqualTo(DateUtils.flattenTime(IkanoParser.stringToDate(DATE)));
        assertThat(transaction.getDescription()).isEqualTo(PENDING_TRANSACTION_NAME);
        Assert.assertTrue(transaction.isPending());
    }

    @Test
    public void transactionIsPending_When_TransactionName_Equals_SkyddatBelopp()
            throws ParseException {
        String transactionName = "skYddaT beLopp";
        String companyName = "Mastercard";

        TransactionEntity transactionEntity = createTransaction(transactionName, companyName);
        Transaction transaction = transactionEntity.toTinkTransaction();

        Assert.assertTrue(transaction.isPending());
        Assertions.assertThat(transaction.getDescription()).isEqualTo(transactionName);
    }

    @Test
    public void descriptionIsEqualTo_TransactionName_When_CompanyName_Is_Null()
            throws ParseException {
        String transactionName = "Inbetalning bankgiro";

        TransactionEntity transactionEntity = createTransaction(transactionName, null);
        Transaction transaction = transactionEntity.toTinkTransaction();

        Assertions.assertThat(transaction.getDescription()).isEqualTo(transactionName);
        Assert.assertFalse(transaction.isPending());
    }

    @Test
    public void descriptionIsEqualTo_CompanyName_When_NotPending_And_CompanyNameNotNull()
            throws ParseException {
        String transactionName = "Köp";
        String companyName = "Ikea";

        TransactionEntity transactionEntity = createTransaction(transactionName, companyName);
        Transaction transaction = transactionEntity.toTinkTransaction();

        Assertions.assertThat(transaction.getDescription()).isEqualTo(companyName);
        Assert.assertFalse(transaction.isPending());
    }

    private TransactionEntity createTransaction(String transactionName, String companyName)
            throws ParseException {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAmount(AMOUNT);
        transaction.setDate(DATE);
        transaction.setOriginalAmount(ORIGINAL_AMOUNT);
        transaction.setTransactionName(transactionName);
        transaction.setCompanyName(companyName);

        return transaction;
    }
}
