package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.nordea;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.nordeadk.NordeaMigrationAccountAssertion;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NordeaCreditcardAccountMigrationTest {

    private NordeaCreditcardAccountMigration migration = new NordeaCreditcardAccountMigration();

    @Test
    public void shouldMarkAccountsAsNotMigrated() {
        // given
        CredentialsRequest request = createNotMigratedRequest();
        // when
        boolean migrated = migration.isDataMigrated(request);
        // then
        assertThat(migrated).isFalse();
    }

    @Test
    public void shouldMarkAccountsAsMigrated() {
        // given
        CredentialsRequest request = createMigratedRequest();
        // when
        boolean migrated = migration.isDataMigrated(request);
        // then
        assertThat(migrated).isTrue();
    }

    @Test
    public void shouldMigrateAccounts() {
        // given
        CredentialsRequest request = createNotMigratedRequest();
        // when
        migration.migrateData(request);
        // then
        assertThat(request.getAccounts()).hasSize(2);
        Account creditCard1 = request.getAccounts().get(0);
        NordeaMigrationAccountAssertion.assertThat(creditCard1)
                .isCreditCard()
                .hasBankId("5678")
                .hasAccountNumber("1234 **** **** 5678");
        Account creditCard2 = request.getAccounts().get(1);
        NordeaMigrationAccountAssertion.assertThat(creditCard2)
                .isCreditCard()
                .hasBankId("8765")
                .hasAccountNumber("1234 **** **** 8765");
    }

    private CredentialsRequest createNotMigratedRequest() {

        CredentialsRequest request = createRequest();
        request.setAccounts(someMigratedAccounts());
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
        List<Account> accounts = new ArrayList<>(2);
        Account creditCardAccount1 = new Account();
        Account creditCardAccount2 = new Account();

        accounts.add(creditCardAccount1);
        accounts.add(creditCardAccount2);

        creditCardAccount1.setType(AccountTypes.CREDIT_CARD);
        creditCardAccount1.setBankId("5678");
        creditCardAccount1.setAccountNumber("1234 **** **** 5678");
        creditCardAccount2.setType(AccountTypes.CREDIT_CARD);
        creditCardAccount2.setBankId("8765");
        creditCardAccount2.setAccountNumber("1234 **** **** 8765");

        return accounts;
    }

    private List<Account> someMigratedAccounts() {
        List<Account> accounts = new ArrayList<>(2);
        Account creditCardAccount1 = new Account();
        Account creditCardAccount2 = new Account();

        accounts.add(creditCardAccount1);
        accounts.add(creditCardAccount2);

        creditCardAccount1.setType(AccountTypes.CREDIT_CARD);
        creditCardAccount1.setBankId("12345678");
        creditCardAccount1.setAccountNumber("1234 **** **** 5678");
        creditCardAccount2.setType(AccountTypes.CREDIT_CARD);
        creditCardAccount2.setBankId("8765");
        creditCardAccount2.setAccountNumber("1234 **** **** 8765");

        return accounts;
    }
}
