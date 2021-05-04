package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.libraries.credentials.service.RefreshableItem;

public class ConsentPermissionsMapperTest {

    private ConsentPermissionsMapper mapper;

    @Before
    public void setUp() throws Exception {
        UkOpenBankingAisConfig mockedAisConfig = mock(UkOpenBankingAisConfig.class);
        this.mapper = new ConsentPermissionsMapper(mockedAisConfig);
    }

    @Test
    public void shouldMapAccountItemsCorrectly() {
        // given
        Set<RefreshableItem> items =
                Sets.newHashSet(RefreshableItem.CHECKING_ACCOUNTS, RefreshableItem.SAVING_ACCOUNTS);

        // when
        ImmutableSet<String> permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_ACCOUNTS_DETAIL.getValue(),
                        ConsentPermission.READ_BALANCES.getValue());
    }

    @Test
    public void shouldMapAllAccountItemsCorrectly() {
        // given
        Set<RefreshableItem> items =
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.CREDITCARD_ACCOUNTS,
                        RefreshableItem.LOAN_ACCOUNTS,
                        RefreshableItem.INVESTMENT_ACCOUNTS);

        // when
        ImmutableSet<String> permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_ACCOUNTS_DETAIL.getValue(),
                        ConsentPermission.READ_BALANCES.getValue());
    }

    @Test
    public void shouldMapTransactionsItemsCorrectly() {
        // given
        Set<RefreshableItem> items =
                Sets.newHashSet(
                        RefreshableItem.CHECKING_TRANSACTIONS, RefreshableItem.SAVING_TRANSACTIONS);

        // when
        ImmutableSet<String> permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_TRANSACTIONS_DETAIL.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_CREDITS.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_DEBITS.getValue());
    }

    @Test
    public void shouldMapAllTransactionsItemsCorrectly() {
        // given
        Set<RefreshableItem> items =
                Sets.newHashSet(
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS,
                        RefreshableItem.CREDITCARD_TRANSACTIONS,
                        RefreshableItem.INVESTMENT_TRANSACTIONS,
                        RefreshableItem.LOAN_TRANSACTIONS);

        // when
        ImmutableSet<String> permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_TRANSACTIONS_DETAIL.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_CREDITS.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_DEBITS.getValue());
    }

    @Test
    public void shouldMapAccountAndTransactionsItemsCorrectly1() {
        // given
        Set<RefreshableItem> items =
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS);

        // when
        ImmutableSet<String> permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_ACCOUNTS_DETAIL.getValue(),
                        ConsentPermission.READ_BALANCES.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_DETAIL.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_CREDITS.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_DEBITS.getValue());
    }
}
