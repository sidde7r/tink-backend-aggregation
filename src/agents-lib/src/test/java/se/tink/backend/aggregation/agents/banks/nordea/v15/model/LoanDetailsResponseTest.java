package se.tink.backend.aggregation.agents.banks.nordea.v15.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.ParseException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.agents.models.Loan;

public class LoanDetailsResponseTest {
    private static final String ITEM = "{\"getLoanDetailsOut\":{\"loanData\":{\"localNumber\":{\"$\":\"1337\"},\"currency\":{\"$\":\"SEK\"},\"granted\":{\"$\":\"-30000.00\"},\"balance\":{\"$\":\"-18000.00\"},\"interestTermEnds\":{},\"interest\":{\"$\":\"4.55\"},\"paymentAccount\":{\"$\":\"1337\"},\"paymentFrequency\":{\"$\":\"PERIOD_01\"}},\"followingPayment\":{\"date\":{\"$\":\"2016-09-28T09:29:40.722+02:00\"},\"interest\":{\"$\":\"69.00\"},\"expenses\":{\"$\":\"0.00\"},\"total\":{\"$\":\"569.00\"},\"amortisation\":{\"$\":\"500.00\"}},\"latestPayment\":{\"date\":{\"$\":\"2016-08-28T09:29:40.722+02:00\"},\"interest\":{\"$\":\"71.00\"},\"expenses\":{\"$\":\"0.00\"},\"total\":{\"$\":\"571.00\"},\"amortisation\":{\"$\":\"500.00\"}}}}";
    private static final String ITEM_WITH_NULLS_AND_EMPTY = "{\"getLoanDetailsOut\":{\"loanData\":{\"localNumber\":{\"$\":null},\"currency\":{\"$\":\"SEK\"},\"granted\":{\"$\":null},\"balance\":{\"$\":null},\"interestTermEnds\":{},\"interest\":{\"$\":null},\"paymentAccount\":{\"$\":null},\"paymentFrequency\":{\"$\":\"PERIOD_01\"}},\"followingPayment\":{\"date\":{\"$\":\"2016-09-28T09:29:40.722+02:00\"},\"interest\":{\"$\":null},\"expenses\":{\"$\":\"0.00\"},\"total\":{\"$\":null},\"amortisation\":{\"$\":null}},\"latestPayment\":{\"date\":{\"$\":\"2016-08-28T09:29:40.722+02:00\"},\"interest\":{\"$\":\"71.00\"},\"expenses\":{\"$\":\"0.00\"},\"total\":{\"$\":\"571.00\"},\"amortisation\":{\"$\":\"500.00\"}}}}";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void deserializeTest() throws IOException, ParseException {
        LoanDetailsResponse loanDetailsResponse = MAPPER.readValue(ITEM, LoanDetailsResponse.class);

        Account account = new Account();
        Loan loan = loanDetailsResponse.toLoan(account, Loan.Type.MORTGAGE, ITEM);

        Assertions.assertThat(loan.getInitialBalance()).isEqualTo(-30000.0);
        Assertions.assertThat(loan.getBalance()).isEqualTo(-18000.0);
        Assertions.assertThat(loan.getSerializedLoanResponse()).isEqualTo(ITEM);
        Assertions.assertThat(loan.getMonthlyAmortization()).isEqualTo(500.0);
        Assertions.assertThat(loan.getAmortized()).isEqualTo(12000.0);
        Assertions.assertThat(loan.getInterest()).isEqualTo(0.0455);
        Assertions.assertThat(loan.getLoanNumber()).isEqualTo("1337");
        Assertions.assertThat(loan.getType()).isEqualTo(Loan.Type.MORTGAGE);
    }

    @Test
    public void testWithNulls() throws IOException, ParseException {
        LoanDetailsResponse loanDetailsResponse = MAPPER.readValue(ITEM, LoanDetailsResponse.class);

        Account account = new Account();
        loanDetailsResponse.toLoan(account, Loan.Type.OTHER, ITEM_WITH_NULLS_AND_EMPTY);
    }
}
