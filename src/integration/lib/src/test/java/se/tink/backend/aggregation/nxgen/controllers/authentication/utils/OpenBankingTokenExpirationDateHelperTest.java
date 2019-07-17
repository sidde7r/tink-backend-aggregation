package se.tink.backend.aggregation.nxgen.controllers.authentication.utils;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class OpenBankingTokenExpirationDateHelperTest {
    @Test(expected = NullPointerException.class)
    public void when_token_lifetime_null_throw_null_pointer_exception() {
        OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(null, ChronoUnit.DAYS);
    }

    @Test(expected = NullPointerException.class)
    public void when_token_lifetime_unit_null_throw_null_pointer_exception() {
        OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(0, null);
    }

    @Test
    public void when_token_lifetime_and_token_lifetime_unit_set_do_not_return_null() {
        Date expirationDateFrom =
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(0, ChronoUnit.DAYS);
        Assertions.assertThat(expirationDateFrom).isNotNull();
    }

    @Test
    public void when_token_null_but_lifetime_and_token_lifetime_unit_set_do_not_return_null() {
        Date expirationDateFrom =
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                        null, 0, ChronoUnit.DAYS);
        Assertions.assertThat(expirationDateFrom).isNotNull();
    }

    @Test
    public void when_token_expires_in_ninety_days_date_must_be_after_token_expiring_in_zero_days() {
        Date expiresInZeroDays =
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(0, ChronoUnit.DAYS);
        Date expiredInNinetyDays =
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(90, ChronoUnit.DAYS);

        Assertions.assertThat(expiredInNinetyDays).isAfter(expiresInZeroDays);
    }
}
