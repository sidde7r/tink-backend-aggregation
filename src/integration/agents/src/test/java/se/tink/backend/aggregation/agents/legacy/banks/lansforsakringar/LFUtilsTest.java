package se.tink.backend.aggregation.agents.banks.lansforsakringar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.PaymentEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.PaymentRequest;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.RecipientEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.UpcomingTransactionEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.i18n.Catalog;

public class LFUtilsTest {
    @Test
    public void testGetApiAdaptedToAccount_AccountThatIncludesClearing() {
        SwedishIdentifier danskeBankAccount = createIdentifier("12780236434");

        String apiAdaptedToAccount = LFUtils.getApiAdaptedToAccount(danskeBankAccount);

        assertThat(apiAdaptedToAccount).isEqualTo("12780236434");
    }

    @Test
    public void testGetApiAdaptedToAccount_AccountThatExcludesClearing() {
        SwedishIdentifier handelsbankenAccount = createIdentifier("6152135538858");

        String apiAdaptedToAccount = LFUtils.getApiAdaptedToAccount(handelsbankenAccount);

        assertThat(apiAdaptedToAccount).isEqualTo("135538858");
    }

    @Test
    public void testGetApiAdaptedToAccount_NordeaAccountThatIncludesClearing() {
        SwedishIdentifier nordeaRegularAccount = createIdentifier("16034332648");

        String apiAdaptedToAccount = LFUtils.getApiAdaptedToAccount(nordeaRegularAccount);

        assertThat(apiAdaptedToAccount).isEqualTo("16034332648");
    }

    @Test
    public void testGetApiAdaptedToAccount_NordeaAccountThatExcludesClearing() {
        SwedishIdentifier nordeaSSNAccount = createIdentifier("33008401141935");

        String apiAdaptedToAccount = LFUtils.getApiAdaptedToAccount(nordeaSSNAccount);

        assertThat(apiAdaptedToAccount).isEqualTo("8401141935");
    }

    private SwedishIdentifier createIdentifier(String clearingAndAccountNumber) {
        AccountIdentifier accountIdentifier =
                AccountIdentifier.create(AccountIdentifierType.SE, clearingAndAccountNumber);

        assertThat(accountIdentifier).isNotNull();

        return accountIdentifier.to(SwedishIdentifier.class);
    }

    @Test
    public void ensurePaymentRequest_withZeroAsPaymentDate_matchesPaymentEntityWithDifferentDate() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToBgPg("4177404-3");
        paymentRequest.setAmount(1030.0);
        paymentRequest.setReference("500000000005335");
        paymentRequest.setFromAccount("90224568878");
        paymentRequest.setPaymentDate(0L);

        PaymentEntity paymentEntity = new PaymentEntity();
        RecipientEntity recipient = new RecipientEntity();
        recipient.setGiroNumber("4177404-3");

        paymentEntity.setRecipient(recipient);
        paymentEntity.setAmount(1030.0);
        paymentEntity.setReference("500000000005335");
        paymentEntity.setFromAccount("90224568878");
        paymentEntity.setDate(1488150000000L);

        Assert.assertTrue(LFUtils.isSamePayment(paymentRequest, paymentEntity));
    }

    @Test
    public void
            ensurePaymentRequest_withSpecifiedPaymentDate_matchesPaymentEntity_withSamePaymentDate() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToBgPg("4177404-3");
        paymentRequest.setAmount(1030.0);
        paymentRequest.setReference("500000000005335");
        paymentRequest.setFromAccount("90224568878");
        paymentRequest.setPaymentDate(1488150000000L);

        PaymentEntity paymentEntity = new PaymentEntity();
        RecipientEntity recipient = new RecipientEntity();
        recipient.setGiroNumber("4177404-3");

        paymentEntity.setRecipient(recipient);
        paymentEntity.setAmount(1030.0);
        paymentEntity.setReference("500000000005335");
        paymentEntity.setFromAccount("90224568878");
        paymentEntity.setDate(1488150000000L);

        Assert.assertTrue(LFUtils.isSamePayment(paymentRequest, paymentEntity));
    }

    @Test
    public void
            ensurePaymentRequest_withSpecifiedPaymentDate_doesNotMatchPaymentEntity_withDifferentPaymentDate() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToBgPg("4177404-3");
        paymentRequest.setAmount(1030.0);
        paymentRequest.setReference("500000000005335");
        paymentRequest.setFromAccount("90224568878");
        paymentRequest.setPaymentDate(1488063600000L);

        PaymentEntity paymentEntity = new PaymentEntity();
        RecipientEntity recipient = new RecipientEntity();
        recipient.setGiroNumber("4177404-3");

        paymentEntity.setRecipient(recipient);
        paymentEntity.setAmount(1030.0);
        paymentEntity.setReference("500000000005335");
        paymentEntity.setFromAccount("90224568878");
        paymentEntity.setDate(1488150000000L);

        Assert.assertFalse(LFUtils.isSamePayment(paymentRequest, paymentEntity));
    }

    @Test
    public void ensurePaymentRequest_doesNotMatchPaymentEntity_withDifferentHash() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToBgPg("4177404-3");
        paymentRequest.setAmount(1030.0);
        paymentRequest.setReference("500000000005332");
        paymentRequest.setFromAccount("90224568878");
        paymentRequest.setPaymentDate(0L);

        PaymentEntity paymentEntity = new PaymentEntity();
        RecipientEntity recipient = new RecipientEntity();
        recipient.setGiroNumber("4177404-3");

        paymentEntity.setRecipient(recipient);
        paymentEntity.setAmount(1030.0);
        paymentEntity.setReference("500000000005335");
        paymentEntity.setFromAccount("90224568878");
        paymentEntity.setDate(1488150000000L);

        Assert.assertFalse(LFUtils.isSamePayment(paymentRequest, paymentEntity));
    }

    @Test
    public void
            ensurePaymentRequest_withZeroAsPaymentDate_matchesUpcomingTransactionEntityWithDifferentDate() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToBgPg("4177404-3");
        paymentRequest.setAmount(1030.0);
        paymentRequest.setReference("500000000005335");
        paymentRequest.setFromAccount("90224568878");
        paymentRequest.setPaymentDate(0L);

        UpcomingTransactionEntity transaction = new UpcomingTransactionEntity();
        RecipientEntity recipient = new RecipientEntity();
        recipient.setGiroNumber("4177404-3");
        recipient.setReference("500000000005335");

        transaction.setPaymentInfo(recipient);
        transaction.setAmount(1030.0);
        transaction.setDate(new Date(1488150000000L));

        Assert.assertTrue(LFUtils.isSamePayment(paymentRequest, transaction));
    }

    @Test
    public void
            ensurePaymentRequest_withSpecifiedPaymentDate_matchesUpcomingTransactionEntity_withSamePaymentDate() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToBgPg("4177404-3");
        paymentRequest.setAmount(1030.0);
        paymentRequest.setReference("500000000005335");
        paymentRequest.setFromAccount("90224568878");
        paymentRequest.setPaymentDate(1488150000000L);

        UpcomingTransactionEntity transaction = new UpcomingTransactionEntity();
        RecipientEntity recipient = new RecipientEntity();
        recipient.setGiroNumber("4177404-3");
        recipient.setReference("500000000005335");

        transaction.setPaymentInfo(recipient);
        transaction.setAmount(1030.0);
        transaction.setDate(new Date(1488150000000L));

        Assert.assertTrue(LFUtils.isSamePayment(paymentRequest, transaction));
    }

    @Test
    public void
            ensurePaymentRequest_withSpecifiedPaymentDate_doesNotMatchUpcomingTransactionEntity_withDifferentPaymentDate() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToBgPg("4177404-3");
        paymentRequest.setAmount(1030.0);
        paymentRequest.setReference("500000000005335");
        paymentRequest.setFromAccount("90224568878");
        paymentRequest.setPaymentDate(1488063600000L);

        UpcomingTransactionEntity transaction = new UpcomingTransactionEntity();
        RecipientEntity recipient = new RecipientEntity();
        recipient.setGiroNumber("4177404-3");
        recipient.setReference("500000000005335");

        transaction.setPaymentInfo(recipient);
        transaction.setAmount(1030.0);
        transaction.setDate(new Date(1488150000000L));

        Assert.assertFalse(LFUtils.isSamePayment(paymentRequest, transaction));
    }

    @Test
    public void ensurePaymentRequest_doesNotMatchUpcomingTransactionEntity_withDifferentHash() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToBgPg("4177404-3");
        paymentRequest.setAmount(1030.0);
        paymentRequest.setReference("500000000005332");
        paymentRequest.setFromAccount("90224568878");
        paymentRequest.setPaymentDate(0L);

        UpcomingTransactionEntity transaction = new UpcomingTransactionEntity();
        RecipientEntity recipient = new RecipientEntity();
        recipient.setGiroNumber("4177404-3");
        recipient.setReference("500000000005335");

        transaction.setPaymentInfo(recipient);
        transaction.setAmount(1030.0);
        transaction.setDate(new Date(1488150000000L));

        Assert.assertFalse(LFUtils.isSamePayment(paymentRequest, transaction));
    }

    @Test
    public void testCorrectTranslationDep() {
        assertEquals(
                "Mottagarkontot är inte giltigt",
                Catalog.getCatalog("sv_SE").getString("Invalid destination account"));
    }

    @Test
    public void ensureValidOCR_forValidValue() {
        Assert.assertTrue(LFUtils.isValidOCR("50000038393"));
    }

    @Test
    public void ensureInvalidOCR_forInvalidValue() {
        Assert.assertFalse(LFUtils.isValidOCR("12345"));
    }
}
