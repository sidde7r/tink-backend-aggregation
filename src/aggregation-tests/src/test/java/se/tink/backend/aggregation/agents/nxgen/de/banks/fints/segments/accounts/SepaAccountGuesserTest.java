package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.accounts;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;

public class SepaAccountGuesserTest
{
    private SepaAccountGuesser sepaAccountGuesser;

    @Before
    public void setUp() {
        this.sepaAccountGuesser = new SepaAccountGuesser();
    }

    @Test
    public void shouldCorrectlyDetectKnownAccountTypes() {
        Assert.assertEquals(FinTsConstants.AccountType.SAVINGS_ACCOUNT_CURSOR, sepaAccountGuesser.guessSepaAccountType("Extra-Konto"));
        Assert.assertEquals(FinTsConstants.AccountType.SAVINGS_ACCOUNT_CURSOR, sepaAccountGuesser.guessSepaAccountType("Sparbrief"));
        Assert.assertEquals(FinTsConstants.AccountType.SAVINGS_ACCOUNT_CURSOR, sepaAccountGuesser.guessSepaAccountType("VL-Sparen"));

        Assert.assertEquals(FinTsConstants.AccountType.FUND_DEPOSIT_ACCOUNT_CURSOR, sepaAccountGuesser.guessSepaAccountType("Direkt-Depot"));
    }

    @Test
    public void shouldGuessAccountTypes() {
        Assert.assertEquals(FinTsConstants.AccountType.SAVINGS_ACCOUNT_CURSOR, sepaAccountGuesser.guessSepaAccountType("Spar Konto"));

        Assert.assertEquals(FinTsConstants.AccountType.FUND_DEPOSIT_ACCOUNT_CURSOR, sepaAccountGuesser.guessSepaAccountType("Depot Konto"));
    }

    @Test
    public void shouldDefaultToCheckingAccountWhenGuessing() {
        Assert.assertEquals(FinTsConstants.AccountType.CHECKING_ACCOUNT_CURSOR, sepaAccountGuesser.guessSepaAccountType("Girokonto"));
        Assert.assertEquals(FinTsConstants.AccountType.CHECKING_ACCOUNT_CURSOR, sepaAccountGuesser.guessSepaAccountType("Anything"));
        Assert.assertEquals(FinTsConstants.AccountType.CHECKING_ACCOUNT_CURSOR, sepaAccountGuesser.guessSepaAccountType(null));
    }

}
