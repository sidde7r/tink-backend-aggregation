package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class LoanDetailsResponseTest {

    private static final String DUMMY_DETAILS =
            "{\n"
                    + "    \"mortgageLoanDetails\": [\n"
                    + "        {\n"
                    + "            \"detailsInGroup\": [\n"
                    + "                {\n"
                    + "                    \"detailName\": \"sample name\",\n"
                    + "                    \"detailValue\": \"sample value\"\n"
                    + "                }"
                    + "            ]\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    @Test
    @Parameters(method = "mortgageParams")
    public void getType(final String inputData, final Type expectedType) {
        // given
        LoanDetailsResponse loanDetailsResponse =
                SerializationUtils.deserializeFromString(inputData, LoanDetailsResponse.class);

        // when
        LoanDetails.Type result = loanDetailsResponse.getType();

        // then
        assertThat(result).isEqualTo(expectedType);
    }

    private static final String LOAN_OF_MORTGAGE_TYPE_1 =
            "{\n"
                    + "    \"mortgageLoanDetails\": [\n"
                    + "        {\n"
                    + "            \"detailsInGroup\": [\n"
                    + "                {\n"
                    + "                    \"detailName\": \"Låntype\",\n"
                    + "                    \"detailValue\": \"PrioritetsLån\"\n"
                    + "                }"
                    + "            ]\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    private static final String LOAN_OF_MORTGAGE_TYPE_2 =
            "{\n"
                    + "    \"mortgageLoanDetails\": [\n"
                    + "        {\n"
                    + "            \"detailsInGroup\": [\n"
                    + "                {\n"
                    + "                    \"detailName\": \"Låntype\",\n"
                    + "                    \"detailValue\": \"EjendomsLån\"\n"
                    + "                }"
                    + "            ]\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    private static final String LOAN_OF_MORTGAGE_TYPE_3 =
            "{\n"
                    + "    \"mortgageLoanDetails\": [\n"
                    + "        {\n"
                    + "            \"detailsInGroup\": [\n"
                    + "                {\n"
                    + "                    \"detailName\": \"Låntype\",\n"
                    + "                    \"detailValue\": \"BoligLån\"\n"
                    + "                }"
                    + "            ]\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    private static final String LOAN_OF_NON_MORTGAGE_TYPE =
            "{\n"
                    + "    \"mortgageLoanDetails\": [\n"
                    + "        {\n"
                    + "            \"detailsInGroup\": [\n"
                    + "                {\n"
                    + "                    \"detailName\": \"Låntype\",\n"
                    + "                    \"detailValue\": \"non mortgage type\"\n"
                    + "                }"
                    + "            ]\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    private Object[] mortgageParams() {
        return new Object[] {
            new Object[] {LOAN_OF_MORTGAGE_TYPE_1, Type.MORTGAGE},
            new Object[] {LOAN_OF_MORTGAGE_TYPE_2, Type.MORTGAGE},
            new Object[] {LOAN_OF_MORTGAGE_TYPE_3, Type.MORTGAGE},
            new Object[] {LOAN_OF_NON_MORTGAGE_TYPE, Type.OTHER},
            new Object[] {DUMMY_DETAILS, Type.OTHER} // without details about loan
        };
    }

    @Test
    @Parameters(method = "initialBalanceParams")
    public void getInitialBalance(
            final String inputData, final ExactCurrencyAmount expectedOutput) {
        // given
        LoanDetailsResponse loanDetailsResponse =
                SerializationUtils.deserializeFromString(inputData, LoanDetailsResponse.class);

        // when
        ExactCurrencyAmount result = loanDetailsResponse.getInitialBalance();

        // then
        assertThat(result).isEqualTo(expectedOutput);
    }

    private static final String BALANCE_HOVEDSTOL =
            "{\n"
                    + "    \"mortgageLoanDetails\": [\n"
                    + "        {\n"
                    + "            \"detailsInGroup\": [\n"
                    + "                {\n"
                    + "                    \"detailName\": \"Hovedstol\",\n"
                    + "                    \"detailValue\": \"491.000,00 DKK\"\n"
                    + "                }"
                    + "            ]\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    private static final String BALANCE_PRINCIPAL =
            "{\n"
                    + "    \"mortgageLoanDetails\": [\n"
                    + "        {\n"
                    + "            \"detailsInGroup\": [\n"
                    + "                {\n"
                    + "                    \"detailName\": \"Principal\",\n"
                    + "                    \"detailValue\": \"2.503.000,00 DKK\"\n"
                    + "                }"
                    + "            ]\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    private Object[] initialBalanceParams() {
        return new Object[] {
            new Object[] {BALANCE_HOVEDSTOL, ExactCurrencyAmount.inDKK(491000.00)},
            new Object[] {BALANCE_PRINCIPAL, ExactCurrencyAmount.inDKK(2503000.00)},
            new Object[] {DUMMY_DETAILS, null} // without details about balance
        };
    }

    @Test
    @Parameters(method = "numOfMonthsBoundParams")
    public void getNumOfMonthsBound(final String inputData, final Integer expectedOutput) {
        // given
        LoanDetailsResponse loanDetailsResponse =
                SerializationUtils.deserializeFromString(inputData, LoanDetailsResponse.class);

        // when
        Integer result = loanDetailsResponse.getNumOfMonthsBound();

        // then
        assertThat(result).isEqualTo(expectedOutput);
    }

    private static final String NUM_OF_MONTHS_RESTLOBETID =
            "{\n"
                    + "    \"mortgageLoanDetails\": [\n"
                    + "        {\n"
                    + "            \"detailsInGroup\": [\n"
                    + "                {\n"
                    + "                    \"detailName\": \"Restløbetid\",\n"
                    + "                    \"detailValue\": \"26 år, 8 måneder\"\n"
                    + "                }"
                    + "            ]\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    private static final String NUM_OF_MONTHS_MATURITY =
            "{\n"
                    + "    \"mortgageLoanDetails\": [\n"
                    + "        {\n"
                    + "            \"detailsInGroup\": [\n"
                    + "                {\n"
                    + "                    \"detailName\": \"Maturity\",\n"
                    + "                    \"detailValue\": \"26 years, 9 months\"\n"
                    + "                }"
                    + "            ]\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    private Object[] numOfMonthsBoundParams() {
        return new Object[] {
            new Object[] {NUM_OF_MONTHS_RESTLOBETID, 320},
            new Object[] {NUM_OF_MONTHS_MATURITY, 321},
            new Object[] {DUMMY_DETAILS, null} // without details about balance
        };
    }

    @Test
    @Parameters(method = "interestRateParams")
    public void getInterestRate(final String inputData, final Double expectedOutput) {
        // given
        LoanDetailsResponse loanDetailsResponse =
                SerializationUtils.deserializeFromString(inputData, LoanDetailsResponse.class);

        // when
        Double result = loanDetailsResponse.getInterestRate();

        // then
        assertThat(result).isEqualTo(expectedOutput);
    }

    private static final String INTEREST_RATE_ENGLISH =
            "{\n"
                    + "    \"mortgageLoanDetails\": [\n"
                    + "        {\n"
                    + "            \"detailsInGroup\": [\n"
                    + "                {\n"
                    + "                    \"detailName\": \"Interest rate\",\n"
                    + "                    \"detailValue\": \"1,830 %\"\n"
                    + "                }"
                    + "            ]\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    private static final String INTEREST_RATE_RENTESATS =
            "{\n"
                    + "    \"mortgageLoanDetails\": [\n"
                    + "        {\n"
                    + "            \"detailsInGroup\": [\n"
                    + "                {\n"
                    + "                    \"detailName\": \"Rentesats\",\n"
                    + "                    \"detailValue\": \"-3,250 %\"\n"
                    + "                }"
                    + "            ]\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    private Object[] interestRateParams() {
        return new Object[] {
            new Object[] {INTEREST_RATE_ENGLISH, 0.0183},
            new Object[] {INTEREST_RATE_RENTESATS, -0.0325},
            new Object[] {DUMMY_DETAILS, null} // without details about balance
        };
    }
}
