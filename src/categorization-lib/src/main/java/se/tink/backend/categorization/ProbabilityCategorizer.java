package se.tink.backend.categorization;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.interfaces.Categorizer;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.core.CategorizationCommand;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.MetricRegistry;

/**
 * The categorizer command weighs together the categorization vectors on the
 * context to set the category for the transaction.
 */
public class ProbabilityCategorizer implements Categorizer {
    private static final LogUtils log = new LogUtils(ProbabilityCategorizer.class);

    private static final double MINIMUM_PROBABILITY = 0.2f;
    public static final Comparator<Map.Entry<CategorizationCommand, CategorizationVector>> COMMAND_CATEGORIZATION_COMPARATOR = Comparator
            .comparingDouble(c -> c.getValue().getWeight());

    private final Category unknownExpenseCategory;
    private final Category unknownIncomeCategory;
    private final ImmutableMap<String, Category> categoriesById;
    private final ImmutableMap<String, Category> categoriesByCode;
    private final ClusterCategories categories;
    private final ImmutableList<Classifier> classifiers;
    private final User user;

    private CategorizationCounter categorizationCounter;
    private String label;

    private Category getDefaultCategory(Transaction transaction) {
        if (transaction.getAmount() > 0) {
            return unknownIncomeCategory;
        } else {
            return unknownExpenseCategory;
        }
    }

    public ProbabilityCategorizer(User user,
            CategoryConfiguration categoryConfiguration, MetricRegistry metricRegistry,
            ClusterCategories categories, Collection<Classifier> classifiers, String label) {

        this.user = user;
        this.categories = categories;
        this.categoriesById = Maps.uniqueIndex(categories.get(), Category::getId);
        this.categoriesByCode = Maps.uniqueIndex(categories.get(), Category::getCode);
        this.unknownExpenseCategory = categoriesByCode.get(categoryConfiguration.getExpenseUnknownCode());
        this.unknownIncomeCategory = categoriesByCode.get(categoryConfiguration.getIncomeUnknownCode());
        this.categorizationCounter = new CategorizationCounter(metricRegistry);
        this.classifiers = ImmutableList.copyOf(classifiers);
        this.label = label;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("unknownExpenseCategory", unknownExpenseCategory)
                .add("unknownIncomeCategory", unknownIncomeCategory)
                .toString();
    }

    @Override
    public Category categorize(Transaction transaction) {

        // Get calculate vectors from all classifiers.

        Map<CategorizationCommand, CategorizationVector> categorizationVectors = classifiers.stream()
                .map(c -> c.categorize(transaction))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(o -> o.command, o -> o.vector));

        if (shouldSkipTransfer(transaction, categorizationVectors)) {
            log.debug("Skipping transfer categorization for transaction " + transaction.getId());
            return categoriesById.get(transaction.getCategoryId());
        }

        // Fetch the categorization vectors from the context

        Category category = null;
        Map<String, Double> mostProbable = null;

        if (categorizationVectors != null) {
            // Merge categorization vectors
            CategorizationVector categorization = CategorizationVector.merge(categorizationVectors.values());

            // Get the best fitted category
            mostProbable = categorization.getMostProbable();
            category = getBestFittedCategory(mostProbable);
        }

        // If no appropriate category could be found, fall back to using the default category
        if (category == null || !transaction.isValidCategoryType(category.getType())) {
            category = getDefaultCategory(transaction);
            categorizationCounter.notCategorized();
        } else {
            final Set<CategorizationCommand> categorizationCommands = getCategorisationCommandsByCategoryCodes(
                    categoryCodesToCategorisationCommands(categorizationVectors), mostProbable.keySet());
            categorizationCounter.categorized(categorizationCommands);
        }

        // Log categorization vectors if categorization changed.
        if ((transaction.getCategoryId() == null || !transaction.getCategoryId().equals(category.getId()))
                && user.isDebug()) {
            log.info(user.getId(), String.format("[transactionId:%s] Setting category to %s, based on %s.",
                    transaction.getId(), category.getCode(), categorizationVectors));
        }

        return category;
    }

    @Override
    public String getLabel() {
        return label;
    }

    private boolean shouldSkipTransfer(Transaction transaction,
            Map<CategorizationCommand, CategorizationVector> transactionCategorizations) {
        return transaction.getCategoryType() != null && transaction.getCategoryType().equals(CategoryTypes.TRANSFERS)
                && !userLearningCategorizationIsMostProbable(transactionCategorizations);
    }

    private boolean userLearningCategorizationIsMostProbable(
            Map<CategorizationCommand, CategorizationVector> transactionCategorizations) {
        return transactionCategorizations != null
                && mostProbableCategorizationCommand(transactionCategorizations)
                .map(CategorizationCommand.USER_LEARNING::equals)
                .orElse(false);
    }

    private Optional<CategorizationCommand> mostProbableCategorizationCommand(
            Map<CategorizationCommand, CategorizationVector> transactionCategorizations) {
        if (transactionCategorizations.isEmpty()) {
            return Optional.empty();
        }

        CategorizationCommand result = Collections
                .max(transactionCategorizations.entrySet(), COMMAND_CATEGORIZATION_COMPARATOR).getKey();

        return Optional.of(result);
    }

    private Category getBestFittedCategory(Map<String, Double> mostProbable) {

        if (mostProbable.size() == 1) {
            String categoryCode = Iterables.getFirst(mostProbable.keySet(), null);
            if (!Strings.isNullOrEmpty(categoryCode)
                    && mostProbable.get(categoryCode) > ProbabilityCategorizer.MINIMUM_PROBABILITY) {
                return categoriesByCode.get(categoryCode);
            }
        } else if (mostProbable.size() > 1) {
            // Find the common parent (if it exists) and return its default child
            Category commonParent = getCommonParent(mostProbable.keySet());
            if (commonParent != null) {
                return commonParent.getDefaultChild(categories.get());
            }
        }

        return null;
    }

    /**
     * Get the common parent category.
     *
     * @return common parent category (if exists; otherwise {@code null})
     */
    private Category getCommonParent(Iterable<String> categoryCodes) {
        Category parent = null;
        for (String code : categoryCodes) {
            Category c = categoriesByCode.get(code);

            if (c == null) {
                continue;
            }

            if (parent == null) {
                parent = categoriesById.get(c.getParent());
            }

            if (parent == null || !parent.getId().equals(c.getParent())) {
                return null;
            }
        }

        return parent;
    }

    /***
     * Create a map from categorization vectors, which represents categories to categorizations commands, where this
     * category was calculated for current transaction
     *
     * @param categorizationVectors from `context.getCategorizationVectors(transactionId);`
     * @return map, which contains categories' codes and categorizations commands, where this category was set
     * for current transaction
     */
    @VisibleForTesting
    static Multimap<String, CategorizationCommand> categoryCodesToCategorisationCommands(
            final Map<CategorizationCommand, CategorizationVector> categorizationVectors) {
        Multimap<String, CategorizationCommand> categoryCodeToCategorisationCommands = ArrayListMultimap.create();

        for (CategorizationCommand categorizationCommand : categorizationVectors.keySet()) {
            for (String code : categorizationVectors.get(categorizationCommand).getDistribution().keySet()) {
                categoryCodeToCategorisationCommands.put(code, categorizationCommand);
            }
        }

        return categoryCodeToCategorisationCommands;
    }

    /***
     * Merge all categorizations commands by chosen categories. This method shows which categorizations commands have
     * impact on chosen transaction category
     *
     * @param categoryCodeToCategorisationCommands is map, which represents categories to categorizations commands
     * @param categoryCodes is interested categories' code.
     * @return set of categorizations commands
     */
    @VisibleForTesting
    static Set<CategorizationCommand> getCategorisationCommandsByCategoryCodes(
            Multimap<String, CategorizationCommand> categoryCodeToCategorisationCommands,
            Collection<String> categoryCodes) {
        Set<CategorizationCommand> categorizationCommands = Sets.newHashSet();
        for (String categoryCode : categoryCodes) {
            categorizationCommands.addAll(categoryCodeToCategorisationCommands.get(categoryCode));
        }

        return categorizationCommands;
    }
}
