package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.nordeadk;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.nordeadk.NordeaMigrationAccountAssertion.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NordeaDkAccountMigrationTest {

    private NordeaDkAccountMigration migration = new NordeaDkAccountMigration();

    @Test
    public void shouldMarkLegacyAccountsAsNotMigrated() {
        // given
        CredentialsRequest request = createNotMigratedRequest();
        // when
        boolean migrated = migration.isDataMigrated(request);
        // then
        assertThat(migrated).isFalse();
    }

    @Test
    public void shouldMarkNewAccountsAsMigrated() {
        // given
        CredentialsRequest request = createMigratedRequest();
        // when
        boolean migrated = migration.isDataMigrated(request);
        // then
        assertThat(migrated).isTrue();
    }

    @Test
    public void shouldMigrateLegacyAccounts() {
        // given
        CredentialsRequest request = createNotMigratedRequest();
        // when
        migration.migrateData(request);
        // then
        assertThat(request.getAccounts()).hasSize(4);
        Account creditCard1 = request.getAccounts().get(0);
        assertThat(creditCard1)
                .isCreditCard()
                .hasBankId("5678")
                .hasAccountNumber("xxxx xxxx xxxx 5678");
        Account checking1 = request.getAccounts().get(2);
        assertThat(checking1)
                .isCheckingAccount()
                .hasBankId("1234567890")
                .hasAccountNumber("0112-3,456-78-90");
    }

    private CredentialsRequest createNotMigratedRequest() {

        CredentialsRequest request = createRequest();
        request.setAccounts(notMigratedAccounts());
        return request;
    }

    private CredentialsRequest createMigratedRequest() {

        CredentialsRequest request = createRequest();
        request.setAccounts(migratedAccounts());
        return request;
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
        List<Account> accounts = new ArrayList<>(4);
        Account creditCardAccount1 = new Account();
        Account creditCardAccount2 = new Account();
        Account checkingAccount1 = new Account();
        Account checkingAccount2 = new Account();

        accounts.add(creditCardAccount1);
        accounts.add(creditCardAccount2);
        accounts.add(checkingAccount1);
        accounts.add(checkingAccount2);

        creditCardAccount1.setType(AccountTypes.CREDIT_CARD);
        creditCardAccount1.setBankId("5678");
        creditCardAccount1.setAccountNumber("1234 **** **** 5678");
        creditCardAccount2.setType(AccountTypes.CREDIT_CARD);
        creditCardAccount2.setBankId("9012");
        creditCardAccount1.setAccountNumber("5678 xxxx xxxx 9012");

        checkingAccount1.setType(AccountTypes.CHECKING);
        checkingAccount1.setBankId("0987654321");
        checkingAccount1.setAccountNumber("DK8765432187654321");
        checkingAccount2.setType(AccountTypes.CHECKING);
        checkingAccount2.setBankId("1234567890");
        checkingAccount2.setAccountNumber("DK1234567812345678");

        return accounts;
    }

    private List<Account> notMigratedAccounts() {
        List<Account> accounts = new ArrayList<>(4);
        Account creditCardAccount1 = new Account();
        Account creditCardAccount2 = new Account();
        Account checkingAccount1 = new Account();
        Account checkingAccount2 = new Account();

        accounts.add(creditCardAccount1);
        accounts.add(creditCardAccount2);
        accounts.add(checkingAccount1);
        accounts.add(checkingAccount2);

        creditCardAccount1.setType(AccountTypes.CREDIT_CARD);
        creditCardAccount1.setBankId("xxxxxxxxxxxx5678");
        creditCardAccount1.setAccountNumber("xxxx xxxx xxxx 5678");
        creditCardAccount2.setType(AccountTypes.CREDIT_CARD);
        creditCardAccount2.setBankId("9012");
        creditCardAccount2.setAccountNumber("5678 xxxx xxxx 9012");

        checkingAccount1.setType(AccountTypes.CHECKING);
        checkingAccount1.setBankId("7890");
        checkingAccount1.setAccountNumber("0112-3,456-78-90");
        checkingAccount2.setType(AccountTypes.CHECKING);
        checkingAccount2.setBankId("0987654321");
        checkingAccount2.setAccountNumber("DK8765432187654321");

        return accounts;
    }
}
