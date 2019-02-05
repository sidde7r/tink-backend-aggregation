package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.v62.AmericanExpressV62SEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.SubcardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class AmericanExpressV62PredicatesTest {

    private ObjectMapper mapper = new ObjectMapper();
    private CardEntity mainCardEntity;
    private CardEntity partnerCardEntity;
    private CreditCardAccount mainCard;
    private List<SubcardEntity> partnerCards;
    private TransactionEntity transaction;

    @Before
    public void setUp() throws Exception {
        mainCardEntity =
                mapper.readValue(AmericanExpressV62PredicatesTestData.MAIN_CARD, CardEntity.class);
        mainCard = mainCardEntity.toCreditCardAccount(new AmericanExpressV62SEConfiguration());
        partnerCardEntity =
                mapper.readValue(
                        AmericanExpressV62PredicatesTestData.PARTNER_CARD, CardEntity.class);
        transaction =
                mapper.readValue(
                        AmericanExpressV62PredicatesTestData.TRANSACTION, TransactionEntity.class);
        partnerCards = Lists.newArrayList();
    }

    @Test
    public void testFilterPartnerTransactions_PartnerCardsNotPresent() {
        assertFalse(
                AmericanExpressV62Predicates.checkIfTransactionsBelongsToPartnerCards.test(
                        transaction, partnerCards));
    }

    @Test
    public void testFilterPartnerTransactions_PartnerCardsPresent() throws IOException {
        partnerCards.add(
                mapper.readValue(
                        AmericanExpressV62PredicatesTestData.PARTNER_SUBCARD, SubcardEntity.class));
        assertTrue(
                AmericanExpressV62Predicates.checkIfTransactionsBelongsToPartnerCards.test(
                        transaction, partnerCards));
    }

    @Test
    public void testFilterPartnerTransactions_MorePartnerCardsPresent() throws IOException {
        partnerCards.add(
                mapper.readValue(
                        AmericanExpressV62PredicatesTestData.PARTNER_SUBCARD, SubcardEntity.class));
        partnerCards.add(
                mapper.readValue(
                        AmericanExpressV62PredicatesTestData.REGULAR_SUBCARD, SubcardEntity.class));
        assertTrue(
                AmericanExpressV62Predicates.checkIfTransactionsBelongsToPartnerCards.test(
                        transaction, partnerCards));
    }

    @Test
    public void testIsPartnerCard_IsPartnerCard() throws IOException {
        assertTrue(
                AmericanExpressV62Predicates.isPartnerCard.test(
                        partnerCardEntity,
                        mapper.readValue(
                                AmericanExpressV62PredicatesTestData.PARTNER_SUBCARD,
                                SubcardEntity.class)));
    }

    @Test
    public void testIsPartnerCard_notPartnerCard() throws IOException {
        assertFalse(
                AmericanExpressV62Predicates.isPartnerCard.test(
                        mainCardEntity,
                        mapper.readValue(
                                AmericanExpressV62PredicatesTestData.REGULAR_SUBCARD,
                                SubcardEntity.class)));
    }

    @Test
    public void getCardEndingNumber_fromCardEntity() {
        assertEquals(
                AmericanExpressV62Predicates.getCardEndingNumbers.apply(
                        mainCardEntity.getCardNumberDisplay()),
                AmericanExpressV62PredicatesTestData.CARD_NUMBER);
    }

    @Test
    public void getCardEndingNumber_fromCreditCardAccount() {
        assertEquals(
                AmericanExpressV62Predicates.getCardEndingNumbers.apply(
                        mainCard.getAccountNumber()),
                AmericanExpressV62PredicatesTestData.CARD_NUMBER);
    }

    @Test
    public void notMainCard_whenMainCard() {
        assertFalse(AmericanExpressV62Predicates.notMainCard.test(mainCard, mainCardEntity));
    }

    @Test
    public void notMainCard_whenPartnerCard() {
        assertTrue(AmericanExpressV62Predicates.notMainCard.test(mainCard, partnerCardEntity));
    }

    @Test
    public void filterOutMainCard_whenOnlyMainCard() {
        assertTrue(
                AmericanExpressV62Predicates.filterOutMainCardFromPartnerCards(
                                Lists.newArrayList(mainCardEntity), mainCard)
                        .isEmpty());
    }

    @Test
    public void filterOutMainCard_whenMainAndPartnerCard() {

        List<CardEntity> cardEntities =
                AmericanExpressV62Predicates.filterOutMainCardFromPartnerCards(
                        Lists.newArrayList(partnerCardEntity, mainCardEntity), mainCard);
        assertEquals(1, cardEntities.size());
        assertNotEquals(cardEntities.get(0), mainCardEntity);
        assertEquals(cardEntities.get(0), partnerCardEntity);
    }

    @Test
    public void filterOutMainCard_whenOnlyPartnerCard() {

        List<CardEntity> cardEntities =
                AmericanExpressV62Predicates.filterOutMainCardFromPartnerCards(
                        Lists.newArrayList(partnerCardEntity), mainCard);
        assertEquals(1, cardEntities.size());
        assertEquals(cardEntities.get(0), partnerCardEntity);
    }

    @Test
    public void getPartnerCardsFromSubcards_whenOnePArtnerCard() throws IOException {
        partnerCards.add(
                mapper.readValue(
                        AmericanExpressV62PredicatesTestData.PARTNER_SUBCARD, SubcardEntity.class));
        List<SubcardEntity> cardEntities =
                AmericanExpressV62Predicates.getPartnerCardsFromSubcards(
                        partnerCards, Lists.newArrayList(partnerCardEntity));
        assertEquals(1, cardEntities.size());
        assertEquals(
                AmericanExpressV62Predicates.getCardEndingNumbers.apply(
                        cardEntities.get(0).getCardProductName()),
                AmericanExpressV62Predicates.getCardEndingNumbers.apply(
                        partnerCardEntity.getCardNumberDisplay()));
    }

    @Test
    public void getPartnerCardsFromSubcards_whenNoPartnerCard() {
        List<SubcardEntity> cardEntities =
                AmericanExpressV62Predicates.getPartnerCardsFromSubcards(
                        partnerCards, Collections.EMPTY_LIST);
        assertTrue(cardEntities.isEmpty());
    }

    @Test
    public void getPartnerCardsFromSubcards_whenNoSubcards() {
        List<SubcardEntity> cardEntities =
                AmericanExpressV62Predicates.getPartnerCardsFromSubcards(
                        Collections.EMPTY_LIST, Lists.newArrayList(partnerCardEntity));
        assertTrue(cardEntities.isEmpty());
    }

    @Test
    public void checkIfSubcardIsParterCard_IsPartnerCard() throws IOException {
        SubcardEntity subcardEntity = mapper.readValue(
                AmericanExpressV62PredicatesTestData.PARTNER_SUBCARD, SubcardEntity.class);
        assertTrue(AmericanExpressV62Predicates.checkIfSubcardIsParterCard.test(
                        subcardEntity, Lists.newArrayList(partnerCardEntity)));
    }

    @Test
    public void checkIfSubcardIsParterCard_NotPartnerCard() throws IOException {
        SubcardEntity subcardEntity = mapper.readValue(
                AmericanExpressV62PredicatesTestData.REGULAR_SUBCARD, SubcardEntity.class);
        assertFalse(AmericanExpressV62Predicates.checkIfSubcardIsParterCard.test(
                       subcardEntity,  Lists.newArrayList(partnerCardEntity)));
    }
}
