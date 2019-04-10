package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.ActivityListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.Message;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.SubcardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class AmericanExpressV62Predicates {

    public static final Function<ActivityListEntity, List<TransactionEntity>>
            getTransactionsFromGivenPage =
                    activity ->
                            Optional.ofNullable(activity.getTransactionList())
                                    // If we don't have more pages, we return true for
                                    // `canStillFetch` and use it
                                    // in paginator
                                    .orElse(Lists.emptyList());
    public static final Predicate<CardEntity> cancelledCardSummaryValuePredicate =
            c -> {
                if ("true".equals(c.getCanceled())) {
                    return false;
                }
                return true;
            };
    protected static final Function<String, String> getCardEndingNumbers =
            fullCardNumber -> fullCardNumber.split("-")[1].trim();
    private static final String ERROR = "ERROR";
    private static final Logger LOGGER =
            LoggerFactory.getLogger(AmericanExpressV62Predicates.class);
    // Cancelled card contains Message with type ERROR
    public static final Predicate<CardEntity> cancelledCardsPredicate =
            c -> {
                Message m = c.getMessage();
                if (m != null && ERROR.equalsIgnoreCase(m.getType())) {
                    // TODO: Not sure if should be warning or just info
                    LOGGER.warn(
                            "Credit card not included because of error message: "
                                    + m.getShortValue());
                    return false;
                }
                return true;
            };
    /*
    If the transactions belongs to any "partner" card (card that is visible in main veiw and in subcards view)
    We want to filter out this transaction.
    If it does not, even if it's not "main card" transaction, it's just a "subcard " transaction that we would like to keep
     */
    public static BiPredicate<TransactionEntity, List<SubcardEntity>>
            checkIfTransactionsBelongsToPartnerCards =
                    (transaction, partnerCards) ->
                            !partnerCards.stream()
                                    .map(card -> card.getSuppIndex())
                                    .filter(
                                            subIndex ->
                                                    !subIndex.equals(transaction.getSuppIndex()))
                                    .collect(Collectors.toList())
                                    .isEmpty();

    public static BiPredicate<CardEntity, SubcardEntity> isPartnerCard =
            (cardEntity, subcardEntity) ->
                    getCardEndingNumbers
                            .apply(cardEntity.getCardNumberDisplay())
                            .equals(getCardEndingNumbers.apply(subcardEntity.getCardProductName()));
    public static BiPredicate<CreditCardAccount, CardEntity> notMainCard =
            (main, subCard) ->
                    !getCardEndingNumbers
                            .apply(subCard.getCardNumberDisplay())
                            .equals(getCardEndingNumbers.apply(main.getAccountNumber()));

    public static BiPredicate<SubcardEntity, List<CardEntity>> checkIfSubcardIsParterCard =
            (subCard, partnerCardList) ->
                    partnerCardList.stream()
                            .anyMatch(
                                    mainCard ->
                                            AmericanExpressV62Predicates.isPartnerCard.test(
                                                    mainCard, subCard));

    public static final Consumer<TransactionEntity> transformIntoTinkTransactions(
            AmericanExpressV62Configuration config, List<Transaction> list) {
        return transaction -> list.add(transaction.toTransaction(config, false));
    }

    public static final List<CardEntity> filterOutMainCardFromPartnerCards(
            List<CardEntity> cardList, CreditCardAccount account) {
        return cardList.stream()
                .filter(subCard -> AmericanExpressV62Predicates.notMainCard.test(account, subCard))
                .collect(Collectors.toList());
    }

    public static List<SubcardEntity> getPartnerCardsFromSubcards(
            List<SubcardEntity> subcardList, List<CardEntity> partnerCardList) {
        return subcardList.stream()
                .filter(subcard -> checkIfSubcardIsParterCard.test(subcard, partnerCardList))
                .collect(Collectors.toList());
    }
}
