package se.tink.backend.common.bankfees;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.core.BankFeeType;

public class BankFeeRulesTest {

    @Test
    public void verifyValidBankFeeTransactions() {

        BankFeeRules matcher = BankFeeRules.getInstance();

        // Nordea
        Assert.assertTrue(matcher.matches("Avgift utl automat"));
        Assert.assertTrue(matcher.matches("Avgift utl automat 1345322"));
        Assert.assertTrue(matcher.matches("Avgift utl automat A23232222"));
        Assert.assertTrue(matcher.matches("Vardagspaketet Jan"));
        Assert.assertTrue(matcher.matches("Vardagspaketet Feb"));
        Assert.assertTrue(matcher.matches("Vardagspaketet Mar"));
        Assert.assertTrue(matcher.matches("Vardagspaketet Apr"));
        Assert.assertTrue(matcher.matches("Vardagspaketet Maj"));
        Assert.assertTrue(matcher.matches("Vardagspaketet Jun"));
        Assert.assertTrue(matcher.matches("Vardagspaketet Jul"));
        Assert.assertTrue(matcher.matches("Vardagspaketet Aug"));
        Assert.assertTrue(matcher.matches("Vardagspaketet Sep"));
        Assert.assertTrue(matcher.matches("Vardagspaketet Okt"));
        Assert.assertTrue(matcher.matches("Vardagspaketet Nov"));
        Assert.assertTrue(matcher.matches("Vardagspaketet Dec"));

        // Danske Bank
        Assert.assertTrue(matcher.matches("Bankavgift"));
        Assert.assertTrue(matcher.matches("Påminnelseavgift"));
        Assert.assertTrue(matcher.matches("Uppläggningsavgift"));

        // SEB
        Assert.assertTrue(matcher.matches("Bankavgift"));
        Assert.assertTrue(matcher.matches("Uttagsavgift"));
        Assert.assertTrue(matcher.matches("Kontantuttagsavgift"));
        Assert.assertTrue(matcher.matches("Enkla Vardag"));

        // Swedbank
        Assert.assertTrue(matcher.matches("Kontantuttagsavgift"));
        Assert.assertTrue(matcher.matches("Pris Internetbet"));
        Assert.assertTrue(matcher.matches("Pris Bankkort Ma"));
        Assert.assertTrue(matcher.matches("Pris Nyckelkund"));

        // SAS Eurobonus
        Assert.assertTrue(matcher.matches("Aviavgift"));
        Assert.assertTrue(matcher.matches("Årsavgift för Kontot"));
        Assert.assertTrue(matcher.matches("Årsavgift"));
        Assert.assertTrue(matcher.matches("Pris Nyckelkund"));

        // ICA
        Assert.assertTrue(matcher.matches("Avgift Bankkort"));

        // SHB
        Assert.assertTrue(matcher.matches("Kortavgift"));
        Assert.assertTrue(matcher.matches("Årsavgift"));
    }

    @Test
    public void verifyInvalidBankFeeTransactions() {

        BankFeeRules matcher = BankFeeRules.getInstance();

        Assert.assertFalse(matcher.matches("Överföring till kalle avgift"));
        Assert.assertFalse(matcher.matches("Årsavgift för Kalle Anka Tidning"));
        Assert.assertFalse(matcher.matches("Avgift Bankkort till Stefan"));
    }

    @Test
    public void verifyWithType(){
        BankFeeRules matcher = BankFeeRules.getInstance();

        BankFeeRules.MatchResult res1 = matcher.matchDetails("Avgift utl automat 12345");
        Assert.assertTrue(res1.matches);
        Assert.assertTrue(res1.type.equals(BankFeeType.CASH_WITHDRAWAL_FEE));

        BankFeeRules.MatchResult res2 = matcher.matchDetails("Vardagspaketet Jan");

        Assert.assertTrue(res2.matches);
        Assert.assertTrue(res2.type.equals(BankFeeType.CARD_FEE));

        BankFeeRules.MatchResult res3 = matcher.matchDetails("Aviavgift");

        Assert.assertTrue(res3.matches);
        Assert.assertTrue(res3.type.equals(BankFeeType.ADMINISTRATION_FEE));


    }
}
