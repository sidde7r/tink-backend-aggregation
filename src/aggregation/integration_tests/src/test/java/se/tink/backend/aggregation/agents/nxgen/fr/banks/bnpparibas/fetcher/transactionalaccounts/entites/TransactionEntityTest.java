package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions.TransactionAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.DateUtils;

public class TransactionEntityTest {

    @Test
    public void ensureMerchantDescriptionsAreModified() {
        String descriptionString1 = "FACTURE CARTE DU 010101 SPOTIFY CARTE 1234XXXXXXXX";
        String descriptionString2 = "FACTURE CARTE DU 010101 LONG MERCHANT NAME WITH SPACES CARTE 12";
        String descriptionString3 = "FACTURE CARTE DU 010101 RESTAURANT PARIS CARTE 1234X";

        Transaction t1 = createTransactionEntityWithDescription(descriptionString1);
        Transaction t2 = createTransactionEntityWithDescription(descriptionString2);
        Transaction t3 = createTransactionEntityWithDescription(descriptionString3);

        Assert.assertEquals("SPOTIFY", t1.getDescription());
        Assert.assertEquals("LONG MERCHANT NAME WITH SPACES", t2.getDescription());
        Assert.assertEquals("RESTAURANT PARIS", t3.getDescription());
    }

    @Test
    public void ensureTransferAndFeeDescriptionsAreNotModified() {
        String descriptionString1 = "VIR SEPA RECU /DE CAF DU NORD /MOTIF XPXREFERENCE 1234";
        String descriptionString2 = "COMMISSIONS - FRAIS VIREMENT EMIS";
        String descriptionString3 = "RETRAIT DAB 01/01/01 12345 1234567 BANK NAME";
        String descriptionString4 = "VIREMENT SEPA EMIS /MOTIF /FULL NAME /REFDO /REFNAME";

        Transaction t1 = createTransactionEntityWithDescription(descriptionString1);
        Transaction t2 = createTransactionEntityWithDescription(descriptionString2);
        Transaction t3 = createTransactionEntityWithDescription(descriptionString3);
        Transaction t4 = createTransactionEntityWithDescription(descriptionString4);

        Assert.assertEquals(descriptionString1, t1.getDescription());
        Assert.assertEquals(descriptionString2, t2.getDescription());
        Assert.assertEquals(descriptionString3, t3.getDescription());
        Assert.assertEquals(descriptionString4, t4.getDescription());
    }

    private Transaction createTransactionEntityWithDescription(String description) {
        TransactionAmountEntity amountEntity = new TransactionAmountEntity();
        amountEntity.setAmount(15d);
        amountEntity.setCurrency("EUR");

        TransactionEntity te = new TransactionEntity();
        te.setTransactionLabel(description);
        te.setTransactionDate(DateUtils.parseDate("01-01-2018"));
        te.setAmount(amountEntity);

        return te.toTinkTransaction();
    }
}
