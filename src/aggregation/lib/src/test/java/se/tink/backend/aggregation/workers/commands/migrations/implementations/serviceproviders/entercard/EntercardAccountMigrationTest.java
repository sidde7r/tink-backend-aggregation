package se.tink.backend.aggregation.workers.commands.migrations.implementations.serviceproviders.entercard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.credentials.service.CreateCredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class EntercardAccountMigrationTest {

    public EnterCardAccountIdMigration migration = new EnterCardAccountIdMigration();

    @Test
    public void shouldMigrationAccountTest() {

        CredentialsRequest request = createMigratedRequest();
        boolean isMigrate = migration.shouldMigrateData(request);
        Assert.assertTrue(isMigrate);
    }

    @Test
    public void shouldNotMigrationAccountTest() {

        CredentialsRequest request = createNotMigratedRequest();
        boolean isNotMigrate = migration.shouldMigrateData(request);
        Assert.assertFalse(isNotMigrate);
    }

    @Test
    public void shouldSkipMigrationAccountTest() {

        CredentialsRequest request = createSkipMigratedRequest();
        boolean isNotMigrate = !migration.shouldMigrateData(request);
        Assert.assertTrue(isNotMigrate);
    }

    private CredentialsRequest createMigratedRequest() {

        CredentialsRequest request = new CreateCredentialsRequest();
        request.setAccounts(toMigrateAccounts());
        return request;
    }

    private CredentialsRequest createNotMigratedRequest() {

        CredentialsRequest request = new CreateCredentialsRequest();
        request.setAccounts(notToMigrateAccounts());
        return request;
    }

    private CredentialsRequest createSkipMigratedRequest() {

        CredentialsRequest request = new CreateCredentialsRequest();
        request.setAccounts(skipMigrationAccounts());
        return request;
    }

    private List<Account> toMigrateAccounts() {
        List<Account> accounts = new ArrayList<>(2);
        Account creditCardAccount1 = new Account();
        Account creditCardAccount2 = new Account();

        accounts.add(creditCardAccount1);
        accounts.add(creditCardAccount2);

        creditCardAccount1.setType(AccountTypes.CREDIT_CARD);
        creditCardAccount1.setBankId("5852111");
        creditCardAccount1.setIdentifiers(
                Stream.of(AccountIdentifier.create(Type.PAYMENT_CARD_NUMBER, "6161000013917309"))
                        .collect(Collectors.toList()));

        creditCardAccount2.setType(AccountTypes.CREDIT_CARD);
        creditCardAccount2.setBankId("4142738");
        creditCardAccount2.setIdentifiers(
                Stream.of(AccountIdentifier.create(Type.PAYMENT_CARD_NUMBER, "4581993000041132"))
                        .collect(Collectors.toList()));

        return accounts;
    }

    private List<Account> notToMigrateAccounts() {
        List<Account> accounts = new ArrayList<>(2);
        Account creditCardAccount1 = new Account();
        Account creditCardAccount2 = new Account();

        accounts.add(creditCardAccount1);
        accounts.add(creditCardAccount2);

        creditCardAccount1.setType(AccountTypes.CREDIT_CARD);
        creditCardAccount1.setBankId("6161000013917309");
        creditCardAccount1.setIdentifiers(
                Stream.of(AccountIdentifier.create(Type.PAYMENT_CARD_NUMBER, "6161000013917309"))
                        .collect(Collectors.toList()));

        creditCardAccount2.setType(AccountTypes.CREDIT_CARD);
        creditCardAccount2.setBankId("4581993000041132");
        creditCardAccount2.setIdentifiers(
                Stream.of(AccountIdentifier.create(Type.PAYMENT_CARD_NUMBER, "4581993000041132"))
                        .collect(Collectors.toList()));

        return accounts;
    }

    private List<Account> skipMigrationAccounts() {
        List<Account> accounts = new ArrayList<>(2);
        Account creditCardAccount1 = new Account();
        Account creditCardAccount2 = new Account();

        accounts.add(creditCardAccount1);
        accounts.add(creditCardAccount2);

        creditCardAccount1.setType(AccountTypes.CREDIT_CARD);
        creditCardAccount1.setBankId("5852111");

        creditCardAccount2.setType(AccountTypes.CREDIT_CARD);
        creditCardAccount2.setBankId("4142738");

        return accounts;
    }
}
