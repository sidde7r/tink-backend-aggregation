package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.util.LoanInterpreter;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
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

    private static final String LOAN_WITH_SUBLOAN_RESPONSE_MISSING_BASE_RATE =
            "{\n"
                    + "  \"loan_id\": \"11111111111\",\n"
                    + "  \"loan_formatted_id\": \"1111 11 11111\",\n"
                    + "  \"product_code\": \"SE9731\",\n"
                    + "  \"currency\": \"SEK\",\n"
                    + "  \"group\": \"mortgage\",\n"
                    + "  \"repayment_status\": \"in_progress\",\n"
                    + "  \"first_draw_down_date\": \"2012-12-13\",\n"
                    + "  \"interest\": {},\n"
                    + "  \"amount\": {\n"
                    + "    \"granted\": 144816.00,\n"
                    + "    \"paid\": 65205.00,\n"
                    + "    \"balance\": 79611.00\n"
                    + "  },\n"
                    + "  \"following_payment\": {\n"
                    + "    \"instalment\": 805.00,\n"
                    + "    \"interest\": 137.00,\n"
                    + "    \"fees\": 0,\n"
                    + "    \"total\": 942.00,\n"
                    + "    \"date\": \"2019-10-27\"\n"
                    + "  },\n"
                    + "  \"latest_payment\": {\n"
                    + "    \"total\": 948.00,\n"
                    + "    \"date\": \"2019-09-27\"\n"
                    + "  },\n"
                    + "  \"repayment_schedule\": {\n"
                    + "    \"instalment_free_months\": [],\n"
                    + "    \"period_between_instalments\": 1,\n"
                    + "    \"initial_payment_date\": \"2012-12-13\",\n"
                    + "    \"loan_account_number\": \"3997 23 31881\",\n"
                    + "    \"debit_account_number\": \"810918-0235\"\n"
                    + "  },\n"
                    + "  \"owners\": [{\n"
                    + "    \"name\": \"NAME SURNAME\"\n"
                    + "  }],\n"
                    + "  \"sub_agreements\": [{\n"
                    + "    \"amount\": {\n"
                    + "      \"granted\": 1.00,\n"
                    + "      \"paid\": 0.00,\n"
                    + "      \"balance\": 1.00\n"
                    + "    },\n"
                    + "    \"interest\": {\n"
                    + "      \"rate\": 1.740,\n"
                    + "      \"reference_rate_type\": \"other\",\n"
                    + "      \"period_start_date\": \"2012-12-11\",\n"
                    + "      \"base_rate\": 2.090\n"
                    + "    }\n"
                    + "  }, {\n"
                    + "    \"amount\": {\n"
                    + "      \"granted\": 144815.00,\n"
                    + "      \"paid\": 65205.00,\n"
                    + "      \"balance\": 79610.00\n"
                    + "    },\n"
                    + "    \"interest\": {\n"
                    + "      \"rate\": 2.090,\n"
                    + "      \"reference_rate_type\": \"other\",\n"
                    + "      \"period_start_date\": \"2012-12-11\"\n"
                    + "    }\n"
                    + "  }]\n"
                    + "}";

    private static final String LOAN_RESPONSE_NO_BASE_RATES_IN_SUBLOAN =
            "{\n"
                    + "  \"loan_id\": \"22222222222\",\n"
                    + "  \"loan_formatted_id\": \"2222 22 22222\",\n"
                    + "  \"product_code\": \"SE9731\",\n"
                    + "  \"currency\": \"SEK\",\n"
                    + "  \"group\": \"mortgage\",\n"
                    + "  \"repayment_status\": \"in_progress\",\n"
                    + "  \"first_draw_down_date\": \"2010-04-21\",\n"
                    + "  \"interest\": {},\n"
                    + "  \"amount\": {\n"
                    + "    \"granted\": 425000.00,\n"
                    + "    \"paid\": 112652.00,\n"
                    + "    \"balance\": 312348.00\n"
                    + "  },\n"
                    + "  \"following_payment\": {\n"
                    + "    \"instalment\": 1158.00,\n"
                    + "    \"interest\": 554.00,\n"
                    + "    \"fees\": 0,\n"
                    + "    \"total\": 1712.00,\n"
                    + "    \"date\": \"2019-11-27\"\n"
                    + "  },\n"
                    + "  \"repayment_schedule\": {\n"
                    + "    \"instalment_free_months\": [],\n"
                    + "    \"period_between_instalments\": 1,\n"
                    + "    \"initial_payment_date\": \"2010-04-21\",\n"
                    + "    \"loan_account_number\": \"3996 10 09320\",\n"
                    + "    \"debit_account_number\": \"3025 22 47876\",\n"
                    + "    \"repayment_day_of_month\": 27\n"
                    + "  },\n"
                    + "  \"owners\": [{\n"
                    + "    \"name\": \"NAME SURNAME\"\n"
                    + "  }, {\n"
                    + "    \"name\": \"NAME2 SURNAME2\"\n"
                    + "  }],\n"
                    + "  \"sub_agreements\": [{\n"
                    + "    \"amount\": {\n"
                    + "      \"granted\": 321691.00,\n"
                    + "      \"paid\": 14526.00,\n"
                    + "      \"balance\": 307165.00\n"
                    + "    },\n"
                    + "    \"interest\": {\n"
                    + "      \"rate\": 2.090,\n"
                    + "      \"reference_rate_type\": \"other\",\n"
                    + "      \"period_start_date\": \"2010-04-20\"\n"
                    + "    }\n"
                    + "  }, {\n"
                    + "    \"amount\": {\n"
                    + "      \"granted\": 103309.00,\n"
                    + "      \"paid\": 98126.00,\n"
                    + "      \"balance\": 5183.00\n"
                    + "    },\n"
                    + "    \"interest\": {\n"
                    + "      \"rate\": 2.090,\n"
                    + "      \"reference_rate_type\": \"other\",\n"
                    + "      \"period_start_date\": \"2010-04-20\"\n"
                    + "    }\n"
                    + "  }]\n"
                    + "}";

    private static final String LOAN_RESPONSE_RATES_MISMATCH =
            "{\n"
                    + "  \"loan_id\": \"39962739632\",\n"
                    + "  \"loan_formatted_id\": \"3996 27 39632\",\n"
                    + "  \"product_code\": \"SE9731\",\n"
                    + "  \"currency\": \"SEK\",\n"
                    + "  \"group\": \"mortgage\",\n"
                    + "  \"repayment_status\": \"in_progress\",\n"
                    + "  \"first_draw_down_date\": \"2011-08-01\",\n"
                    + "  \"interest-\": {},\n"
                    + "  \"amount\": {\n"
                    + "    \"granted\": 488500.00,\n"
                    + "    \"paid\": 74478.00,\n"
                    + "    \"balance\": 414022.00\n"
                    + "  },\n"
                    + "  \"following_payment\": {\n"
                    + "    \"instalment\": 951.00,\n"
                    + "    \"interest\": 621.00,\n"
                    + "    \"fees\": 0,\n"
                    + "    \"total\": 1572.00,\n"
                    + "    \"date\": \"2019-11-27\"\n"
                    + "  },\n"
                    + "  \"repayment_schedule\": {\n"
                    + "    \"instalment_free_months\": [],\n"
                    + "    \"period_between_instalments\": 1,\n"
                    + "    \"initial_payment_date\": \"2011-08-01\",\n"
                    + "    \"loan_account_number\": \"3996 27 39632\",\n"
                    + "    \"debit_account_number\": \"1403 36 35300\",\n"
                    + "    \"repayment_day_of_month\": 27\n"
                    + "  },\n"
                    + "  \"owners\": [{\n"
                    + "    \"name\": \"NAME SURNAME\"\n"
                    + "  }, {\n"
                    + "    \"name\": \"NAME2 SURNAME2\"\n"
                    + "  }],\n"
                    + "  \"sub_agreements\": [{\n"
                    + "    \"amount\": {\n"
                    + "      \"granted\": 373500.00,\n"
                    + "      \"paid\": 11856.00,\n"
                    + "      \"balance\": 361644.00\n"
                    + "    },\n"
                    + "    \"interest\": {\n"
                    + "      \"rate\": 1.640,\n"
                    + "      \"reference_rate_type\": \"other\",\n"
                    + "      \"period_start_date\": \"2011-08-01\",\n"
                    + "      \"base_rate\": 2.090\n"
                    + "    }\n"
                    + "  }, {\n"
                    + "    \"amount\": {\n"
                    + "      \"granted\": 115000.00,\n"
                    + "      \"paid\": 62622.00,\n"
                    + "      \"balance\": 52378.00\n"
                    + "    },\n"
                    + "    \"interest\": {\n"
                    + "      \"rate\": 2.640,\n"
                    + "      \"reference_rate_type\": \"other\",\n"
                    + "      \"period_start_date\": \"2011-08-01\",\n"
                    + "      \"base_rate\": 3.090\n"
                    + "    }\n"
                    + "  }]\n"
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
        Optional<LoanAccount> loanAccountOptional =
                SerializationUtils.deserializeFromString(
                                LOAN_WITH_SUBLOAN_RESPONSE, LoanDetailsResponse.class)
                        .toTinkLoanAccount();
        Assert.assertTrue(loanAccountOptional.isPresent());
        LoanAccount loanAccount = loanAccountOptional.get();
        Assert.assertEquals(new Double(1.47 / 100), loanAccount.getInterestRate());
        Assert.assertEquals(
                ExactCurrencyAmount.of(BigDecimal.valueOf(1583282.0).negate(), "SEK"),
                loanAccount.getExactBalance());
        Assert.assertEquals(
                ExactCurrencyAmount.of(BigDecimal.valueOf(95468.0), "SEK"),
                loanAccount.getDetails().getExactAmortized());
        Assert.assertEquals(
                ExactCurrencyAmount.inSEK(1678750.0).negate(),
                loanAccount.getDetails().getInitialBalance());
        Assert.assertEquals(new HolderName("NAME SURNAME"), loanAccount.getHolderName());
        Assert.assertTrue(loanAccount.isUniqueIdentifierEqual("************8901"));
        loanAccountOptional =
                SerializationUtils.deserializeFromString(
                                LOAN_WITH_SUBLOAN_RESPONSE_MISSING_BASE_RATE,
                                LoanDetailsResponse.class)
                        .toTinkLoanAccount();
        Assert.assertTrue(loanAccountOptional.isPresent());
        loanAccount = loanAccountOptional.get();
        Assert.assertEquals(new Double(1.740 / 100), loanAccount.getInterestRate());

        Assert.assertEquals(
                ExactCurrencyAmount.of(BigDecimal.valueOf(79611.0).negate(), "SEK"),
                loanAccount.getExactBalance());
        Assert.assertEquals(
                ExactCurrencyAmount.of(BigDecimal.valueOf(65205.0), "SEK"),
                loanAccount.getDetails().getExactAmortized());
        Assert.assertEquals(
                ExactCurrencyAmount.inSEK(144816.0).negate(),
                loanAccount.getDetails().getInitialBalance());
        Assert.assertEquals(new HolderName("NAME SURNAME"), loanAccount.getHolderName());
        Assert.assertTrue(loanAccount.isUniqueIdentifierEqual("************1111"));

        loanAccountOptional =
                SerializationUtils.deserializeFromString(
                                LOAN_RESPONSE_NO_BASE_RATES_IN_SUBLOAN, LoanDetailsResponse.class)
                        .toTinkLoanAccount();
        Assert.assertTrue(loanAccountOptional.isPresent());
        loanAccount = loanAccountOptional.get();
        Assert.assertEquals(new Double(2.090 / 100), loanAccount.getInterestRate());
        Assert.assertEquals(
                ExactCurrencyAmount.of(BigDecimal.valueOf(312348.0).negate(), "SEK"),
                loanAccount.getExactBalance());
        Assert.assertEquals(
                ExactCurrencyAmount.of(BigDecimal.valueOf(112652.0), "SEK"),
                loanAccount.getDetails().getExactAmortized());
        Assert.assertEquals(
                ExactCurrencyAmount.inSEK(425000.0).negate(),
                loanAccount.getDetails().getInitialBalance());
        Assert.assertEquals(new HolderName("NAME SURNAME"), loanAccount.getHolderName());
        Assert.assertTrue(loanAccount.isUniqueIdentifierEqual("************2222"));

        loanAccountOptional =
                SerializationUtils.deserializeFromString(
                                LOAN_RESPONSE_RATES_MISMATCH, LoanDetailsResponse.class)
                        .toTinkLoanAccount();
        Assert.assertFalse(loanAccountOptional.isPresent());
    }

    @Test
    public void testLoanParsing() {
        Optional<LoanAccount> loanAccountOptional =
                SerializationUtils.deserializeFromString(LOAN_RESPONSE, LoanDetailsResponse.class)
                        .toTinkLoanAccount();
        Assert.assertTrue(loanAccountOptional.isPresent());
        LoanAccount loanAccount = loanAccountOptional.get();
        Assert.assertEquals(new Double(0.500 / 100), loanAccount.getInterestRate());
        Assert.assertEquals(
                ExactCurrencyAmount.of(BigDecimal.valueOf(1345973.00).negate(), "SEK"),
                loanAccount.getExactBalance());
        Assert.assertEquals(
                ExactCurrencyAmount.of(BigDecimal.valueOf(279027.00), "SEK"),
                loanAccount.getDetails().getExactAmortized());
        Assert.assertEquals(
                ExactCurrencyAmount.inSEK(1625000.00).negate(),
                loanAccount.getDetails().getInitialBalance());
        Assert.assertEquals(new HolderName("NAME MIDDLENAME SURNAME"), loanAccount.getHolderName());
        Assert.assertTrue(loanAccount.isUniqueIdentifierEqual("************7727"));
    }

    @Test
    // Should not throw NPE
    public void mapWithMissingFollowingPaymentTest() {
        Optional<LoanAccount> loanAccountOptional =
                SerializationUtils.deserializeFromString(
                                MISSING_FOLLOWING_PAYMENT_RESPONSE, LoanDetailsResponse.class)
                        .toTinkLoanAccount();

        Assert.assertTrue(loanAccountOptional.isPresent());
        LoanAccount loanAccount = loanAccountOptional.get();
        User user = mock(User.class);
        Provider provider = mock(Provider.class);
        when(user.getFlags()).thenReturn(Lists.newArrayList());
        loanAccount.toSystemAccount(user, provider);
        loanAccount
                .getDetails()
                .toSystemLoan(loanAccount, LoanInterpreter.getInstance(MarketCode.SE));
    }
}
