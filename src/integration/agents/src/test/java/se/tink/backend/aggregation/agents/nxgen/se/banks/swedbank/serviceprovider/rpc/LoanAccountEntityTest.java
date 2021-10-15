package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LoanAccountEntityTest {

    @Test
    public void shouldMapLoanForValidLoan() {
        Date date = new GregorianCalendar(1995, Calendar.NOVEMBER, 5).getTime();

        String interest = "0.02";
        LoanAccountEntity loanAccountEntity =
                SerializationUtils.deserializeFromString(
                        "{\n"
                                + "            \"productId\": \"IP-SPAR\",\n"
                                + "            \"availableAmount\": \"66666,66\",\n"
                                + "            \"selectedForQuickbalance\": false,\n"
                                + "            \"type\": \"LOAN\",\n"
                                + "            \"availableForFavouriteAccount\": false,\n"
                                + "            \"availableForPriorityAccount\": true,\n"
                                + "            \"id\": \"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb\",\n"
                                + "            \"name\": \"Car loan\",\n"
                                + "            \"accountNumber\": \"333 333 333-8\",\n"
                                + "            \"clearingNumber\": \"8103-3\",\n"
                                + "            \"fullyFormattedNumber\": \"8103-4,333 333 333-8\",\n"
                                + "            \"nonFormattedNumber\": \"810343333333338\",\n"
                                + "            \"balance\": \"-66666,66\",\n"
                                + "            \"currency\": \"SEK\"\n"
                                + "        }",
                        LoanAccountEntity.class);

        LoanAccount result = loanAccountEntity.toLoanAccount(interest, date).orElse(null);

        Assert.assertEquals(
                "8103-4,333 333 333-8", Objects.requireNonNull(result).getAccountNumber());
        Assert.assertEquals("Car loan", Objects.requireNonNull(result).getName());
        Assert.assertEquals(AccountTypes.LOAN, Objects.requireNonNull(result).getType());
        Assert.assertEquals(
                "SEK", Objects.requireNonNull(result).getExactBalance().getCurrencyCode());
        Assert.assertEquals(
                "-66666.66",
                Objects.requireNonNull(result).getExactBalance().getExactValue().toString());
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionIfLoanHasPositiveAmount() {
        Date date = new GregorianCalendar(1995, Calendar.NOVEMBER, 5).getTime();

        LoanAccountEntity loanAccountEntity =
                SerializationUtils.deserializeFromString(
                        "{\n"
                                + "            \"productId\": \"IP-SPAR\",\n"
                                + "            \"availableAmount\": \"66 666,66\",\n"
                                + "            \"selectedForQuickbalance\": false,\n"
                                + "            \"type\": \"LOAN\",\n"
                                + "            \"availableForFavouriteAccount\": false,\n"
                                + "            \"availableForPriorityAccount\": true,\n"
                                + "            \"id\": \"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb\",\n"
                                + "            \"name\": \"Car loan\",\n"
                                + "            \"accountNumber\": \"333 333 333-8\",\n"
                                + "            \"clearingNumber\": \"8103-3\",\n"
                                + "            \"fullyFormattedNumber\": \"8103-4,333 333 333-8\",\n"
                                + "            \"nonFormattedNumber\": \"810343333333338\",\n"
                                + "            \"balance\": \"66666,66\",\n"
                                + "            \"currency\": \"SEK\"\n"
                                + "        }",
                        LoanAccountEntity.class);

        Throwable throwable = catchThrowable(() -> loanAccountEntity.toLoanAccount("0.02", date));

        assertThat(throwable).isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
