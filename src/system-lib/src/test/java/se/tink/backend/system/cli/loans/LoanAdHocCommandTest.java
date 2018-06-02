package se.tink.backend.system.cli.loans;

import org.junit.Test;

import java.util.regex.Matcher;

import static org.junit.Assert.*;

public class LoanAdHocCommandTest {

    private static final String TEST1 = "{\"loanName\":\"Bolån Hypotek\",\"loanNumber\":\"9029.69\",\"originalDebt\":\"1 008 750,00\",\"currentDebt\":\"1 008 750,00\",\"currentInterestRate\":\"1,47 %\",\"rateBoundUntil\":\"2015-12-01\",\"rateBindingPeriodLength\":\"3 MÅNADER\",\"borrowers\":[{\"name\":\"William\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"0,00 %\"},{\"name\":\"Nina\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"0,00 %\"}],\"securities\":[{\"securityText\":\"TROLLHÄTTAN\",\"securityType\":\"Pantbrev/Inteckning\"}],\"fixedRate\":true}";
    private static final String TEST2 = "{\"loanName\":\"Bolån Hypotek\",\"loanNumber\":\"9029.69\",\"originalDebt\":\"1 008 750,00\",\"currentDebt\":\"1 008 750,00\",\"currentInterestRate\":\"1,47 %\",\"rateBoundUntil\":\"2015-12-01\",\"rateBindingPeriodLength\":\"60 MÅNADER\",\"borrowers\":[{\"name\":\"William\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"0,00 %\"},{\"name\":\"Nina\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"0,00 %\"}],\"securities\":[{\"securityText\":\"TROLLHÄTTAN\",\"securityType\":\"Pantbrev/Inteckning\"}],\"fixedRate\":true}";
    private static final String TEST3 = "{\"loanName\":\"Bolån Hypotek\",\"loanNumber\":\"9029.69\",\"originalDebt\":\"1 008 750,00\",\"currentDebt\":\"1 008 750,00\",\"currentInterestRate\":\"1,47 %\",\"rateBoundUntil\":\"2015-12-01\",\"rateBindingPeriodLength\":\"36 MÅNADER\",\"borrowers\":[{\"name\":\"William\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"0,00 %\"},{\"name\":\"Nina\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"0,00 %\"}],\"securities\":[{\"securityText\":\"TROLLHÄTTAN\",\"securityType\":\"Pantbrev/Inteckning\"}],\"fixedRate\":true}";
    private static final String TEST4 = "{\"loanName\":\"Bolån Hypotek\",\"loanNumber\":\"9029.69\",\"originalDebt\":\"1 008 750,00\",\"currentDebt\":\"1 008 750,00\",\"currentInterestRate\":\"1,47 %\",\"rateBoundUntil\":\"2015-12-01\",\"rateBindingPeriodLength\":\"12 MÅNADER\",\"borrowers\":[{\"name\":\"William\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"0,00 %\"},{\"name\":\"Nina\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"0,00 %\"}],\"securities\":[{\"securityText\":\"TROLLHÄTTAN\",\"securityType\":\"Pantbrev/Inteckning\"}],\"fixedRate\":true}";

    private static final String TEST_VARIABLE1 = "{\"loanName\":\"Privatlån\",\"loanNumber\":\"9023.41\",\"originalDebt\":\"254 000,00\",\"currentDebt\":\"211 660,00\",\"currentInterestRate\":\"5,35 %\",\"rateBoundUntil\":null,\"rateBindingPeriodLength\":null,\"borrowers\":[{\"name\":\"Jesper\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"50,00 %\"},{\"name\":\"Ida\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"50,00 %\"}],\"securities\":[],\"fixedRate\":false}";
    private static final String TEST_VARIABLE2 = "{\"loanName\":\"Privatlån\",\"loanNumber\":\"9023.64\",\"originalDebt\":\"224 250,00\",\"currentDebt\":\"214 905,00\",\"currentInterestRate\":\"5,70 %\",\"rateBoundUntil\":null,\"rateBindingPeriodLength\":null,\"borrowers\":[{\"name\":\"Christoffer\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"50,00 %\"},{\"name\":\"Malvina\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"50,00 %\"}],\"securities\":[],\"fixedRate\":false}";
    private static final String TEST_VARIABLE3 = "{\"loanName\":\"Privatlån\",\"loanNumber\":\"9023.40\",\"originalDebt\":\"172 500,00\",\"currentDebt\":\"146 616,00\",\"currentInterestRate\":\"6,18 %\",\"rateBoundUntil\":null,\"rateBindingPeriodLength\":null,\"borrowers\":[{\"name\":\"Anders\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"50,00 %\"},{\"name\":\"Sandra\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"50,00 %\"}],\"securities\":[],\"fixedRate\":false}";
    private static final String TEST_VARIABLE4 = "{\"loanName\":\"Privatlån\",\"loanNumber\":\"9023.64\",\"originalDebt\":\"45 000,00\",\"currentDebt\":\"39 640,00\",\"currentInterestRate\":\"5,90 %\",\"rateBoundUntil\":null,\"rateBindingPeriodLength\":null,\"borrowers\":[{\"name\":\"Mattias\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"50,00 %\"},{\"name\":\"Linda\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"50,00 %\"}],\"securities\":[],\"fixedRate\":false}";

    @Test
    public void testSwappingToCorrectNumMonthsBound() {
        Matcher m1 = LoanAdHocCommand.PATTERN_RATE_BINDING.matcher(TEST1);
        Matcher m2 = LoanAdHocCommand.PATTERN_RATE_BINDING.matcher(TEST2);
        Matcher m3 = LoanAdHocCommand.PATTERN_RATE_BINDING.matcher(TEST3);
        Matcher m4 = LoanAdHocCommand.PATTERN_RATE_BINDING.matcher(TEST4);

        m1.find();
        m2.find();
        m3.find();
        m4.find();

        assertEquals("3", m1.group(1));
        assertEquals("60", m2.group(1));
        assertEquals("36", m3.group(1));
        assertEquals("12", m4.group(1));
    }

    @Test
    public void testSettingVariableNumMonthsBound() {
        Matcher m1 = LoanAdHocCommand.PATTERN_VARIABLE_RATE.matcher(TEST_VARIABLE1);
        Matcher m2 = LoanAdHocCommand.PATTERN_VARIABLE_RATE.matcher(TEST_VARIABLE2);
        Matcher m3 = LoanAdHocCommand.PATTERN_VARIABLE_RATE.matcher(TEST_VARIABLE3);
        Matcher m4 = LoanAdHocCommand.PATTERN_VARIABLE_RATE.matcher(TEST_VARIABLE4);

        m1.find();
        m2.find();
        m3.find();
        m4.find();

        assertEquals("null", m1.group(1));
        assertEquals("null", m2.group(1));
        assertEquals("null", m3.group(1));
        assertEquals("null", m4.group(1));

        assertEquals("false", m1.group(2));
        assertEquals("false", m2.group(2));
        assertEquals("false", m3.group(2));
        assertEquals("false", m4.group(2));
    }
}