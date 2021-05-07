package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role.HOLDER;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.Balance;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.BalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.NorwegianIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class DnbAccountMapperTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/dnb/resources";
    private static final String TEST_BBAN = "test_bban";
    private static final String TEST_NAME = "test_name";
    private static final String TEST_CURRENCY = "test_currency";
    private static final String TEST_OWNER_NAME = "test_owner_name";

    private static final Balance CLOSING_BOOKED = createBalance(1.1, "closingBooked");
    private static final Balance EXPECTED = createBalance(2.1, "expected");
    private static final Balance OPENING_BOOKED = createBalance(3.1, "openingBooked");
    private static final Balance INTERIM_AVAILABLE = createBalance(4.1, "interimAvailable");
    private static final Balance FORWARD_AVAILABLE = createBalance(5.1, "forwardAvailable");
    private static final Balance OTHER = createBalance(6.1, "asdgkljsglks");
    private static final List<Balance> ALL_BALANCES =
            asList(
                    CLOSING_BOOKED,
                    EXPECTED,
                    OPENING_BOOKED,
                    INTERIM_AVAILABLE,
                    FORWARD_AVAILABLE,
                    OTHER);

    private DnbAccountMapper accountMapper;

    @Before
    public void setup() {
        accountMapper = new DnbAccountMapper();
    }

    @Test
    public void shouldReturnEmptyOptionalWhenFailedToParse() {
        // when
        Optional<TransactionalAccount> maybeAccount = accountMapper.toTinkAccount(null, null);

        // then
        assertThat(maybeAccount.isPresent()).isFalse();
    }

    @Test
    @Parameters(method = "accountNamesToExpectedTypes")
    public void shouldMapCorrectAccountTypes(String accountName, AccountTypes expectedType) {
        // given
        AccountEntity accountEntity = getTestAccount(accountName);
        BalancesResponse balancesResponse = getTestBalances();

        // when
        Optional<TransactionalAccount> maybeAccount =
                accountMapper.toTinkAccount(accountEntity, balancesResponse);

        // then
        assertThat(maybeAccount.isPresent()).isTrue();

        TransactionalAccount account = maybeAccount.get();
        assertThat(account.getType()).isEqualTo(expectedType);
    }

    @SuppressWarnings("unused")
    private static Object accountNamesToExpectedTypes() {
        return new Object[] {
            new Object[] {"SPAREKONTO", AccountTypes.SAVINGS},
            new Object[] {"SAVING", AccountTypes.SAVINGS},
            new Object[] {"SUPERSPAR", AccountTypes.SAVINGS},
            new Object[] {"PLASSERINGSKONTO", AccountTypes.SAVINGS},
            new Object[] {"PLASSERINGSkonto 123", AccountTypes.SAVINGS},
            new Object[] {"Anything else", AccountTypes.CHECKING},
        };
    }

    @Test
    public void shouldMapAccountProperly() {
        // given
        AccountEntity accountEntity = getTestAccount();
        BalancesResponse balancesResponse = getTestBalances();

        // when
        Optional<TransactionalAccount> maybeAccount =
                accountMapper.toTinkAccount(accountEntity, balancesResponse);

        // then
        assertThat(maybeAccount.isPresent()).isTrue();

        TransactionalAccount account = maybeAccount.get();
        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(account.isUniqueIdentifierEqual(TEST_BBAN)).isTrue();
        assertThat(account.getAccountNumber()).isEqualTo(TEST_BBAN);
        assertThat(account.getName()).isEqualTo(TEST_NAME);
        assertThat(account.getIdentifiers()).hasSize(1);
        assertThat(account.getIdentifiers().get(0)).isEqualTo(new NorwegianIdentifier(TEST_BBAN));
        assertThat(account.getApiIdentifier()).isEqualTo(TEST_BBAN);
        assertThat(account.getParties().get(0).getName()).isEqualTo(capitalize(TEST_OWNER_NAME));
        assertThat(account.getParties().get(0).getRole()).isEqualTo(HOLDER);
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.of(1234.0, "NOK"));
    }

    @Test
    @Parameters(method = "bookedBalanceParams")
    public void shouldMapCorrectBookedBalance(
            List<Balance> balancesFromResponse,
            boolean shouldFindBalance,
            Balance expectedBalance) {
        // given
        AccountEntity accountEntity = getTestAccount();
        BalancesResponse balancesResponse = new BalancesResponse();
        balancesResponse.setBalances(balancesFromResponse);

        // when
        Optional<TransactionalAccount> maybeAccount =
                accountMapper.toTinkAccount(accountEntity, balancesResponse);

        // then
        assertThat(maybeAccount.isPresent()).isEqualTo(shouldFindBalance);

        if (maybeAccount.isPresent()) {
            TransactionalAccount account = maybeAccount.get();
            ExactCurrencyAmount bookedAmount = account.getExactBalance();

            assertThat(bookedAmount).isEqualTo(expectedBalance.toTinkAmount());
        }
    }

    @SuppressWarnings("unused")
    private static Object[] bookedBalanceParams() {
        return Stream.of(
                        BalanceMapParams.builder(ALL_BALANCES)
                                .expectedBalance(OPENING_BOOKED)
                                .build(),
                        BalanceMapParams.builder(ALL_BALANCES)
                                .removeBalance(OPENING_BOOKED)
                                .expectedBalance(CLOSING_BOOKED)
                                .build(),
                        BalanceMapParams.builder(ALL_BALANCES)
                                .removeBalance(OPENING_BOOKED, CLOSING_BOOKED)
                                .expectedBalance(EXPECTED)
                                .build(),
                        BalanceMapParams.builder(ALL_BALANCES)
                                .removeBalance(OPENING_BOOKED, CLOSING_BOOKED, EXPECTED)
                                .expectedBalance(INTERIM_AVAILABLE)
                                .build(),
                        BalanceMapParams.builder(ALL_BALANCES)
                                .removeBalance(
                                        OPENING_BOOKED, CLOSING_BOOKED, EXPECTED, INTERIM_AVAILABLE)
                                .build())
                .map(BalanceMapParams::toMethodParams)
                .toArray();
    }

    @Test
    @Parameters(method = "availableBalanceParams")
    public void shouldMapCorrectAvailableBalance(
            List<Balance> balancesFromResponse,
            boolean shouldFindBalance,
            Balance expectedBalance) {
        // given
        AccountEntity accountEntity = getTestAccount();
        BalancesResponse balancesResponse = new BalancesResponse();
        balancesResponse.setBalances(balancesFromResponse);

        // when
        Optional<TransactionalAccount> maybeAccount =
                accountMapper.toTinkAccount(accountEntity, balancesResponse);

        // then
        assertThat(maybeAccount.isPresent()).isTrue();

        TransactionalAccount account = maybeAccount.get();
        ExactCurrencyAmount availableBalance = account.getExactAvailableBalance();
        if (shouldFindBalance) {
            assertThat(availableBalance).isEqualTo(expectedBalance.toTinkAmount());
        } else {
            assertThat(availableBalance).isNull();
        }
    }

    @SuppressWarnings("unused")
    private static Object[] availableBalanceParams() {
        return Stream.of(
                        BalanceMapParams.builder(ALL_BALANCES)
                                .expectedBalance(INTERIM_AVAILABLE)
                                .build(),
                        BalanceMapParams.builder(ALL_BALANCES)
                                .removeBalance(INTERIM_AVAILABLE)
                                .expectedBalance(EXPECTED)
                                .build(),
                        BalanceMapParams.builder(ALL_BALANCES)
                                .removeBalance(INTERIM_AVAILABLE, EXPECTED)
                                .expectedBalance(FORWARD_AVAILABLE)
                                .build(),
                        BalanceMapParams.builder(ALL_BALANCES)
                                .removeBalance(INTERIM_AVAILABLE, EXPECTED, FORWARD_AVAILABLE)
                                .build())
                .map(BalanceMapParams::toMethodParams)
                .toArray();
    }

    @RequiredArgsConstructor
    private static class BalanceMapParams {

        private final List<Balance> balances;
        private final boolean shouldFindBalance;
        private final Balance expectedBalance;

        private Object[] toMethodParams() {
            return new Object[] {balances, shouldFindBalance, expectedBalance};
        }

        @SuppressWarnings("SameParameterValue")
        private static BalanceMapParamsBuilder builder(List<Balance> initialBalances) {
            return new BalanceMapParamsBuilder(initialBalances);
        }

        private static class BalanceMapParamsBuilder {

            private final List<Balance> balances;
            private Balance expectedBalance;

            BalanceMapParamsBuilder(List<Balance> initialBalances) {
                this.balances = new ArrayList<>(initialBalances);
            }

            private BalanceMapParamsBuilder removeBalance(Balance... balances) {
                this.balances.removeAll(asList(balances));
                return this;
            }

            private BalanceMapParamsBuilder expectedBalance(Balance expectedBalance) {
                this.expectedBalance = expectedBalance;
                return this;
            }

            private BalanceMapParams build() {
                return new BalanceMapParams(balances, expectedBalance != null, expectedBalance);
            }
        }
    }

    private static AccountEntity getTestAccount() {
        return getTestAccount(TEST_NAME);
    }

    private static AccountEntity getTestAccount(String name) {
        return new AccountEntity(TEST_BBAN, name, TEST_CURRENCY, TEST_OWNER_NAME);
    }

    private static BalancesResponse getTestBalances() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "balances.json").toFile(), BalancesResponse.class);
    }

    private static Balance createBalance(double amount, String type) {
        Balance balance = new Balance();
        balance.setBalanceType(type);

        AmountEntity amountEntity = new AmountEntity();
        amountEntity.setAmount(String.valueOf(amount));
        amountEntity.setCurrency("NOK");

        balance.setBalanceAmount(amountEntity);
        return balance;
    }
}
