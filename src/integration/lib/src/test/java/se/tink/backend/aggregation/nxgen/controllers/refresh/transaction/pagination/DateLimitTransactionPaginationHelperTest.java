package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RunWith(MockitoJUnitRunner.class)
public class DateLimitTransactionPaginationHelperTest {

    @Mock private Date dateLimit;

    @Mock private CredentialsRequest request;

    @Mock private Account account;

    @Before
    public void setup() {
        dateLimit =
                Date.from(LocalDate.parse("1970-01-01").atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    @Test
    public void shouldReturnDateLimitWhenAccountsPresent() {
        // given
        TransactionPaginationHelper helper =
                new DateLimitTransactionPaginationHelper(dateLimit, request);

        // when
        Optional<Date> date = helper.getTransactionDateLimit(account);

        // then
        assertThat(date).isPresent();
        assertThat(date.get()).isEqualTo(dateLimit);
    }
}
