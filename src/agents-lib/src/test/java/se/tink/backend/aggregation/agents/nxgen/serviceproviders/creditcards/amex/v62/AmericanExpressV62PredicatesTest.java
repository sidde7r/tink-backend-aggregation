package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.v62.AmericanExpressV62SEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.SubcardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TransactionEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AmericanExpressV62PredicatesTest {

    private ObjectMapper mapper = new ObjectMapper();
    private CardEntity mainCard;
    private List<SubcardEntity> partnerCards;
    private TransactionEntity transaction;

    @Before
    public void setUp() throws Exception {
        mainCard =
                mapper.readValue(AmericanExpressV62PredicatesTestData.MAIN_CARD, CardEntity.class);
        transaction =
                mapper.readValue(
                        AmericanExpressV62PredicatesTestData.TRANSACTION, TransactionEntity.class);
        partnerCards = Lists.newArrayList();
    }

    @Test
    public void testFilterPartnerTransactions_PartnerCardsNotPresent() {
        assertFalse(
                AmericanExpressV62Predicates.filterPartnerTransactions.test(
                        transaction, partnerCards));
    }

    @Test
    public void testFilterPartnerTransactions_PartnerCardsPresent() throws IOException {
        partnerCards.add(
                mapper.readValue(
                        AmericanExpressV62PredicatesTestData.PARTNER_SUBCARD, SubcardEntity.class));
        assertTrue(
                AmericanExpressV62Predicates.filterPartnerTransactions.test(
                        transaction, partnerCards));
    }

    @Test
    public void testFilterPartnerTransactions_MorePartnerCardsPresent() throws IOException {
        partnerCards.add(
                mapper.readValue(
                        AmericanExpressV62PredicatesTestData.PARTNER_SUBCARD, SubcardEntity.class));
        partnerCards.add(
                mapper.readValue(
                        AmericanExpressV62PredicatesTestData.REGULAR_SUBCARD,
                        SubcardEntity.class));
        assertTrue(
                AmericanExpressV62Predicates.filterPartnerTransactions.test(
                        transaction, partnerCards));
    }

    @Test
    public void testIsPartnerCard_IsPartnerCard() throws IOException {
        assertTrue(
                AmericanExpressV62Predicates.isPartnerCard.test(
                        mainCard.toCreditCardAccount(new AmericanExpressV62SEConfiguration()),
                        mapper.readValue(
                                AmericanExpressV62PredicatesTestData.PARTNER_SUBCARD,
                                SubcardEntity.class)));
    }

    @Test
    public void testIsPartnerCard_notPartnerCard() throws IOException {
        assertFalse(
                AmericanExpressV62Predicates.isPartnerCard.test(
                        mainCard.toCreditCardAccount(new AmericanExpressV62SEConfiguration()),
                        mapper.readValue(
                                AmericanExpressV62PredicatesTestData.REGULAR_SUBCARD,
                                SubcardEntity.class)));
    }

    @Test
    public void getCardEndingNumber() {
        assertEquals(
                AmericanExpressV62Predicates.getCardEndingNumbers.apply(
                        mainCard.getCardNumberDisplay()),
                AmericanExpressV62PredicatesTestData.CARD_NUMBER);
    }
}
