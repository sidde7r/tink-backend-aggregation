package se.tink.backend.categorization.rules;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.mahout.classifier.naivebayes.AbstractNaiveBayesClassifier;
import org.apache.mahout.classifier.naivebayes.ComplementaryNaiveBayesClassifier;
import org.apache.mahout.classifier.naivebayes.NaiveBayesModel;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;
import se.tink.backend.categorization.CategorizationVector;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.categorization.lookup.CitiesByMarket;
import se.tink.backend.core.CategorizationWeight;
import se.tink.backend.core.Market;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.CategorizationUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.QuickIndex;
import se.tink.libraries.serialization.utils.SerializationUtils;


public class NaiveBayesCategorizationCommand implements Classifier {

    private final static LogUtils log = new LogUtils(NaiveBayesCategorizationCommand.class);

    // TODO: Inject prepopulated classifiers to reduce 1) runtime complexity and 2) reduce the runtime error surface.
    private final static LoadingCache<String, AbstractNaiveBayesClassifier> models = CacheBuilder.newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES).build(new CacheLoader<String, AbstractNaiveBayesClassifier>() {

                @Override
                public AbstractNaiveBayesClassifier load(String path) throws Exception {

                    // Unfortunately we can only read NaiveBayesModel from file system (and not from a stream), so we
                    // are temporarily putting those files on disk to read up the model.

                    TemporaryResourcesDirectory naiveBayesModel = TemporaryResourcesDirectory.create(
                            "naiveBayesModel",
                            new java.io.File(path, ".naiveBayesModel.bin.crc").getPath(),
                            new java.io.File(path, "naiveBayesModel.bin").getPath()
                    );
                    try {
                        NaiveBayesModel model = NaiveBayesModel
                                .materialize(new Path(naiveBayesModel.getFile().toURI()), CONF);
                        return new ComplementaryNaiveBayesClassifier(model);
                    } finally {
                        naiveBayesModel.close();
                    }
                }

            });

    private final static LoadingCache<String, QuickIndex> descriptionIndexCache = CacheBuilder.newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES).build(new CacheLoader<String, QuickIndex>() {

                @Override
                public QuickIndex load(String file) throws Exception {
                    InputStream stream = this.getClass().getResourceAsStream(file);
                    try {
                        return SerializationUtils.deserializeFromBinary(stream,
                                new TypeReference<QuickIndex>() {
                                });
                    } finally {
                        stream.close();
                    }
                }

            });

    // Model properties
    private final static Configuration CONF = new Configuration();
    private final static int N = 3;
    private final static float BOOST = 5f;
    private static final ImmutableSet<Market.Code> MARKETS = ImmutableSet.of(Market.Code.SE, Market.Code.NL);
    private final LabelIndexCache labelIndexCache;

    // Relative to the working directory.
    private final static String BASE_PATH = "/naivebayes/%s-%s/";

    private static final String DESCRIPTION_INDEX_DIRECTORY = "descriptionIndex";
    private static final String MODEL_DIRECTORY = "model";

    private final CitiesByMarket citiesByMarket;
    private final Provider provider;
    private final String modelPath;
    private final String labelIndexPath;
    private final String descriptionIndexPath;
    private final NaiveBayesCategorizationCommandType type;

    // Model
    private AbstractNaiveBayesClassifier classifier;
    private QuickIndex descriptionIndex;
    private Map<Integer, String> labelIndex;

    @VisibleForTesting
    /*package*/ NaiveBayesCategorizationCommand(
            LabelIndexCache labelIndexCache, CitiesByMarket citiesByMarket, NaiveBayesCategorizationCommandType type,
            Provider provider) {

        this.type = Preconditions.checkNotNull(type);
        this.labelIndexCache = Preconditions.checkNotNull(labelIndexCache);
        this.citiesByMarket = Preconditions.checkNotNull(citiesByMarket);
        this.provider = provider;

        final String market = constructMarketStringFromProvider(this.provider);
        final String basePath = constructBasePath(this.type.getDirectoryString(), market);

        descriptionIndexPath = basePath + DESCRIPTION_INDEX_DIRECTORY;
        labelIndexPath = basePath;
        modelPath = basePath + MODEL_DIRECTORY;

        try {
            loadModel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<NaiveBayesCategorizationCommand> build(
            LabelIndexCache labelIndexCache,
            CitiesByMarket citiesByMarket,
            NaiveBayesCategorizationCommandType type,
            Provider provider) {

        if (!provider.isUsingDemoAgent() && !MARKETS.contains(Market.Code.valueOf(provider.getMarket()))) {
            return Optional.empty();
        }

        NaiveBayesCategorizationCommand instance = new NaiveBayesCategorizationCommand(labelIndexCache,
                citiesByMarket, type, provider);
        return Optional.of(instance);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", type).toString();
    }

    @Override
    public Optional<Outcome> categorize(Transaction transaction) {
        if (!type.getPredicate().test(transaction)) {
            return Optional.empty();
        }

        String cleanDescription = getCleanDescription(provider, transaction);

        // Don't execute if the clean description is empty or only contains the default character.
        if (Strings
                .isNullOrEmpty(
                        cleanDescription.replaceAll(String.valueOf(CategorizationUtils.DEFAULT_CHARACTER), ""))) {
            return Optional.empty();
        }

        Vector features = CategorizationUtils.createFeatureVector(cleanDescription, N, BOOST, descriptionIndex);
        Vector results = classifier.classifyFull(features);

        Map<String, Double> categorization = Maps.newHashMap();

        double aPrioriProbability = 1 / (double) results.size();
        double totalScore = 0;

        for (Element element : results.all()) {
            totalScore += element.get();
        }

        for (Element element : results.all()) {
            double prob = element.get() / totalScore;
            if (prob > aPrioriProbability) {
                categorization.put(labelIndex.get(element.index()), element.get());
            }
        }

        Outcome result = new Outcome(
                type.getCommand(),
                new CategorizationVector(CategorizationWeight.DEFAULT_WEIGHT, categorization)
        );
        return Optional.of(result);
    }

    private String getCleanDescription(Provider provider, Transaction transaction) {

        String description = transaction.getDescription();

        // If the description is empty, or if it was modified by the user, use the original description instead.
        if (Strings.isNullOrEmpty(description) || transaction.isUserModifiedDescription()) {
            description = transaction.getOriginalDescription();
        }

        String cleanDescription = description;

        if (Market.Code.SE.name().equals(provider.getMarket())) {
            cleanDescription = CategorizationUtils.removeSwedishPersonalIdentityNumber(cleanDescription);
        }

        if (Market.Code.NL.name().equals(provider.getMarket())) {
            cleanDescription = CategorizationUtils.removeDutchCardNumber(cleanDescription);
        }

        cleanDescription = CategorizationUtils.clean(cleanDescription);
        cleanDescription = CategorizationUtils.trimNumbers(cleanDescription);

        return citiesByMarket.getForMarket(Market.Code.valueOf(provider.getMarket()))
                .map(cities -> (Function<String, String>) d -> {
                    d = cities.trim(d);
                    d = CategorizationUtils.trimNumbers(d);
                    d = CategorizationUtils.trim(d);
                    return d;
                })
                .orElse(Function.identity())
                .apply(cleanDescription);
    }

    private static String constructMarketStringFromProvider(Provider provider) {
        return provider.isUsingDemoAgent() ? "se" : provider.getMarket().toLowerCase();
    }

    private static String constructBasePath(String type, String market) {
        return String.format(BASE_PATH, type, market);
    }

    /**
     * Load model (from file)
     *
     * @throws IOException
     */
    private void loadModel() throws IOException {
        // Load Naive Bayes classifier, given a path to a model.
        try {
            classifier = models.get(modelPath);
        } catch (ExecutionException e) {
            throw new IOException(String.format("Could not initialize model and classifier from '%s'.", modelPath), e);
        }

        // Load label index (mapping to category codes)
        try {
            labelIndex = labelIndexCache.get(labelIndexPath);
        } catch (ExecutionException e) {
            throw new IOException(String.format("Could not initialize label index from  '%s'.", labelIndexPath), e);
        }

        // Load description index (mapping of unique cleaned descriptions in the training set)
        try {
            descriptionIndex = descriptionIndexCache.get(descriptionIndexPath);
        } catch (ExecutionException e) {
            throw new IOException(
                    String.format("Could not initialize description index from '%s'.", descriptionIndexPath), e);
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Model %s was successfully loaded.", descriptionIndexPath));
        }
    }

    public static List<NaiveBayesCategorizationCommand> buildAllTypes(LabelIndexCache labelIndexCache,
            CitiesByMarket citiesByMarket,
            Provider provider) {

        return Arrays.stream(NaiveBayesCategorizationCommandType.values())
                .map(type -> NaiveBayesCategorizationCommand.build(labelIndexCache, citiesByMarket, type, provider))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}

