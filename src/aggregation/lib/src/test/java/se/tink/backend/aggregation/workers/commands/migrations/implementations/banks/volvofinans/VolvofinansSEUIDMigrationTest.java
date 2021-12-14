package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.volvofinans;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
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
public class VolvofinansSEUIDMigrationTest {

    private static final String ACCOUNT_NUMBER = "7654321";
    private static final String ACCOUNT_ID =
            "AAwE4USj2EbnSBNqOo5gEGkJZaGbfu3oPNmbc5AxYT0X3mflSNARiYtoOhB95jUgiV8SplRyzkD6JUJDk06XgXDGfbq0m8SBeLaiqhup14gw";

    private final VolvofinansSEUIDMigration migration = new VolvofinansSEUIDMigration();

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
            new Object[] {Collections.emptyList(), true}
        };
    }

    @Test
    @Parameters(method = "migrateDataParams")
    public void shouldMigrateData(
            List<Account> accounts, Tuple expectedBefore, Tuple expectedAfter) {

        // given
        CredentialsRequest request = createRequest();
        request.setAccounts(accounts);

        // before migration
        assertThat(request.getAccounts())
                .extracting("bankId")
                .containsExactly(expectedBefore.toArray());

        // when
        migration.migrateData(request);

        // then
        assertThat(request.getAccounts())
                .extracting("bankId")
                .containsExactly(expectedAfter.toArray());
    }

    private Object[] migrateDataParams() {
        return new Object[] {
            new Object[] {migratedAccounts(), tuple(ACCOUNT_NUMBER), tuple(ACCOUNT_NUMBER)},
            new Object[] {notMigratedAccounts(), tuple(ACCOUNT_ID), tuple(ACCOUNT_NUMBER)},
            new Object[] {
                ListUtils.union(migratedAccounts(), notMigratedAccounts()),
                tuple(ACCOUNT_NUMBER, ACCOUNT_ID),
                tuple(ACCOUNT_NUMBER, ACCOUNT_NUMBER)
            },
            new Object[] {Collections.emptyList(), tuple(), tuple()}
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
        account.setBankId(ACCOUNT_NUMBER);
        account.setAccountNumber(ACCOUNT_NUMBER);
        return new ArrayList<>(Collections.singletonList(account));
    }

    private List<Account> notMigratedAccounts() {
        Account accountWithAccountId = new Account();
        accountWithAccountId.setBankId(ACCOUNT_ID);
        accountWithAccountId.setAccountNumber(ACCOUNT_NUMBER);
        return new ArrayList<>(Collections.singletonList(accountWithAccountId));
    }
}
