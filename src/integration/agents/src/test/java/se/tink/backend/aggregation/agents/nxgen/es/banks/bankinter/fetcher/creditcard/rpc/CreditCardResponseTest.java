package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterTestData.loadTestResponse;

import java.math.BigDecimal;
import java.text.ParseException;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.entities.PaginationKey;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CreditCardResponseTest {
    @Test
    public void testCreditCardResponse() throws ParseException {
        final CreditCardResponse response =
                loadTestResponse("7.tarjetas_ficha.xhtml", CreditCardResponse.class);
        assertTrue(response.isCreditCard());

        final CreditCardAccount account = response.toCreditCardAccount();
        assertEquals("Visa Clasica", account.getName());
        assertEquals("************1234", account.getCardModule().getCardNumber());
        assertEquals("1234", account.getAccountNumber());
        assertEquals(BigDecimal.valueOf(-123.45), account.getExactBalance().getExactValue());
        assertEquals("Fulano Pérez de Tal", account.getHolderName().toString());
        assertEquals(
                BigDecimal.valueOf(1337.42), account.getExactAvailableCredit().getExactValue());

        final PaginationKey paginationKey =
                account.getFromTemporaryStorage(
                                StorageKeys.FIRST_PAGINATION_KEY, PaginationKey.class)
                        .get();
        assertEquals(
                "3qxVkKn9SzljG0B0VSLdwDPSHcawPXH3v5IBxaSwcty2yzUVExbFVS0IQuJlucrWBq8q1Ewd6MgNkiHgG3yNhOIr9BoX8ihLJiRMkntKN0ukQqUY1HQ9FPka5W7jtHjCEd07fdpbDvObFSOLiX7Yb/mGOXCZH4xBpCWRmQ==",
                paginationKey.getViewState());
        assertEquals("movimientos-form:j_id944598273_73dea2bf", paginationKey.getSource());
    }
}
