package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.rpc.FetchLoanDetailsResponse;
import se.tink.backend.aggregation.constants.MarketCode;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.util.LoanInterpreter;
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
