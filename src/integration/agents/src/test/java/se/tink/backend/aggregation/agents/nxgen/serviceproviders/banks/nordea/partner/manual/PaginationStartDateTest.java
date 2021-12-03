package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.manual;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerMarketUtil;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class PaginationStartDateTest {

    private Account account;
    private se.tink.backend.agents.rpc.Account rpcAccount;
    private AgentComponentProvider componentProvider;
    private CredentialsRequest credentialsRequest;

    @Before
    public void setUp() throws Exception {
        componentProvider = mock(AgentComponentProvider.class);
        credentialsRequest = mock(CredentialsRequest.class);
        account = mock(Account.class);
        rpcAccount = mock(se.tink.backend.agents.rpc.Account.class);
    }

    @Test
    public void shouldSetDateYearBack() {
        // given
        LocalDate localDate = LocalDate.of(2021, 1, 1);
        LocalDate localDateYearBack = localDate.minus(1, ChronoUnit.YEARS);

        // when
        when(credentialsRequest.getAccounts()).thenReturn(Collections.emptyList());
        when(componentProvider.getLocalDateTimeSource()).thenReturn(getLocalDateTimeSource());
        when(account.isUniqueIdentifierEqual(any())).thenReturn(true);

        LocalDate result =
                NordeaPartnerMarketUtil.getPaginationStartDate(
                        account, credentialsRequest, componentProvider);

        // then
        assertEquals(result, localDateYearBack);
    }

    @Test
    public void shouldSetDateAccordingToCertainDate() {
        // given
        LocalDate localDate = LocalDate.of(2021, 1, 1);
        LocalDate localDateTenDaysBack = localDate.minus(10, ChronoUnit.DAYS);
        Date fakeCertainDate =
                Date.from(
                        localDateTenDaysBack
                                .atStartOfDay()
                                .atZone(ZoneId.systemDefault())
                                .toInstant());
        rpcAccount.setCertainDate(fakeCertainDate);
        credentialsRequest.setAccounts(Collections.singletonList(rpcAccount));

        // when
        when(account.isUniqueIdentifierEqual(any(String.class))).thenReturn(true);
        when(rpcAccount.getCertainDate()).thenReturn(fakeCertainDate);
        when(account.getUniqueIdentifier()).thenReturn("123");
        when(rpcAccount.getBankId()).thenReturn("123");
        when(credentialsRequest.getAccounts()).thenReturn(Collections.singletonList(rpcAccount));
        when(componentProvider.getLocalDateTimeSource()).thenReturn(getLocalDateTimeSource());

        LocalDate result =
                NordeaPartnerMarketUtil.getPaginationStartDate(
                        account, credentialsRequest, componentProvider);

        // then
        assertEquals(result, localDateTenDaysBack);
    }

    private LocalDateTimeSource getLocalDateTimeSource() {
        return new LocalDateTimeSource() {
            @Override
            public LocalDateTime now() {
                return null;
            }

            @Override
            public Instant getInstant() {
                return null;
            }

            @Override
            public LocalDateTime now(ZoneId zoneId) {
                return LocalDateTime.of(2021, 1, 1, 1, 1);
            }

            @Override
            public Instant getInstant(ZoneId zoneId) {
                return null;
            }
        };
    }
}
