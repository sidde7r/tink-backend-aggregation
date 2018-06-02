package se.tink.analytics.jobs.categorization.jobs;

import com.datastax.spark.connector.japi.SparkContextJavaFunctions;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import se.tink.analytics.AnalyticsContext;
import se.tink.analytics.entites.CategorizationTransaction;
import se.tink.analytics.jobs.categorization.entities.Coach;
import se.tink.analytics.jobs.categorization.entities.TransactionGroup;
import se.tink.analytics.jobs.categorization.utils.CoachesUtils;
import se.tink.analytics.jobs.categorization.utils.DescriptionUtils;
import se.tink.analytics.spark.filters.CassandraTransactionFilter;
import se.tink.analytics.spark.functions.TrainCategorizationModelSparkFunctions.Filters;
import se.tink.analytics.spark.functions.TrainCategorizationModelSparkFunctions.Mappers;
import se.tink.analytics.spark.functions.TrainCategorizationModelSparkFunctions.Reducers;
import se.tink.analytics.utils.CassandraUtil;
import se.tink.backend.core.CassandraTransaction;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Cities;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.LogUtils;

public class PrepareCategorizationModelJob {

    private static final String CASSANDRA_KEYSPACE = "tink"; // todo, shouldn't be hardcoded
    private final static String CASSANDRA_TRANSACTIONS_TABLE = "transactions";

    private final static String CITIES_PATH = "data/seeding/cities-%s.txt";

    private static final CSVFormat CSV_FORMAT = CSVFormat.newFormat(';').withRecordSeparator('\n').withQuote('"')
            .withEscape('\\').withNullString("NULL");

    private final static LogUtils log = new LogUtils(PrepareCategorizationModelJob.class);

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    private Map<String, Cities> citiesByMarket = Maps.newHashMap();

    private AnalyticsContext analyticsContext;

    public PrepareCategorizationModelJob(AnalyticsContext analyticsContext) {
        this.analyticsContext = analyticsContext;
    }

    public static void main(String[] args) throws Exception {

        TrainingContext trainingContext = parseArguments(args);

        log.info(String.format("Using context: %s", trainingContext));

        AnalyticsContext context = AnalyticsContext.build("train-categorization-model", trainingContext.propertyFile);

        context.start();
        try {
            new PrepareCategorizationModelJob(context).run(trainingContext);
        } finally {
            context.stop();
        }
    }

    private void loadCities(TrainingContext context) {

        for (String market : context.markets) {
            Cities cities = citiesByMarket.get(market);

            if (cities == null) {
                cities = new Cities();
            }

            if (cities.size() == 0) {
                cities.loadCities(String.format(CITIES_PATH, market));
            }

            citiesByMarket.put(market, cities);
        }
    }

    private boolean preprocess(final Map<String, Coach> coachByKey, TrainingContext context) throws SQLException {

        if (coachByKey == null || coachByKey.size() == 0) {
            return false;
        }

        if (Objects.equal(Sources.FILE, context.source)) {
            // From file

            if (coachByKey.size() > 1) {
                log.error("You can only have one coach when preprocessing from file.");
                return false;
            }

            final String inputFilename = context.input;

            preprocessFromFile(coachByKey.get(coachByKey.keySet().toArray()[0]), inputFilename);
            return true;
        } else if (Objects.equal(Sources.DATABASE, context.source)) {
            // From database

            final Map<String, String> marketByUserId = getMarketByUserId(context.markets);
            final Map<String, String> categoryCodeById = getCategoryCodeById();

            log.debug(String.format("Collected %s categories from database", categoryCodeById.size()));

            Set<CategoryTypes> categoryTypes = FluentIterable.from(context.categoryTypes).transform(
                    s -> CategoryTypes.valueOf(s.toUpperCase())).toSet();

            CassandraTransactionFilter filter = new CassandraTransactionFilter.Builder()
                    .setOwnedBy(Sets.newHashSet(marketByUserId.keySet()))
                    .withinPeriod(new Period(context.startDate, context.endDate))
                    .setCategoryTypes(categoryTypes)
                    .build();

            preprocessFromDatabase(coachByKey, marketByUserId, categoryCodeById, filter, context);
            return true;
        } else {
            // Invalid source
            log.error(String.format("Invalid source (%s). Use `database` (default) or `file`.", context.source));
            return false;
        }
    }

    private void preparePreprocessing(Map<String, Coach> coachByKey) {
        try {
            for (Coach c : coachByKey.values()) {
                c.preparePreprocessing();
            }
        } catch (IOException e) {
            log.error("Unable to prepare for preprocessing.", e);
        }
    }

    private void finalizePreprocessing(Map<String, Coach> coachByKey) {
        try {
            for (Coach c : coachByKey.values()) {
                c.finalizePreprocessing();
            }
        } catch (IOException e) {
            log.error("Unable to cleanup the preprocessing.", e);
        }
    }

    private int preprocess(Map<String, Coach> coachByKey, JavaRDD<TransactionGroup> transactions, int numPartitions) {

        int count = 0;

        log.info(String.format("Repartition to %s partitions", numPartitions));

        // Create a new RDD with more partitions. The reason for this is to get smaller partitions that will be
        // easier to load into memory. It did not work on the ABN cluster for some reason so we will not repartition
        // if numPartitions = 0. Also note that a repartition will move a lot of data around between nodes.
        if (numPartitions > 0) {
            transactions = transactions.repartition(numPartitions);
        }

        // The iterator will consume as much memory as the largest partition in the RDD. This is better to use compared
        // to .collect() since a collect will collect data from all partitions and store in memory on the driver.
        Iterator<TransactionGroup> iterator = transactions.toLocalIterator();

        while (iterator.hasNext()) {

            TransactionGroup ct = iterator.next();

            for (CategorizationTransaction transaction : ct.toTransactionList()) {

                try {

                    String key = Coach.createKey(transaction.getMarket(), transaction.getCategoryType());
                    Coach coach = coachByKey.get(key);
                    if (coach == null) {
                        log.warn(String.format("Unable to find coach for key %s.", key));
                        continue;
                    }

                    coach.preprocess(transaction.getDescription(), transaction.getCategoryCode());

                    count++;
                } catch (Exception e) {
                    log.warn("Unable to preprocess transaction.", e);
                }
            }
        }

        return count;
    }

    private void preprocessFromDatabase(final Map<String, Coach> coachByKey, final Map<String, String> marketByUserId,
            final Map<String, String> categoryCodeById, CassandraTransactionFilter filter, TrainingContext context) {

        log.info("# PRE-PROCESS (from database)");

        Stopwatch stopwatch = Stopwatch.createStarted();

        int count = 0;

        JavaSparkContext sparkContext = analyticsContext.getSparkContext();

        Broadcast<Map<String, String>> marketByUserIdBroadcast = sparkContext.broadcast(marketByUserId);
        Broadcast<Map<String, String>> categoryCodeByIdBroadcast = sparkContext.broadcast(categoryCodeById);
        Broadcast<Map<String, Cities>> citiesByMarketBroadcast = sparkContext.broadcast(citiesByMarket);

        // Retrieve all transactions.
        JavaRDD<CassandraTransaction> cassandraTransactions = getCassandraTransactions();

        // Filter transactions with default filter
        cassandraTransactions = cassandraTransactions.filter(filter);

        // Map and clean the description on all transactions
        JavaRDD<CategorizationTransaction> transactions = cassandraTransactions.map(Mappers
                .toCategorizationTransaction(marketByUserIdBroadcast, categoryCodeByIdBroadcast,
                        citiesByMarketBroadcast));

        // Group transactions by description
        JavaPairRDD<String, TransactionGroup> groupedByDescription = transactions.mapToPair(Mappers.MAP_BY_DESCRIPTION);

        // Merge by key and count occurrences by category code and user id
        groupedByDescription = groupedByDescription.reduceByKey(Reducers.MERGE_TRANSACTION_GROUP);

        // Filter by minimum occurrences
        groupedByDescription = groupedByDescription.filter(Filters.withMinOccurrences(context.minOccurrences));

        // Filter by minimum users
        groupedByDescription = groupedByDescription.filter(Filters.withMinUsers(context.minUsers));

        JavaRDD<TransactionGroup> result = groupedByDescription.values();

        preparePreprocessing(coachByKey);
        count += preprocess(coachByKey, result, context.numPartitions);
        finalizePreprocessing(coachByKey);

        log.info(String.format("Preprocessing took %ss", stopwatch.elapsed(TimeUnit.SECONDS)));
        log.info(String.format("\tProcessed %s qualified transactions", count));

    }

    private JavaRDD<CassandraTransaction> getCassandraTransactions() {

        SparkContextJavaFunctions javaFunctions = analyticsContext.getJavaFunc();

        CassandraUtil<CassandraTransaction> util = new CassandraUtil<>(CASSANDRA_KEYSPACE, CassandraTransaction.class);

        return util.read(javaFunctions, CASSANDRA_TRANSACTIONS_TABLE, CassandraTransaction.getColumnMap());
    }

    private void preprocessFromFile(final Coach coach, String fileName) {

        log.info("# PRE-PROCESS (from file)");

        Stopwatch stopwatch = Stopwatch.createStarted();

        int count = 0;

        try {
            coach.preparePreprocessing();

            count = Files.readLines(new File(fileName),
                    Charsets.UTF_8, new LineProcessor<Integer>() {

                        int count = 0;
                        int cleaned = 0;

                        @Override
                        public Integer getResult() {
                            return count;
                        }

                        @Override
                        public boolean processLine(String line) throws IOException {

                            try {
                                CSVParser parser = CSVParser.parse(line, CSV_FORMAT);
                                CSVRecord r = parser.getRecords().get(0);

                                String categoryCode = r.get(0);
                                String description = r.get(1);

                                if (categoryCode.startsWith(coach.getCategoryType())) {

                                    String cleanDescription = DescriptionUtils
                                            .cleanDescription(description, citiesByMarket, coach.getMarket());

                                    if (cleanDescription.length() < 30) {

                                        if (!description.toLowerCase().equals(cleanDescription)) {
                                            cleaned++;
                                            log.debug(String.format("Cleaned (%s):\t\"%s\" => \"%s\"", cleaned,
                                                    description.toLowerCase(), cleanDescription));
                                        }

                                        coach.preprocess(cleanDescription, categoryCode);
                                        count++;
                                        if (count % 1000 == 0) {
                                            log.info(String.format("Preprocessed %d lines", count));
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                log.error(String.format("Unable to process line: [%s]", line));
                                throw e;
                            }

                            return true;
                        }
                    });

            coach.finalizePreprocessing();
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info(String.format("Preprocessing took %ss", stopwatch.elapsed(TimeUnit.SECONDS)));
        log.info(String.format("\tProcessed %s transactions", count));
    }

    protected void run(TrainingContext context) throws Exception {

        log.info("Starting pre process!");

        Stopwatch stopwatch = Stopwatch.createStarted();

        final Map<String, Coach> coachByKey = CoachesUtils.getCoaches(context.markets, context.categoryTypes,
                context.unhandledCategories, context.workingDirectory);

        loadCities(context);

        boolean success = preprocess(coachByKey, context);

        if (!success) {
            log.error("Could not pre process categorization data");
        }

        log.info(String.format("Pre process took %ss", stopwatch.elapsed(TimeUnit.SECONDS)));
        log.info("Pre process completed!");
    }

    private Map<String, String> getMarketByUserId(final Set<String> markets) throws SQLException {

        ImmutableMap.Builder<String, String> marketByUserIdMap = ImmutableMap.builder();

        try (Statement stmt = analyticsContext.getMysqlConnection().createStatement()) {
            if (stmt.execute("SELECT id, profile_market  FROM users")) {
                try (ResultSet resultset = stmt.getResultSet()) {
                    while (resultset.next()) {

                        String userId = resultset.getString(1);
                        String market = resultset.getString(2).toLowerCase();

                        if (markets.contains(market)) {
                            marketByUserIdMap.put(userId, market);
                        }
                    }
                }
            }
        }

        return marketByUserIdMap.build();
    }

    private Map<String, String> getCategoryCodeById() throws SQLException {

        ImmutableMap.Builder<String, String> categoryMap = ImmutableMap.builder();

        try (Statement stmt = analyticsContext.getMysqlConnection().createStatement()) {
            if (stmt.execute("SELECT id, code  FROM categories")) {
                try (ResultSet resultset = stmt.getResultSet()) {
                    while (resultset.next()) {

                        String id = resultset.getString(1);
                        String code = resultset.getString(2);

                        categoryMap.put(id, code);
                    }
                }
            }
        }

        return categoryMap.build();
    }

    private static class Sources {
        final static String DATABASE = "database";
        final static String FILE = "file";
    }

    private static class TrainingContext {

        static final String DEFAULT_INPUT_FILENAME = "data/seeding/expenses-training.csv";
        static final String DEFAULT_SOURCE = Sources.DATABASE;
        static final int DEFAULT_MIN_OCCURRENCES = 5;
        static final int DEFAULT_MIN_USERS = 2;
        static final int DEFAULT_PARTITIONS = 0;

        public String propertyFile;
        public String source;
        public String input;
        public ImmutableSet<String> markets;
        public ImmutableSet<String> categoryTypes;
        public ImmutableSet<String> unhandledCategories;
        public Date startDate;
        public Date endDate;
        public int minOccurrences;
        public int minUsers;
        public int numPartitions;
        public String workingDirectory;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this.getClass())
                    .add("propertyFile", propertyFile)
                    .add("source", source)
                    .add("input", input)
                    .add("startDate", startDate)
                    .add("endDate", endDate)
                    .add("markets", markets)
                    .add("categoryTypes", categoryTypes)
                    .add("unhandledCategories", unhandledCategories)
                    .add("minOccurrences", minOccurrences)
                    .add("minUsers", minUsers)
                    .add("workingDirectory", workingDirectory)
                    .toString();
        }
    }

    @SuppressWarnings("AccessStaticViaInstance")
    private static TrainingContext parseArguments(String args[]) throws ParseException {

        final String source = "source";
        final String input = "input";
        final String startDate = "startDate";
        final String endDate = "endDate";
        final String market = "market";
        final String markets = "markets";
        final String categoryType = "categoryType";
        final String categoryTypes = "categoryTypes";
        final String occurrences = "occurrences";
        final String users = "users";
        final String workingDirectory = "workingDirectory";
        final String numPartitions = "numPartitions";
        final String unhandledCategory = "unhandledCategory";
        final String unhandledCategories = "unhandledCategories";

        Options options = new Options();

        options.addOption("s", source, true, "Source (file, database or preprocessed).");
        options.addOption("i", input, true, "Input file to process. Only applicable if source=file.");
        options.addOption("d", startDate, true, "Start date. Only applicable if source=database.");
        options.addOption("e", endDate, true, "End date. Only applicable if source=database.");
        options.addOption("o", occurrences, true, "Minimum occurrences.");
        options.addOption("u", users, true, "Minimum user count.");
        options.addOption("n", numPartitions, true, "Number of partitions that should be used before collecting rdd:s");
        options.addOption("w", workingDirectory, true, "Working directory");

        OptionGroup marketOptionGroup = new OptionGroup();
        marketOptionGroup.addOption(OptionBuilder.withLongOpt(market).hasArg().create(market));
        marketOptionGroup.addOption(OptionBuilder.withLongOpt(markets).hasArgs().create(markets));
        marketOptionGroup.setRequired(true);

        OptionGroup categoryOptionGroup = new OptionGroup();
        categoryOptionGroup.addOption(OptionBuilder.withLongOpt(categoryType).hasArg().create(categoryType));
        categoryOptionGroup.addOption(OptionBuilder.withLongOpt(categoryTypes).hasArg().create(categoryTypes));
        categoryOptionGroup.setRequired(true);

        OptionGroup unhandledCategoriesOptionGroup = new OptionGroup();
        unhandledCategoriesOptionGroup
                .addOption(OptionBuilder.withLongOpt(unhandledCategory).hasArg().create(unhandledCategory));
        unhandledCategoriesOptionGroup
                .addOption(OptionBuilder.withLongOpt(unhandledCategories).hasArg().create(unhandledCategories));
        unhandledCategoriesOptionGroup.setRequired(true);

        options.addOptionGroup(marketOptionGroup);
        options.addOptionGroup(categoryOptionGroup);
        options.addOptionGroup(unhandledCategoriesOptionGroup);

        CommandLine line = new BasicParser().parse(options, args, true);

        TrainingContext context = new TrainingContext();

        String inputMarkets = line.getOptionValue(marketOptionGroup.getSelected()).toLowerCase();
        String inputCategoryTypes = line.getOptionValue(categoryOptionGroup.getSelected()).toLowerCase();
        String inputUnhandledCategories = line.getOptionValue(unhandledCategoriesOptionGroup.getSelected())
                .toLowerCase();

        context.propertyFile = line.getArgs()[0];
        context.source = line.getOptionValue(source, TrainingContext.DEFAULT_SOURCE);
        context.markets = ImmutableSet.copyOf(COMMA_SPLITTER.splitToList(inputMarkets));
        context.categoryTypes = ImmutableSet.copyOf(COMMA_SPLITTER.splitToList(inputCategoryTypes));
        context.unhandledCategories = ImmutableSet.copyOf(COMMA_SPLITTER.splitToList(inputUnhandledCategories));
        context.workingDirectory = line.getOptionValue(workingDirectory);

        if (Objects.equal(Sources.FILE, context.source)) {
            context.input = line.getOptionValue(input, TrainingContext.DEFAULT_INPUT_FILENAME);

        } else if (Objects.equal(Sources.DATABASE, context.source)) {
            String startDateStr = line.getOptionValue(startDate, null);
            String endDateStr = line.getOptionValue(endDate, null);

            if (Strings.isNullOrEmpty(endDateStr)) {
                context.endDate = DateUtils.getToday();
                log.info(String.format("No end date supplied (`endDate` argument). Using today (%s).",
                        context.endDate));
            } else {
                context.endDate = DateUtils.parseDate(endDateStr);
            }

            if (Strings.isNullOrEmpty(startDateStr)) {
                Calendar startDateCalendar = DateUtils.getCalendar(context.endDate);
                startDateCalendar.add(Calendar.YEAR, -1);
                context.startDate = startDateCalendar.getTime();
                log.info(String.format(
                        "No start date supplied (`startDate` argument). Using one year before end date (%s).",
                        context.startDate));
            } else {
                context.startDate = DateUtils.parseDate(startDateStr);
            }
        }

        context.minOccurrences = getOptionInteger(line, occurrences, TrainingContext.DEFAULT_MIN_OCCURRENCES);
        context.minUsers = getOptionInteger(line, users, TrainingContext.DEFAULT_MIN_USERS);
        context.numPartitions = getOptionInteger(line, numPartitions, TrainingContext.DEFAULT_PARTITIONS);

        return context;
    }

    private static int getOptionInteger(CommandLine line, String key, int defaultValue) {
        String tmp = line.getOptionValue(key, null);

        if (Strings.isNullOrEmpty(tmp)) {
            return defaultValue;
        }

        return Integer.parseInt(tmp);
    }
}
