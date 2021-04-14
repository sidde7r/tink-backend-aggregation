package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.rpc;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.jsoup.select.Elements;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.transactionalaccount.entities.AccountEntity;

public class GlobalPositionResponseTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/ruralvia/resources";

    private static String htmlGlobalPosition;
    private static GlobalPositionResponse globalPosition;

    @BeforeClass
    public static void setUp() throws Exception {
        htmlGlobalPosition =
                new String(Files.readAllBytes(Paths.get(TEST_DATA_PATH, "globalPosition.html")));
        globalPosition = new GlobalPositionResponse(htmlGlobalPosition);
    }

    @Test
    public void getAccountsShouldFetch() {
        // when
        List<AccountEntity> result = globalPosition.getAccounts();

        // then
        AccountEntity fetchedAccount = result.get(0);
        AccountEntity mockedAccount = mockedAccount();
        assertEquals(mockedAccount.getAccountNumber(), fetchedAccount.getAccountNumber());
        assertEquals(mockedAccount.getAccountAlias(), fetchedAccount.getAccountAlias());
        assertEquals(mockedAccount.getCurrency(), fetchedAccount.getCurrency());
        assertEquals(mockedAccount.getBalance(), fetchedAccount.getBalance());
    }

    @Test
    public void getAccountsShouldNotFetchWhenFormNotFound() {
        // given
        GlobalPositionResponse globalPositionResponse = new GlobalPositionResponse("");

        // when
        List<AccountEntity> result = globalPositionResponse.getAccounts();

        // then
        assertEquals(0, result.size());
    }

    @Test
    public void findAccountsElementOnHtml() {
        // when
        Elements result = globalPosition.findAccountsElementOnHtml();

        // then
        assertEquals(1, result.size());
    }

    private AccountEntity mockedAccount() {
        AccountEntity accountEntity = new AccountEntity();

        accountEntity.setAccountNumber("ES5000818447506159992545");
        accountEntity.setAccountAlias("C/C PARTICULARES");
        accountEntity.setCurrency("EUR");
        accountEntity.setBalance("100,00");

        return accountEntity;
    }
}
