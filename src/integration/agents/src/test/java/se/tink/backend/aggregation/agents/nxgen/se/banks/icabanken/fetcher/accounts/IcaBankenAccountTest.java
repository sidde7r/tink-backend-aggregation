package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.TransactionsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities.LoanEntity;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IcaBankenAccountTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/icabanken/resources";

    private static List<AccountEntity> allAccounts;
    private static LoanEntity loanEntity;
    private static LoanAccount loanAccount;

    @BeforeClass
    public static void setup() {
        IcaBankenApiClient apiClient = mock(IcaBankenApiClient.class);

        loanEntity = mock(LoanEntity.class);

        when(apiClient.fetchAccounts())
                .thenReturn(
                        deserializeFromFile("transactionalAccounts.json", AccountsResponse.class)
                                .getBody()
                                .getAccounts());

        allAccounts = apiClient.fetchAccounts().getAllAccounts();

        when(loanEntity.toTinkLoan())
                .thenReturn(deserializeFromFile("loanAccount.json", LoanEntity.class).toTinkLoan());

        loanAccount = loanEntity.toTinkLoan();
    }

    @Test
    public void shouldParseTransactionalAccountUniqueIdentifierCorrectly() {
        Iterator<AccountEntity> iterator = allAccounts.iterator();
        assertEquals(2, allAccounts.size());
        assertTrue(
                iterator.next()
                        .toTinkTransactionalAccount()
                        .isUniqueIdentifierEqual("92720000003"));
        assertTrue(
                iterator.next()
                        .toTinkTransactionalAccount()
                        .isUniqueIdentifierEqual("92720000001"));
    }

    @Test
    public void shouldParseTransactionalAccountIdentifiersCorrectly() {
        Iterator<AccountEntity> iterator = allAccounts.iterator();

        Set<AccountIdentifier> identifiers =
                iterator.next().toTinkTransactionalAccount().getIdentifiers();

        assertEquals(2, identifiers.size());
        assertTrue(identifiers.contains(new IbanIdentifier("SE1592700000092720000003")));
        assertTrue(identifiers.contains(new SwedishIdentifier("92720000003")));
    }

    @Test
    public void shouldHandleEmptyAccountsCorrectly() {
        AccountsResponse accounts =
                deserializeFromFile("emptyTransactionalAccount.json", AccountsResponse.class);
        assertEquals(
                new ArrayList<>(),
                accounts.getBody().getAccounts().getAllAccounts().stream()
                        .filter(AccountEntity::isTransactionalAccount)
                        .map(AccountEntity::toTinkTransactionalAccount)
                        .collect(Collectors.toList()));
    }

    @Test
    public void shouldHandleEmptyTransactionsCorrectly() {
        TransactionsBodyEntity transactions =
                deserializeFromFile("emptyTransactionList.json", TransactionsBodyEntity.class);
        assertEquals(new ArrayList<>(), transactions.toTinkTransactions());
    }

    @Test
    public void shouldParseLoanAccountUniqueIdentifierCorrectly() {
        assertTrue(loanAccount.isUniqueIdentifierEqual("12341231233"));
    }

    @Test
    public void shouldParseLoanIdentiferCorrectly() {
        assertEquals(1, loanAccount.getIdModule().getIdentifiers().size());
        assertEquals(
                new SwedishIdentifier("12341231233"),
                loanAccount.getIdModule().getIdentifiers().stream().findFirst().get());
    }

    private static <T> T deserializeFromFile(String fileName, Class<T> responseClass) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, fileName).toFile(), responseClass);
    }
}
