package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AnswerEntityGlobalPositionResponseTest {
    private static final String TEST_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/evobanco/resources/";
    private static final String HOLDER_NAME = "SZYMON MYSIAK";

    private AnswerEntityGlobalPositionResponse answerEntityGlobalPositionResponse;
    private EvoBancoApiClient evoBancoApiClient;
    AccountHoldersResponse accountHoldersResponse;

    @Before
    public void init() {
        answerEntityGlobalPositionResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_PATH + "global_position_response.json").toFile(),
                        AnswerEntityGlobalPositionResponse.class);
        evoBancoApiClient = mock(EvoBancoApiClient.class);
        when(evoBancoApiClient.fetchAccountHolders(anyString()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_PATH, "account_holders.json").toFile(),
                                AccountHoldersResponse.class));

        SessionStorage sessionStorage = new SessionStorage();
        sessionStorage.put(EvoBancoConstants.Storage.HOLDER_NAME, HOLDER_NAME);
    }

    @Test
    public void
            getTransactionalAccountsShouldReturnCorrectlyCategorisedAsCheckingAndSavingsAccounts() {
        // given
        // when
        Collection<TransactionalAccount> accounts =
                answerEntityGlobalPositionResponse.getTransactionalAccounts(
                        "dummyHolderName", evoBancoApiClient);

        answerEntityGlobalPositionResponse.getCreditCardAccounts();

        // then
        assertThat(accounts).hasSize(5);
        Iterator<TransactionalAccount> iterator = accounts.iterator();
        assertAccount(
                iterator.next(),
                TransactionalAccountType.CHECKING,
                "ES5720802222116842134322",
                "Cuenta Joven");
        assertAccount(
                iterator.next(),
                TransactionalAccountType.CHECKING,
                "ES2220958691301233839322",
                "Cuenta Inteligente");
        assertAccount(
                iterator.next(),
                TransactionalAccountType.SAVINGS,
                "ES2520384822754925793519",
                "Depósito");
        assertAccount(
                iterator.next(),
                TransactionalAccountType.CHECKING,
                "ES9100751447058421936923",
                "Cuenta Base");
        assertAccount(
                iterator.next(),
                TransactionalAccountType.CHECKING,
                "ES4330048561412595737223",
                "TARJETA DE DÉBITO");
    }

    private void assertAccount(
            TransactionalAccount account,
            TransactionalAccountType type,
            String iban,
            String accountName) {
        assertThat(account.getType()).isEqualTo(type.toAccountType());
        assertThat(account.getUniqueIdentifier()).isEqualTo(iban);
        assertThat(account.getName()).isEqualTo(accountName);
    }

    @Test
    public void getCreditCardsShouldReturnCorrectlyCategorisedAsCreditCards() {
        // given
        // when
        Collection<CreditCardAccount> creditCards =
                answerEntityGlobalPositionResponse.getCreditCardAccounts();

        // then
        assertThat(creditCards).hasSize(2);
        Iterator<CreditCardAccount> iterator = creditCards.iterator();
        assertCreditCard(iterator.next(), "41331908", "Tarjeta Mixta *1908");
        assertCreditCard(iterator.next(), "45111009", "TARJETA MIXTA *1009");
    }

    private void assertCreditCard(
            CreditCardAccount creditCard, String uniqueIdentifier, String name) {
        assertThat(creditCard.getUniqueIdentifier()).isEqualTo(uniqueIdentifier);
        assertThat(creditCard.getName()).isEqualTo(name);
    }
}
