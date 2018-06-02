package se.tink.analytics.jobs;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import scala.Tuple2;
import se.tink.analytics.AnalyticsContext;
import se.tink.analytics.spark.filters.CassandraTransactionFilter;
import se.tink.analytics.spark.functions.SeedDescriptionExtrapolationCorpusSparkFunctions;
import se.tink.analytics.spark.functions.SparkFunctions;
import se.tink.analytics.utils.CassandraUtil;
import se.tink.backend.core.CassandraTransaction;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringExtrapolator;

public class SeedDescriptionExtrapolationCorpusJob {

    private final static String CASSANDRA_KEYSPACE = "tink"; // todo, shouldn't be hardcoded
    private final static String CASSANDRA_TRANSACTIONS_TABLE = "transactions";
    private final static String DEFAULT_INPUT_FILENAME = "data/analytics-jobs/categorization/extrapolation-corpus-raw.txt";
    private final static String DEFAULT_OUTPUT_FILENAME = "data/analytics-jobs/categorization/extrapolation-corpus-greedy.txt";

    private final static LogUtils log = new LogUtils(SeedDescriptionExtrapolationCorpusJob.class);

    private AnalyticsContext analyticsContext;

    private final static String OPTION_SOURCE = "source";
    private final static String OPTION_INPUT = "input";
    private final static String OPTION_OUTPUT = "output";

    public SeedDescriptionExtrapolationCorpusJob(AnalyticsContext analyticsContext) {
        this.analyticsContext = analyticsContext;
    }

    public static void main(String[] args) throws Exception {

        CommandLineParser parser = new BasicParser();

        CommandLine line = parser.parse(constructBasicParserOptions(), args);

        String source = line.getOptionValue(OPTION_SOURCE, "file");
        String inputFile = line.getOptionValue(OPTION_INPUT, DEFAULT_INPUT_FILENAME);
        String outputFile = line.getOptionValue(OPTION_OUTPUT, DEFAULT_OUTPUT_FILENAME);

        AnalyticsContext context = AnalyticsContext.build("seed-description-extrapolation-corpus", line.getArgs()[0]);

        context.start();
        try {
            new SeedDescriptionExtrapolationCorpusJob(context).run(source, inputFile, outputFile);
        } finally {
            context.stop();
        }
    }

    protected void run(String source, String inputFilename, String outputFilename) throws Exception {

        log.info(String.format("Seed description extrapolation corpus from %s.", source));

        Stopwatch watch = Stopwatch.createStarted();

        StringExtrapolator extrapolator = null;

        if ("file".equals(source)) {
            // From file (either a loose, unfiltered corpus; or, just a list of raw transaction descriptions).

            extrapolator = fromFile(inputFilename);
        } else if ("database".equals(source)) {
            // From database
            extrapolator = fromDatabase();
        } else {
            // Invalid source

            log.error(String.format("Invalid source (%s). Use `file` or `database`.", source));
        }

        if (extrapolator != null) {
            createGreedyCorpus(extrapolator, outputFilename);
        }

        watch.stop();
        log.info(String.format("Done! (%s)", watch.toString()));
    }

    /**
     * Create string extrapolator with corpus from database.
     */
    private StringExtrapolator fromDatabase() {

        final int minDescriptionLength = 10;
        final int minUserCount = 4;

        try {
            StringExtrapolator extrapolator = new StringExtrapolator();

            // Retrieve all transactions.
            CassandraUtil<CassandraTransaction> util = new CassandraUtil<>(CASSANDRA_KEYSPACE,
                    CassandraTransaction.class);

            JavaRDD<CassandraTransaction> transactions = util
                    .read(analyticsContext.getJavaFunc(), CASSANDRA_TRANSACTIONS_TABLE,
                            CassandraTransaction.getColumnMap());

            // Filter for retrieving credit card expenses.
            CassandraTransactionFilter creditCardExpensesFilter = new CassandraTransactionFilter.Builder()
                    .setCategoryType(CategoryTypes.EXPENSES)
                    .setType(TransactionTypes.CREDIT_CARD).build();

            // Credit card expenses with a long enough description.
            JavaRDD<CassandraTransaction> filteredTransactions = transactions
                    .filter(creditCardExpensesFilter)
                    .filter(SeedDescriptionExtrapolationCorpusSparkFunctions.Filters.minDescriptionLength(
                            minDescriptionLength));

            // Distinct transaction descriptions by user (i.e. one description occurrence per user).
            JavaPairRDD<UUID, String> distinctDescriptionsByUser = filteredTransactions
                    .mapToPair(SeedDescriptionExtrapolationCorpusSparkFunctions.Mappers.DESCRIPTION_BY_USER)
                    .distinct();

            // Number of users for each distinct transaction description.
            JavaPairRDD<String, Integer> distinctUserCountByDescription = distinctDescriptionsByUser
                    .mapToPair(SeedDescriptionExtrapolationCorpusSparkFunctions.Mappers.PREPARE_FOR_COUNT)
                    .reduceByKey(SeedDescriptionExtrapolationCorpusSparkFunctions.Reducers.SUM_INTEGERS);

            // Filter distinct descriptions by user count.
            JavaPairRDD<String, Integer> filteredDistinctDescriptions = distinctUserCountByDescription
                    .filter(SparkFunctions.Filters.<String, Integer>valueGreaterThanOrEqualTo(minUserCount));

            // Add descriptions to extrapolator.
            for (Tuple2<String, Integer> x : filteredDistinctDescriptions.sortByKey().collect()) {
                extrapolator.add(x._1());
            }

            return extrapolator;

        } catch (Exception e) {
            log.error("Unable to process transactions.", e);
            return null;
        }
    }

    /**
     * Create a greedy corpus from the supplied extrapolator and save it to file.
     *
     * @param extrapolator
     * @param outputFilename
     * @throws IOException
     */
    private void createGreedyCorpus(StringExtrapolator extrapolator, String outputFilename) throws IOException {
        Set<String> corpus = extrapolator.getCorpus();
        Set<String> greedy = Sets.newLinkedHashSet();

        log.info(String.format("Created corpus with %s descriptions.", corpus.size()));

        for (String phrase : corpus) {
            greedy.add(extrapolator.extrapolate(phrase, true));
        }

        log.info(String.format("Extrapolated phrases into %s greedy extrapolations.", greedy.size()));

        BufferedWriter output = null;

        try {
            output = new BufferedWriter(new OutputStreamWriter(Files.asByteSink(
                    new File(outputFilename)).openStream(), Charsets.UTF_8));

            for (String phrase : greedy) {
                output.write(phrase);
                output.newLine();
            }

            log.info(String.format("Successfully wrote greedy corpus to file (%s).", outputFilename));
        } catch (Exception e) {
            log.error(String.format("Unable to write corpus to file (%s).", outputFilename), e);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    /**
     * Create string extrapolator with corpus from file.
     *
     * @param filename
     * @return
     */
    private StringExtrapolator fromFile(String filename) {
        try {
            return StringExtrapolator.load(filename, input -> CharMatcher.anyOf(",\" ").trimTrailingFrom(
                    CharMatcher.anyOf("\" ").trimLeadingFrom(
                            input.toLowerCase().replaceAll("[ \t]*[,]*[ \t]+", " "))));
        } catch (IOException e) {
            log.error(String.format("Unable to load corpus from file (%s).", filename), e);
            return null;
        }
    }

    private static Options constructBasicParserOptions() {
        final Options options = new Options();
        options.addOption("s", OPTION_SOURCE, true, "source (file or database)");
        options.addOption("i", OPTION_INPUT, true, "input file name");
        options.addOption("o", OPTION_OUTPUT, true, "output file name");
        return options;
    }
}
