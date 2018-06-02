package se.tink.backend.categorization.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import se.tink.backend.categorization.CategorizationVector;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.common.search.SimilarTransactionsSearcher;
import se.tink.backend.common.search.containers.TransactionSearchContainer;
import se.tink.backend.core.CategorizationCommand;
import se.tink.backend.core.CategorizationWeight;
import se.tink.backend.core.Category;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.Predicates;

/**
 * Find similar transactions where user changed category
 * <p>
 * Sets: categoryId
 */
public class UserLearningCommand implements Classifier {
    public static final Comparator<Map.Entry<String, Collection<SearchHit>>> BY_MOST_HITS = Comparator.comparing(
            Map.Entry::getValue, Comparator.comparing(Collection::size));
    private static double CHANGE_THRESHOLD = 0.5;
    private static ObjectMapper mapper = new ObjectMapper();

    private final ImmutableMap<String, Category> categoriesById;
    private final SimilarTransactionsSearcher similarTransactionsSearcher;
    private final String userId;
    private final boolean anyUserModifiedTransaction;
    private static final Comparator<Map.Entry<String, Collection<SearchHit>>> BY_TOTAL_SCORE = Comparator
            .comparing(Map.Entry::getValue, Comparator.comparing(UserLearningCommand::calculateTotalScore));

    public UserLearningCommand(String userId, SimilarTransactionsSearcher similarTransactionsSearcher,
            ClusterCategories categories, Collection<Transaction> instoreTransactions) {
        this.userId = Preconditions.checkNotNull(userId);
        this.categoriesById = Maps.uniqueIndex(categories.get(), Category::getId);

        this.similarTransactionsSearcher = Preconditions.checkNotNull(similarTransactionsSearcher);

        // TODO: TPv2 modifies the instoreTransactions list after instantiation. Could this be a bug?
        anyUserModifiedTransaction = instoreTransactions.stream()
                .anyMatch(t -> t.isUserModifiedCategory() || t.isUserModifiedLocation());
    }

    private static String extractCategoryIdFromSearchHit(SearchHit hit) {
        Map<String, Object> result = (Map<String, Object>) hit.getSource().get("category");
        return (String) result.get("id");
    }

    private static Transaction convertToTransaction(SearchHit hit) {
        return mapper.convertValue(hit.getSource(), TransactionSearchContainer.class).getTransaction();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }

    @Override
    public Optional<Outcome> categorize(Transaction transaction) {
        // No description to base the lookup on
        if (Strings.isNullOrEmpty(transaction.getDescription())) {
            return Optional.empty();
        }

        // The transaction has already been categorized by the user
        if (transaction.isUserModifiedCategory()) {
            return Optional.empty();
        }

        // No transactions have ever been modified by the user (hence, no user learning exists)
        if (!anyUserModifiedTransaction) {
            return Optional.empty();
        }

        SearchResponse response = similarTransactionsSearcher
                .queryElasticSearchForSimilarTransactions(transaction, userId, null);

        // No hits found
        if (response.getHits() == null || response.getHits().getHits().length == 0) {
            return Optional.empty();
        }

        // Set category.
        return handleUserLearningCategorization(transaction, response)
                .map(v -> new Outcome(CategorizationCommand.USER_LEARNING, v));
    }

    /**
     * Check user categorized transaction to see if this transaction should be changed as well.
     *
     * @param transaction
     * @param response
     */
    private Optional<CategorizationVector> handleUserLearningCategorization(Transaction transaction,
            SearchResponse response) {

        // Filter result on userModifiedCategory = true and index by categoryId.
        ListMultimap<String, SearchHit> changedHitsByCategoryId = filterAndIndexSearchHits(response,
                Transaction::isUserModifiedCategory);

        if (changedHitsByCategoryId.isEmpty()) {
            return Optional.empty();
        }

        // Filter result on userModifiedCategory = false and index by categoryId.
        ListMultimap<String, SearchHit> unchangedHitsByCategoryId = filterAndIndexSearchHits(response,
                Predicates.not(Transaction::isUserModifiedCategory));

        int unchangedHits = 0;
        String unchangedCategoryId = null;

        // If category is null for the transaction check against largest old category

        if (Strings.isNullOrEmpty(transaction.getCategoryId())) {
            Optional<Map.Entry<String, Collection<SearchHit>>> entryWithMaxHits = unchangedHitsByCategoryId.asMap()
                    .entrySet().stream()
                    .sorted(BY_MOST_HITS.reversed())
                    .findFirst();

            unchangedHits = entryWithMaxHits.map(Map.Entry::getValue).map(Collection::size).orElse(0);
            unchangedCategoryId = entryWithMaxHits.map(Map.Entry::getKey).orElse(null);
        } else if (unchangedHitsByCategoryId.containsKey(transaction.getCategoryId())) {
            unchangedHits = unchangedHitsByCategoryId.get(transaction.getCategoryId()).size();
            unchangedCategoryId = transaction.getCategoryId();
        }

        final int totalNumberOfChangedTransactions = changedHitsByCategoryId.size();

        // Find categories with highest score.

        final List<Map.Entry<String, Collection<SearchHit>>> highestScoredCategories = changedHitsByCategoryId.asMap()
                .entrySet().stream()
                .sorted(BY_TOTAL_SCORE.reversed())
                .limit(2)
                .collect(Collectors.toList());

        // We assume highestScoredCategories isn't empty since we expect to have returned early further up in code if
        // changedHitsByCategoryId is empty.
        final Map.Entry<String, Collection<SearchHit>> categoryWithHighestScore = highestScoredCategories.get(0);

        final Optional<Collection<SearchHit>> categoryWithSecondHighestScore =
                highestScoredCategories.size() > 1 ?
                        Optional.of(highestScoredCategories.get(1).getValue()) :
                        Optional.empty();

        final double bestScore = calculateTotalScore(categoryWithHighestScore.getValue());
        final int bestScoreNumberOfChangedTransactions = categoryWithHighestScore.getValue().size();
        final String bestCategoryId = categoryWithHighestScore.getKey();
        final double secondBestScore = categoryWithSecondHighestScore
                .map(UserLearningCommand::calculateTotalScore)
                .orElse(0.0);

        if (bestCategoryId == null) {
            return Optional.empty();
        }

        // Check that the largest category is a threshold % bigger than secondLargest.

        if ((bestScore - secondBestScore) / secondBestScore <= CHANGE_THRESHOLD) {
            return Optional.empty();
        }

        // Check best score category against the largest category of the unchanged transactions.

        if (unchangedCategoryId != null) {
            if ((double) (bestScoreNumberOfChangedTransactions)
                    / (double) (totalNumberOfChangedTransactions + unchangedHits) < CHANGE_THRESHOLD &&
                    !unchangedCategoryId.equals(bestCategoryId)) {
                return Optional.empty();
            }
        }

        // Change category.

        Category bestCategory = categoriesById.get(bestCategoryId);

        if (bestCategory != null) {
            return Optional.of(new CategorizationVector(CategorizationWeight.USER_LEARNING, bestCategory.getCode(), 1));
        }

        return Optional.empty();
    }

    private ImmutableListMultimap<String, SearchHit> filterAndIndexSearchHits(SearchResponse response,
            Predicate<Transaction> transactionFilter) {
        return Multimaps.index(
                StreamSupport.stream(response.getHits().spliterator(), false).filter(
                        Predicates.compose(
                                UserLearningCommand::convertToTransaction,
                                transactionFilter
                        )
                ).iterator(),
                UserLearningCommand::extractCategoryIdFromSearchHit
        );
    }

    private static double calculateTotalScore(Collection<SearchHit> searchHits) {
        double totalScore = 0;
        for (SearchHit hit : searchHits) {
            totalScore += hit.getScore();
        }
        return totalScore;
    }
}
