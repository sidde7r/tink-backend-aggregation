package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import java.math.BigDecimal;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.rpc.FetchLoanDetailsResponse;
import se.tink.backend.aggregation.constants.MarketCode;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.util.LoanInterpreter;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.user.rpc.User;

public class NordeaLoanFetcherTest {

    private static final String MISSING_FOLLOWING_PAYMENT_RESPONSE =
            "{\"loan_id\": \"123456\", "
                    + "    \"loan_formatted_id\": \"123456\", "
                    + "    \"product_code\": \"SE9711\", "
                    + "    \"currency\": \"SEK\", "
                    + "    \"group\": \"mortgage\", "
                    + "    \"repayment_status\": \"in_progress\", "
                    + "    \"first_draw_down_date\": \"2019-08-23\", "
                    + "    \"interest\": { "
                    + "        \"rate\": 1.44, "
                    + "        \"reference_rate_type\": \"other\", "
                    + "        \"period_start_date\": \"2019-08-15\", "
                    + "        \"base_rate\": 2.09 "
                    + "    }, "
                    + "    \"amount\": { "
                    + "        \"granted\": 1352000, "
                    + "        \"paid\": 0, "
                    + "        \"balance\": 1352000 "
                    + "    }, "
                    + "    \"repayment_schedule\": { "
                    + "        \"instalment_free_months\": [], "
                    + "        \"period_between_instalments\": 1, "
                    + "        \"initial_payment_date\": \"2019-08-23\", "
                    + "        \"final_payment_date\": \"2069-07-27\", "
                    + "        \"loan_account_number\": \"123456\", "
                    + "        \"debit_account_number\": \"123456\" "
                    + "    }, "
                    + "    \"owners\": [ "
                    + "        { "
                    + "            \"name\": \"Owner 1\" "
                    + "        }, "
                    + "        { "
                    + "            \"name\": \"Owner 2\" "
                    + "        } "
                    + "    ] "
                    + "}";

    private static final String LOAN_WITH_SUBLOAN_RESPONSE =
            "{\n"
                    + "    \"amount\": {\n"
                    + "        \"balance\": 1583282.0,\n"
                    + "        \"granted\": 1678750.0,\n"
                    + "        \"paid\": 95468.0\n"
                    + "    },\n"
                    + "    \"currency\": \"SEK\",\n"
                    + "    \"first_draw_down_date\": \"2014-10-24\",\n"
                    + "    \"following_payment\": {\n"
                    + "        \"date\": \"2019-09-27\",\n"
                    + "        \"fees\": 320.0,\n"
                    + "        \"instalment\": 1646.0,\n"
                    + "        \"interest\": 2003.0,\n"
                    + "        \"total\": 3969.0\n"
                    + "    },\n"
                    + "    \"group\": \"mortgage\",\n"
                    + "    \"interest\": {},\n"
                    + "    \"loan_formatted_id\": \"1234 56 78901\",\n"
                    + "    \"loan_id\": \"12345678901\",\n"
                    + "    \"owners\": [\n"
                    + "        {\n"
                    + "            \"name\": \"NAME SURNAME\"\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"product_code\": \"SE9731\",\n"
                    + "    \"repayment_schedule\": {\n"
                    + "        \"debit_account_number\": \"881101-2171\",\n"
                    + "        \"initial_payment_date\": \"2014-10-24\",\n"
                    + "        \"instalment_free_months\": [],\n"
                    + "        \"loan_account_number\": \"3998 28 09989\",\n"
                    + "        \"period_between_instalments\": 1\n"
                    + "    },\n"
                    + "    \"repayment_status\": \"in_progress\",\n"
                    + "    \"sub_agreements\": [\n"
                    + "        {\n"
                    + "            \"amount\": {\n"
                    + "                \"balance\": 1481250.0,\n"
                    + "                \"granted\": 1481250.0,\n"
                    + "                \"paid\": 0.0\n"
                    + "            },\n"
                    + "            \"interest\": {\n"
                    + "                \"base_rate\": 2.09,\n"
                    + "                \"discounted_rate_end_date\": \"2024-08-27\",\n"
                    + "                \"period_start_date\": \"2019-08-28\",\n"
                    + "                \"rate\": 1.47,\n"
                    + "                \"reference_rate_type\": \"other\"\n"
                    + "            }\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"amount\": {\n"
                    + "                \"balance\": 102032.0,\n"
                    + "                \"granted\": 197500.0,\n"
                    + "                \"paid\": 95468.0\n"
                    + "            },\n"
                    + "            \"interest\": {\n"
                    + "                \"base_rate\": 2.09,\n"
                    + "                \"discounted_rate_end_date\": \"2024-08-27\",\n"
                    + "                \"period_start_date\": \"2019-08-28\",\n"
                    + "                \"rate\": 1.47,\n"
                    + "                \"reference_rate_type\": \"other\"\n"
                    + "            }\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    private static final String LOAN_RESPONSE =
            "{\n"
                    + "  \"loan_id\": \"39961997727\",\n"
                    + "  \"loan_formatted_id\": \"3996 19 97727\",\n"
                    + "  \"product_code\": \"SE9711\",\n"
                    + "  \"currency\": \"SEK\",\n"
                    + "  \"group\": \"mortgage\",\n"
                    + "  \"repayment_status\": \"in_progress\",\n"
                    + "  \"first_draw_down_date\": \"2011-01-21\",\n"
                    + "  \"interest\": {\n"
                    + "    \"rate\": 0.500,\n"
                    + "    \"reference_rate_type\": \"other\",\n"
                    + "    \"period_start_date\": \"2015-03-27\"\n"
                    + "  },\n"
                    + "  \"amount\": {\n"
                    + "    \"granted\": 1625000.00,\n"
                    + "    \"paid\": 279027.00,\n"
                    + "    \"balance\": 1345973.00\n"
                    + "  },\n"
                    + "  \"following_payment\": {\n"
                    + "    \"instalment\": 2709.00,\n"
                    + "    \"interest\": 572.00,\n"
                    + "    \"fees\": 0,\n"
                    + "    \"total\": 3281.00,\n"
                    + "    \"date\": \"2019-09-27\"\n"
                    + "  },\n"
                    + "  \"repayment_schedule\": {\n"
                    + "    \"instalment_free_months\": [],\n"
                    + "    \"period_between_instalments\": 1,\n"
                    + "    \"initial_payment_date\": \"2011-01-21\",\n"
                    + "    \"loan_account_number\": \"3996 19 97727\",\n"
                    + "    \"debit_account_number\": \"3480 34 18101\"\n"
                    + "  },\n"
                    + "  \"owners\": [{\n"
                    + "    \"name\": \"NAME MIDDLENAME SURNAME\"\n"
                    + "  }, {\n"
                    + "    \"name\": \"NAME1 SURNAME1\"\n"
                    + "  }]\n"
                    + "}";

    @Test
    public void testLoanParsingWithSubLoans() {
        LoanAccount loanAccount =
                SerializationUtils.deserializeFromString(
                                LOAN_WITH_SUBLOAN_RESPONSE, FetchLoanDetailsResponse.class)
                        .toTinkLoanAccount();
        Assert.assertEquals(new Double(1.47 / 100), loanAccount.getInterestRate());
        Assert.assertEquals(
                ExactCurrencyAmount.of(new BigDecimal(1583282.0).negate(), "SEK"),
                loanAccount.getExactBalance());
        Assert.assertEquals(
                ExactCurrencyAmount.of(new BigDecimal(95468.0), "SEK"),
                loanAccount.getDetails().getExactAmortized());
        Assert.assertEquals(
                new Amount("SEK", 1678750.0).negate(),
                loanAccount.getDetails().getInitialBalance());
        Assert.assertEquals(new HolderName("NAME SURNAME"), loanAccount.getHolderName());
        Assert.assertTrue(loanAccount.isUniqueIdentifierEqual("************8901"));
    }

    @Test
    public void testLoanParsing() {
        LoanAccount loanAccount =
                SerializationUtils.deserializeFromString(
                                LOAN_RESPONSE, FetchLoanDetailsResponse.class)
                        .toTinkLoanAccount();
        Assert.assertEquals(new Double(0.500 / 100), loanAccount.getInterestRate());
        Assert.assertEquals(
                ExactCurrencyAmount.of(new BigDecimal(1345973.00).negate(), "SEK"),
                loanAccount.getExactBalance());
        Assert.assertEquals(
                ExactCurrencyAmount.of(new BigDecimal(279027.00), "SEK"),
                loanAccount.getDetails().getExactAmortized());
        Assert.assertEquals(
                new Amount("SEK", 1625000.00).negate(),
                loanAccount.getDetails().getInitialBalance());
        Assert.assertEquals(new HolderName("NAME MIDDLENAME SURNAME"), loanAccount.getHolderName());
        Assert.assertTrue(loanAccount.isUniqueIdentifierEqual("************7727"));
    }

    @Test
    // Should not throw NPE
    public void mapWithMissingFollowingPaymentTest() {
        LoanAccount loanAccount =
                SerializationUtils.deserializeFromString(
                                MISSING_FOLLOWING_PAYMENT_RESPONSE, FetchLoanDetailsResponse.class)
                        .toTinkLoanAccount();
        User user = mock(User.class);
        when(user.getFlags()).thenReturn(Lists.newArrayList());
        loanAccount.toSystemAccount(user);
        loanAccount
                .getDetails()
                .toSystemLoan(loanAccount, LoanInterpreter.getInstance(MarketCode.SE));
    }
}
