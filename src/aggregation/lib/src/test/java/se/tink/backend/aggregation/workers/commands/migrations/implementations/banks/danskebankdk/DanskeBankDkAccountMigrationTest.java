package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.danskebankdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.collections4.ListUtils;
import org.assertj.core.groups.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RunWith(JUnitParamsRunner.class)
public class DanskeBankDkAccountMigrationTest {

    private static final String NEW_BANK_ID = "1234567890";

    private final DanskeBankDkAccountMigration migration = new DanskeBankDkAccountMigration();

    @Test
    @Parameters(method = "isDataMigratedParams")
    public void shouldCheckIfAccountsAreMigrated(List<Account> accounts, boolean expected) {
        // given
        CredentialsRequest request = createRequest();
        request.setAccounts(accounts);

        // when
        boolean result = migration.isDataMigrated(request);

        // then
        assertThat(result).isEqualTo(expected);
    }

    private Object[] isDataMigratedParams() {
        return new Object[] {
            new Object[] {migratedAccounts(), true},
            new Object[] {notMigratedAccounts(), false},
            new Object[] {ListUtils.union(migratedAccounts(), notMigratedAccounts()), false},
            new Object[] {Collections.emptyList(), true},
        };
    }

    @Test
    @Parameters(method = "migrateDataParams")
    public void shouldMigrateData(List<Account> accounts, int expectedSize, Tuple expected) {
        // given
        CredentialsRequest request = createRequest();
        request.setAccounts(accounts);

        // when
        migration.migrateData(request);

        // then
        assertThat(request.getAccounts())
                .hasSize(expectedSize)
                .extracting("bankId")
                .containsExactly(expected.toArray());
    }

    private Object[] migrateDataParams() {
        return new Object[] {
            new Object[] {migratedAccounts(), 1, tuple(NEW_BANK_ID)},
            new Object[] {notMigratedAccounts(), 3, tuple(NEW_BANK_ID, NEW_BANK_ID, NEW_BANK_ID)},
            new Object[] {
                ListUtils.union(migratedAccounts(), notMigratedAccounts()),
                4,
                tuple(NEW_BANK_ID, NEW_BANK_ID, NEW_BANK_ID, NEW_BANK_ID)
            },
            new Object[] {Collections.emptyList(), 0, tuple()},
        };
    }

    private CredentialsRequest createRequest() {
        return new CredentialsRequest() {
            @Override
            public boolean isManual() {
                return true;
            }

            @Override
            public CredentialsRequestType getType() {
                return CredentialsRequestType.MIGRATE;
            }
        };
    }

    private List<Account> migratedAccounts() {
        Account account = new Account();
        account.setBankId(NEW_BANK_ID);
        return new ArrayList<>(Collections.singletonList(account));
    }

    private List<Account> notMigratedAccounts() {
        Account accountWithLongAccountNumber = new Account();
        Account accountWithIban = new Account();
        Account duplicatedAccount = new Account();

        accountWithLongAccountNumber.setBankId("1234" + NEW_BANK_ID);
        accountWithIban.setBankId("DK123000" + NEW_BANK_ID);
        duplicatedAccount.setBankId(NEW_BANK_ID + "-duplicate-0");

        return Arrays.asList(accountWithLongAccountNumber, accountWithIban, duplicatedAccount);
    }
}
