package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.transaction;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.LaBanquePostaleTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LaBanquePostaleTransactionFetcherTest {

    private LaBanquePostaleTransactionalAccountFetcher objectUnderTest;
    private LaBanquePostaleApiClient client;

    @Before
    public void setup() {
        client = mock(LaBanquePostaleApiClient.class);
        objectUnderTest = new LaBanquePostaleTransactionalAccountFetcher(client);
    }

    @Test
    public void shouldMapTransactionsToCorrectTypes() {
        // given
        when(client.getTransactions(any(), any())).thenReturn(mockedTransactions());
        TransactionalAccount account = mock(TransactionalAccount.class);
        when(account.getApiIdentifier()).thenReturn("");
        when(account.getType()).thenReturn(AccountTypes.CHECKING);
        // when
        Collection<? extends Transaction> tinkTransactions =
                objectUnderTest.getTransactionsFor(account, 0).getTinkTransactions();
        // then
        assertCorrectTypesAssigned(tinkTransactions);
    }

    private void assertCorrectTypesAssigned(Collection<? extends Transaction> tinkTransactions) {
        Transaction creditCardTransaction =
                tinkTransactions.stream()
                        .filter(a -> TransactionTypes.CREDIT_CARD.equals(a.getType()))
                        .findFirst()
                        .orElseThrow(IllegalArgumentException::new);
        Transaction paymentTransaction =
                tinkTransactions.stream()
                        .filter(a -> TransactionTypes.PAYMENT.equals(a.getType()))
                        .findFirst()
                        .orElseThrow(IllegalArgumentException::new);
        Transaction withdrawalTransaction =
                tinkTransactions.stream()
                        .filter(a -> TransactionTypes.WITHDRAWAL.equals(a.getType()))
                        .findFirst()
                        .orElseThrow(IllegalArgumentException::new);
        Transaction transferTransaction =
                tinkTransactions.stream()
                        .filter(a -> TransactionTypes.TRANSFER.equals(a.getType()))
                        .findFirst()
                        .orElseThrow(IllegalArgumentException::new);
        Transaction defaultTransaction =
                tinkTransactions.stream()
                        .filter(a -> TransactionTypes.DEFAULT.equals(a.getType()))
                        .findFirst()
                        .orElseThrow(IllegalArgumentException::new);
        assertThat(creditCardTransaction.getDescription())
                .isEqualTo("ACHAT CB Monese         01.01.20 EUR        101,00 CARTE");
        assertThat(creditCardTransaction.getExactAmount().getExactValue())
                .isEqualByComparingTo("-101.00");
        assertThat(paymentTransaction.getDescription())
                .isEqualTo("000000 VERSEMENT EFFECTUE LE 000000 A BEZONS");
        assertThat(paymentTransaction.getExactAmount().getExactValue())
                .isEqualByComparingTo("100.00");
        assertThat(withdrawalTransaction.getDescription())
                .isEqualTo("000000 RETRAIT EFFECTUE LE 000000  A PARIS");
        assertThat(withdrawalTransaction.getExactAmount().getExactValue())
                .isEqualByComparingTo("-100.00");
        assertThat(transferTransaction.getDescription())
                .isEqualTo("VIREMENT INSTANTANE DE Someotheruser");
        assertThat(transferTransaction.getExactAmount().getExactValue())
                .isEqualByComparingTo("101.00");
        assertThat(defaultTransaction.getDescription())
                .isEqualTo("MINIMUM FORFAITAIRE TRIMESTRIEL D UTILISATION DU DECOUVERT");
        assertThat(defaultTransaction.getExactAmount().getExactValue())
                .isEqualByComparingTo("-1.50");
    }

    TransactionsResponse mockedTransactions() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"avoirDispo\": 0.00,\n"
                        + "  \"code_media\": \"9241\",\n"
                        + "  \"code_ret\": \"0000\",\n"
                        + "  \"compteNumero\": \"1000000E000\",\n"
                        + "  \"date_solde\": \"19/10/2020\",\n"
                        + "  \"decAuto\": {\n"
                        + "    \"avantageFrais\": 0.00,\n"
                        + "    \"montant\": 0.00,\n"
                        + "    \"natureMaj\": \"M\",\n"
                        + "    \"offreGroupee\": {\n"
                        + "      \"libelleType\": \"d'un seuil d'agios\",\n"
                        + "      \"type\": \"4\"\n"
                        + "    },\n"
                        + "    \"tauxApplicable\": 0.00\n"
                        + "  },\n"
                        + "  \"dec_msg\": \"\",\n"
                        + "  \"heureSoldeOp\": \"14h06\",\n"
                        + "  \"libelleTypeCompte\": \"CCP   1000000E000 \",\n"
                        + "  \"message\": \"\",\n"
                        + "  \"nbOperations\": 5,\n"
                        + "  \"operations\": [\n"
                        + "    {\n"
                        + "      \"date\": \"19/08/2020\",\n"
                        + "      \"libelle\": \"000000 RETRAIT EFFECTUE LE\",\n"
                        + "      \"libelleComplementaire\": \"000000  A PARIS\",\n"
                        + "      \"montant\": -100.00\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"date\": \"19/08/2020\",\n"
                        + "      \"libelle\": \"000000 VERSEMENT EFFECTUE LE\",\n"
                        + "      \"libelleComplementaire\": \"000000 A BEZONS\",\n"
                        + "      \"montant\": 100.00\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"date\": \"06/07/2020\",\n"
                        + "      \"libelle\": \"ACHAT CB Monese         01.01.20\",\n"
                        + "      \"libelleComplementaire\": \"EUR        101,00 CARTE\",\n"
                        + "      \"montant\": -101.00\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"date\": \"06/07/2020\",\n"
                        + "      \"libelle\": \"VIREMENT INSTANTANE DE\",\n"
                        + "      \"libelleComplementaire\": \"Someotheruser\",\n"
                        + "      \"montant\": 101.00\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"date\": \"03/07/2020\",\n"
                        + "      \"libelle\": \"MINIMUM FORFAITAIRE TRIMESTRIEL\",\n"
                        + "      \"libelleComplementaire\": \"D UTILISATION DU DECOUVERT\",\n"
                        + "      \"montant\": -1.50\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"solde\": \"0.00\",\n"
                        + "  \"typePartenaire\": \"\"\n"
                        + "}",
                TransactionsResponse.class);
    }
}
