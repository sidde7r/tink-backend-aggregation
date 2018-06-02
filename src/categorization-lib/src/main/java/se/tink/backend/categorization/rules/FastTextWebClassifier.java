package se.tink.backend.categorization.rules;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.categorization.CategorizationVector;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.api.FastTextClassifierService;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.categorization.rpc.FastTextClassifierResult;
import se.tink.backend.categorization.rpc.FastTextLabel;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.utils.FunctionUtils;
import se.tink.backend.common.utils.TransactionUtils;
import se.tink.backend.core.CategorizationCommand;
import se.tink.backend.core.CategorizationWeight;
import se.tink.backend.core.Category;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.LogUtils;

public class FastTextWebClassifier implements Classifier {
    private static final LogUtils log = new LogUtils(FastTextWebClassifier.class);

    private final FastTextClassifierService fastTextClassifierService;
    private final Function<String, String> preprocessor;
    private final String modelName;
    private final CategorizationConfiguration categorizationConfiguration;
    private final CategoryConfiguration categoryConfiguration;
    private final ClusterCategories categories;
    private final ImmutableMap<String, Category> categoriesById;
    private final ImmutableMap<String, Category> categoriesByCode;

    public FastTextWebClassifier(FastTextClassifierService fastTextClassifierService, String modelName
            , List<Function<String, String>> preprocessors
            , CategorizationConfiguration categorizationConfiguration
            , CategoryConfiguration categoryConfiguration
            , ClusterCategories categories) {
        this.fastTextClassifierService = fastTextClassifierService;
        this.modelName = modelName;
        this.categorizationConfiguration = categorizationConfiguration;
        this.categoryConfiguration = categoryConfiguration;
        this.categories = categories;

        this.categoriesById = Maps.uniqueIndex(categories.get(), Category::getId);
        this.categoriesByCode = Maps.uniqueIndex(categories.get(), Category::getCode);
        this.preprocessor = new FunctionUtils<String>().compose(preprocessors);
    }

    public synchronized Optional<Map<String, Double>> predict(String transactionText) {
        if (Strings.isNullOrEmpty(transactionText)) {
            return Optional.empty();
        }

        FastTextClassifierResult result = fastTextClassifierService.classifyQuery(modelName, transactionText);
        return predictionToDistributionMap(transactionText, result.getLabels());
    }

    private Optional<Map<String, Double>> predictionToDistributionMap(String transactionText,
            List<FastTextLabel> labels) {
        Map<String, Double> categorizationDistribution = labels.stream()
                .collect(Collectors.toMap(FastTextLabel::getLabel, FastTextLabel::getPercentage));

        // We require a minimum of two labels to make an educated classification
        if (labels.size() < 2) {
            return Optional.empty();
        }

        // Check a few key points to see whether we trust the output from fastText
        if (TransactionUtils.isSwish(transactionText)
                || categoryConfiguration.getTransferUnknownCode().equals(labels.get(0).getLabel())
                || categoryConfiguration.getExpenseUnknownCode().equals(labels.get(0).getLabel())) {
            return Optional.empty();
        }

        if (lowConfidenceResults(labels.get(0), labels.get(1), categorizationDistribution)) {
            Category parentCategory = trustWorthyParentCategory(labels.get(0), labels.get(1),
                    categorizationDistribution);
            if (parentCategory == null) {
                return Optional.empty();
            }

            double newPercentage = categorizationDistribution.get(labels.get(0).getLabel())
                    + categorizationDistribution.get(labels.get(1).getLabel());
            categorizationDistribution.remove(labels.get(0).getLabel());
            categorizationDistribution.remove(labels.get(1).getLabel());
            categorizationDistribution
                    .put(parentCategory.getDefaultChild(categories.get()).getCode(), newPercentage);
        }

        return Optional.of(categorizationDistribution);
    }

    private boolean lowConfidenceResults(FastTextLabel firstLabel, FastTextLabel secondLabel,
            Map<String, Double> categorizationDistribution) {
        if (categorizationDistribution.get(firstLabel.getLabel()) < categorizationConfiguration
                .getMinimumPercentage()) {
            return true;
        }

        if (categorizationDistribution.get(firstLabel.getLabel())
                - categorizationDistribution.get(secondLabel.getLabel())
                < categorizationConfiguration.getTopTwoPercentageDelta()) {
            return true;
        }

        return false;
    }

    private Category trustWorthyParentCategory(FastTextLabel firstLabel, FastTextLabel secondLabel,
            Map<String, Double> categorizationDistribution) {
        if (categorizationDistribution.get(firstLabel.getLabel()) + categorizationDistribution
                .get(secondLabel.getLabel()) >= categorizationConfiguration.getMinimumPercentage()) {
            return getCommonParent(firstLabel, secondLabel);
        }

        return null;
    }

    private Category getCommonParent(FastTextLabel firstLabel, FastTextLabel secondLabel) {
        try {
            Category firstCategory = categoriesByCode.get(firstLabel.getLabel());
            Category secondCategory = categoriesByCode.get(secondLabel.getLabel());
            Category parent = categoriesById.get(firstCategory.getParent());

            if (parent.getId().equals(categoriesById.get(secondCategory.getParent()).getId())) {
                return parent;
            }
        } catch (NullPointerException exception) {
            log.error(String.format("%s, %s did not have a category or a parent category", firstLabel.getLabel(),
                    secondLabel.getLabel()), exception);
        }

        return null;
    }

    @Override
    public Optional<Outcome> categorize(Transaction t) {
        return predict(preprocessor.apply(t.getDescription()))
                .map(d -> new Outcome(CategorizationCommand.GENERAL_EXPENSES,
                        new CategorizationVector(CategorizationWeight.DEFAULT_WEIGHT, d)));
    }
}
