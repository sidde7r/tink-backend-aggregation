package se.tink.backend.categorization.rules;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.categorization.CategorizationVector;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.utils.FunctionUtils;
import se.tink.backend.common.utils.TransactionUtils;
import se.tink.backend.core.CategorizationCommand;
import se.tink.backend.core.CategorizationWeight;
import se.tink.backend.core.Category;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Transaction;

public class FastTextClassifier implements Classifier {
    private static final Logger log = LoggerFactory.getLogger(FastTextClassifier.class);

    private static final int LABEL_TEXT_LENGTH = 9;
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("[\\r\\n]+");
    private static final Splitter SPACE_SPLITTER = Splitter.on(" ").trimResults();

    private final CategoryConfiguration categoryConfiguration;
    private final CategorizationConfiguration categorizationConfiguration;
    private final ClusterCategories categories;
    private final ImmutableMap<String, Category> categoriesById;
    private final ImmutableMap<String, Category> categoriesByCode;
    private final File model;
    private final int topLabels;
    private final File executable;
    private final Function<String, String> preprocessor;

    private Process fastTextProcess;
    private OutputStream processInput;
    private BufferedReader processOutputReader;

    public FastTextClassifier(File executable, File model, CategoryConfiguration categoryConfiguration,
            CategorizationConfiguration categorizationConfiguration,
            int topLabels, ClusterCategories categories, List<Function<String, String>> preprocessors) {
        this.executable = executable;
        this.model = model;
        this.categoryConfiguration = categoryConfiguration;
        this.categorizationConfiguration = categorizationConfiguration;
        this.categories = categories;
        this.categoriesById = Maps.uniqueIndex(categories.get(), Category::getId);
        this.categoriesByCode = Maps.uniqueIndex(categories.get(), Category::getCode);
        this.topLabels = topLabels;
        this.preprocessor = new FunctionUtils<String>().compose(preprocessors);
    }

    public synchronized Optional<Map<String, Double>> predict(String transactionText) {
        String predictedDistribution;

        try {
            predictedDistribution = predictFromFasttext(transactionText);
        } catch (IOException predictionException) {
            log.error("fastText process crashed, restarting", predictionException);

            try {
                processInput.close();
            } catch (IOException inputCloseException) {
                log.error("Could not close fastText process input", inputCloseException);
            }
            try {
                processOutputReader.close();
            } catch (IOException outputCloseException) {
                log.error("Could not close fastText output reader", outputCloseException);
            }

            start();

            try {
                predictedDistribution = predictFromFasttext(transactionText);
            } catch (IOException secondPredictionException) {
                throw new RuntimeException(secondPredictionException);
            }
        }

        return predictionToDistributionMap(transactionText, predictedDistribution);
    }

    private String predictFromFasttext(String transactionText) throws IOException {
        String predictedDistribution;

        processInput
                .write(NEWLINE_PATTERN.matcher(transactionText).replaceAll(" ").getBytes(StandardCharsets.UTF_8));
        processInput.write("\n".getBytes());
        processInput.flush();
        predictedDistribution = processOutputReader.readLine();

        return predictedDistribution;
    }

    private Optional<Map<String, Double>> predictionToDistributionMap(String transactionText,
            String predictedDistribution) {
        Preconditions.checkNotNull(predictedDistribution);

        List<String> categoryAndProbabilities = Lists.newArrayList(SPACE_SPLITTER.split(predictedDistribution));
        Map<String, Double> categorizationDistribution = new HashMap<>();
        if (categorizationConfiguration.usePatchedFastText()) {
            Boolean seenWordBefore = Boolean.parseBoolean(categoryAndProbabilities.remove(0));
            if (!seenWordBefore) {
                return Optional.empty();
            }
        }

        List<String> topLabels = new ArrayList<>(categoryAndProbabilities.size() / 2);
        for (int i = 0; i < categoryAndProbabilities.size(); i += 2) {
            topLabels.add(categoryAndProbabilities.get(i).substring(LABEL_TEXT_LENGTH));
            categorizationDistribution
                    .put(categoryAndProbabilities.get(i).substring(LABEL_TEXT_LENGTH),
                            Double.parseDouble(categoryAndProbabilities.get(i + 1)));
        }

        // Do we trust fastText?
        if (shouldBeUncategorized(transactionText, topLabels)) {
            return Optional.empty();
        }

        if (untrustworthyResults(topLabels, categorizationDistribution)) {
            Category parentCategory = trustWorthyParentCategory(topLabels, categorizationDistribution);
            if (parentCategory != null) {
                double newPercentage = categorizationDistribution.get(topLabels.get(0)) + categorizationDistribution
                        .get(topLabels.get(1));
                categorizationDistribution.remove(topLabels.get(0));
                categorizationDistribution.remove(topLabels.get(1));
                categorizationDistribution
                        .put(parentCategory.getDefaultChild(categories.get()).getCode(), newPercentage);

            } else {
                return Optional.empty();
            }
        }

        return Optional.of(categorizationDistribution);
    }

    private boolean untrustworthyResults(List<String> topLabels, Map<String, Double> categorizationDistribution) {
        if (topLabels.size() > 2) {
            if (categorizationDistribution.get(topLabels.get(0)) < categorizationConfiguration.getMinimumPercentage()) {
                return true;
            } else {
                if (categorizationDistribution.get(topLabels.get(0)) - categorizationDistribution.get(topLabels.get(1))
                        < categorizationConfiguration.getTopTwoPercentageDelta()) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean shouldBeUncategorized(String transactionText, List<String> topLabels) {
        if (TransactionUtils.isSwish(transactionText)) {
            return true;
        } else if (topLabels != null && topLabels.size() > 0) {
            if (categoryConfiguration.getTransferUnknownCode().equals(topLabels.get(0)) ||
                    categoryConfiguration.getExpenseUnknownCode().equals(topLabels.get(0))) {
                return true;
            }
        }

        return false;
    }

    private Category trustWorthyParentCategory(List<String> topLabels, Map<String, Double> categorizationDistribution) {
        if (topLabels.size() > 2) {
            if (categorizationDistribution.get(topLabels.get(0)) + categorizationDistribution.get(topLabels.get(1))
                    >= categorizationConfiguration.getMinimumPercentage()) {
                return getCommonParent(topLabels.subList(0, 1));
            }
        }

        return null;
    }

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

    @Override
    public Optional<Outcome> categorize(Transaction transaction) {
        return predict(preprocessor.apply(transaction.getDescription()))
                .map(d -> new Outcome(CategorizationCommand.GENERAL_EXPENSES,
                        new CategorizationVector(CategorizationWeight.DEFAULT_WEIGHT, d)));
    }

    // Start using ProcessBuilder
    @PostConstruct
    public void start() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    executable.getAbsolutePath(),
                    "predict-prob",
                    model.getAbsolutePath(),
                    "-",
                    Integer.toString(topLabels)
            );
            fastTextProcess = processBuilder.start();
            processInput = fastTextProcess.getOutputStream();
            InputStream processOutput = fastTextProcess.getInputStream();
            processOutputReader = new BufferedReader(new InputStreamReader(processOutput, StandardCharsets.UTF_8));
        } catch (IOException fastTextStartException) {
            // Could not start process
            if (fastTextProcess != null && fastTextProcess.isAlive()) {
                fastTextProcess.destroy();
            }

            try {
                if (processInput != null) {
                    processInput.close();
                }
            } catch (IOException inputCloseException) {
                fastTextStartException.addSuppressed(inputCloseException);
            }
            try {
                if (processOutputReader != null) {
                    processOutputReader.close();
                }
            } catch (IOException outputCloseException) {
                fastTextStartException.addSuppressed(outputCloseException);
            }

            if (!executable.delete()) {
                log.warn("Can't delete fastText executable after start failure: {}",
                        executable.getAbsolutePath());
            }

            throw new RuntimeException(fastTextStartException);
        }
    }

    @PreDestroy
    public void stop() {
        if (fastTextProcess != null && fastTextProcess.isAlive()) {
            fastTextProcess.destroy();
        }

        try {
            processInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            processOutputReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
