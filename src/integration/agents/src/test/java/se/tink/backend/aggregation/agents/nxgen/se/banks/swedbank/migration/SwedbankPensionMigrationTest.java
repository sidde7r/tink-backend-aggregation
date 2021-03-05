package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.migration;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.DetailedPensionEntity;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class SwedbankPensionMigrationTest {
    private SwedbankSEApiClient apiClient;
    private SystemUpdater systemUpdater;
    private DetailedPensionEntity pensionEntity;
    private static String PENSION_OLD_IDENTIFIER = "1112223332";
    private static String PENSION_NEW_IDENTIFIER = "800021112223332";

    @Before
    public void setUp() {
        apiClient = mock(SwedbankSEApiClient.class);
        systemUpdater = mock(SystemUpdater.class);
        pensionEntity =
                SerializationUtils.deserializeFromString(
                        SwedbankPensionMigrationTestData.PENSION_DETAIL,
                        DetailedPensionEntity.class);
    }

    @Test
    public void testAggregateNewPension() {
        // Case for new users: use new identifier

        // Given
        List<Account> existingAccounts = Lists.emptyList();

        // When
        InvestmentAccount parsedAccount =
                pensionEntity.toTinkInvestmentAccount(apiClient, existingAccounts, systemUpdater);

        // Then
        assertEquals(
                "Parsed account has new identifier",
                PENSION_NEW_IDENTIFIER,
                parsedAccount.getIdModule().getUniqueId());
    }

    @Test
    public void testPensionWithExistingOldId() {
        // Case when a user has aggregated an account with old identifier:
        // migrate existing account, and use new identifier

        // Given
        List<Account> existingAccounts = createAccountsWithBankIds(PENSION_OLD_IDENTIFIER);

        // When
        InvestmentAccount parsedAccount =
                pensionEntity.toTinkInvestmentAccount(apiClient, existingAccounts, systemUpdater);

        // Then
        assertEquals(
                "Parsed account has new identifier",
                PENSION_NEW_IDENTIFIER,
                parsedAccount.getIdModule().getUniqueId());
        assertEquals(
                "Existing account is migrated to new identifier",
                PENSION_NEW_IDENTIFIER,
                existingAccounts.get(0).getBankId());
    }

    @Test
    public void testPensionWithExistingOldAndNewId() {
        // Case when duplicate exists already:
        // one of them will be closed, but should be deleted manually

        // Given
        List<Account> existingAccounts =
                createAccountsWithBankIds(PENSION_OLD_IDENTIFIER, PENSION_NEW_IDENTIFIER);

        // When
        InvestmentAccount parsedAccount =
                pensionEntity.toTinkInvestmentAccount(apiClient, existingAccounts, systemUpdater);

        // Then
        assertEquals(
                "Parsed account has old identifier",
                PENSION_OLD_IDENTIFIER,
                parsedAccount.getIdModule().getUniqueId());
        assertEquals(
                "Identifier on existing account is not changed",
                PENSION_OLD_IDENTIFIER,
                existingAccounts.get(0).getBankId());
        assertEquals(
                "Identifier on existing account is not changed",
                PENSION_NEW_IDENTIFIER,
                existingAccounts.get(1).getBankId());
    }

    @Test
    public void testPensionWithExistingNewId() {
        // Case when user has aggregated account with new identifier:
        // use new identifier

        // Given
        List<Account> existingAccounts = createAccountsWithBankIds(PENSION_NEW_IDENTIFIER);

        // When
        InvestmentAccount parsedAccount =
                pensionEntity.toTinkInvestmentAccount(apiClient, existingAccounts, systemUpdater);

        // Then
        assertEquals(
                "Parsed account has new identifier",
                PENSION_NEW_IDENTIFIER,
                parsedAccount.getIdModule().getUniqueId());
        assertEquals(
                "Identifier on existing account is not changed",
                PENSION_NEW_IDENTIFIER,
                existingAccounts.get(0).getBankId());
    }

    private List<Account> createAccountsWithBankIds(String... bankIds) {
        List<Account> accounts = new ArrayList<>(bankIds.length);
        for (String bankId : bankIds) {
            Account account = new Account();
            account.setId(UUIDUtils.generateUUID());
            account.setBankId(bankId);
            accounts.add(account);

            // update bankID with system updater
            when(systemUpdater.updateAccountMetaData(eq(account.getId()), any()))
                    .then(
                            call -> {
                                account.setBankId(call.getArgument(1));
                                return account;
                            });
        }
        return accounts;
    }
}
