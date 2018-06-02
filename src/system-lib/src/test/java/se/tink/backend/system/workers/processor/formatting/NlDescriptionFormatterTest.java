package se.tink.backend.system.workers.processor.formatting;

import org.junit.Test;
import se.tink.backend.core.Transaction;
import static org.assertj.core.api.Assertions.assertThat;

public class NlDescriptionFormatterTest {

    /**
     * Tests for "[ ,]PAS\\d{3,4}($|( .*))"
     */
    @Test
    public void testForeignTransactionDescriptions() throws Exception {

        NlDescriptionFormatter formatter = new NlDescriptionFormatter();

        assertThat(formatter.clean("SUBWAY EARLS,PAS660")).isEqualTo("SUBWAY EARLS");
        assertThat(formatter.clean("SUBWAY EARLS,PAS660 GBP 9,50 1EUR=0,7257448 GBP KOSTEN ¤0,15 ACHTERAF BEREKEND"))
                .isEqualTo("SUBWAY EARLS");

        // We require a end of line or space after PAS\\d{3,4}
        assertThat(formatter.clean("SUBWAY EARLS,PAS660Word")).isEqualTo("SUBWAY EARLS,PAS660Word");
    }

    /**
     * Tests for "NL By Adyen$"
     */
    @Test
    public void testByAdyenDescriptions() throws Exception {

        NlDescriptionFormatter formatter = new NlDescriptionFormatter();

        assertThat(formatter.clean("Tinder By Adyen")).isEqualTo("Tinder");

        assertThat(formatter.clean("Spotify by Adyen and other...")).isEqualTo("Spotify by Adyen and other...");
    }

    /**
     * Tests for dutch city trimming
     */
    @Test
    public void testCityTrimming() throws Exception {

        NlDescriptionFormatter formatter = new NlDescriptionFormatter();

        // End with city
        assertThat(formatter.getCleanDescription(createTransaction("Q-park Mahler 4 Amsterdam")))
                .isEqualTo("Q-park Mahler 4");

        // End with city and nl
        assertThat(formatter.getCleanDescription(createTransaction("Q-park Mahler 4 Amsterdam Nl")))
                .isEqualTo("Q-park Mahler 4");

        // End with city and Nld
        assertThat(formatter.getCleanDescription(createTransaction("Q-park Mahler 4 Amsterdam Nld")))
                .isEqualTo("Q-park Mahler 4");

        assertThat(formatter.getCleanDescription(createTransaction("Q-park Bodegraven-Reeuwijk")))
                .isEqualTo("Q-park");
    }

    public Transaction createTransaction(String description) {
        Transaction transaction = new Transaction();

        transaction.setOriginalDescription(description);

        return transaction;
    }

}
