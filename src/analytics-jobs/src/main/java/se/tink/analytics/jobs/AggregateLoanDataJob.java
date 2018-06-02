package se.tink.analytics.jobs;

import com.datastax.spark.connector.japi.SparkContextJavaFunctions;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import se.tink.analytics.AnalyticsContext;
import se.tink.analytics.jobs.loandata.AggregatedAreaLoanDataApplier;
import se.tink.analytics.jobs.loandata.AggregatedAreaLoanDataExtended;
import se.tink.analytics.jobs.loandata.FilterByProvider;
import se.tink.analytics.jobs.loandata.LoanData;
import se.tink.analytics.jobs.loandata.LoanDataFilter;
import se.tink.analytics.jobs.loandata.LoanDataToAggregatedAreaLoanData;
import se.tink.analytics.jobs.loandata.LoansToAccount;
import se.tink.analytics.jobs.loandata.UserCoordinates;
import se.tink.analytics.jobs.loandata.UserCoordinatesFilter;
import se.tink.analytics.jobs.loandata.UserCoordinatesIdsToTuple;
import se.tink.analytics.jobs.loandata.UserDemographics;
import se.tink.analytics.spark.functions.SparkFunctions;
import se.tink.analytics.utils.CassandraUtil;
import se.tink.backend.core.Market;
import se.tink.backend.core.interests.AggregatedAreaLoanData;
import se.tink.backend.utils.LogUtils;


public class AggregateLoanDataJob implements Serializable {

    private static final LogUtils log = new LogUtils(AggregateLoanDataJob.class);
    private static final String CASSANDRA_KEYSPACE = "tink";
    private static final String TABLE_LOAN_DATA = "loan_data";
    private static final String TABLE_USERS_DEMOGRAPHICS = "users_demographics";
    private static final String TABLE_USERS_COORDINATES = "users_coordinates";
    private static final String TABLE_AGGREGATED_AREA_LOAN = "aggregated_loans_by_area";

    private static final ImmutableSet<String> PROVIDERS = ImmutableSet.of(
            "danskebank",
            "danskebank-bankid",
            "seb-bankid",
            "nordea-bankid",
            "handelsbanken",
            "handelsbanken-bankid",
            "savingsbank-bankid",
            "swedbank-bankid",
            "lansforsakringar-bankid",
            "lansforsakringar",
            "sbab-bankid"
    );

    private Market.Code marketCode;
    private AnalyticsContext analyticsContext;

    public AggregateLoanDataJob(AnalyticsContext analyticsContext) {
        this.analyticsContext = analyticsContext;

        marketCode = Market.Code.SE;
    }

    public static void main(String[] args) throws Exception {

        Preconditions.checkArgument(args.length > 0, "Configuration file must be provided");

        AnalyticsContext context = AnalyticsContext.build("AGGREGATE_LOAN_DATA_JOB", args[0]);

        context.start();
        try {
            new AggregateLoanDataJob(context).run();
        } finally {
            context.stop();
        }
    }

    private void run() {
        log.info("Aggregating Loan Data per Postal Code for Market:" + marketCode);

        Stopwatch watch = Stopwatch.createStarted();

        JavaSparkContext sparkContext = analyticsContext.getSparkContext();

        // Read up user demographics
        JavaRDD<UserDemographics> userDemographics = getUserDemographics(analyticsContext.getJavaFunc());

        // Filter out only for correct market and where we have postal code
        JavaRDD<UUID> users = userDemographics
                .filter(SparkFunctions.Filters.getByMarket(marketCode.toString()))
                .map(SparkFunctions.Mappers.USER_DEMOGRAPHICS_TO_USERID);

        // Collect and create lookup set of userid
        Broadcast<HashSet<UUID>> broadcastUsers = sparkContext.broadcast(Sets.newHashSet(users.collect()));

        Map<UUID, UUID> areaIdByUserId = getUserCoordinates(analyticsContext.getJavaFunc())
                .filter(new UserCoordinatesFilter(broadcastUsers))
                .mapToPair(new UserCoordinatesIdsToTuple())
                .collectAsMap();

        Broadcast<Map<UUID, UUID>> broadcastAreaIdByUserId = sparkContext.broadcast(areaIdByUserId);

        log.info("Initialize phase took " + watch.elapsed(TimeUnit.MILLISECONDS) + "ms.");

        // Read up loans
        JavaRDD<LoanData> loans = getLoans(analyticsContext.getJavaFunc());

        // Group by account and get first
        JavaPairRDD<UUID, Iterable<LoanData>> loansByAccount = loans.groupBy(new LoansToAccount());
        JavaPairRDD<UUID, LoanData> snapshotByAccount = loansByAccount.mapValues(SparkFunctions.Mappers.GET_FIRST);

        // Filter by users with area
        JavaRDD<LoanData> snapshots = snapshotByAccount.values()
                .filter(new FilterByProvider(PROVIDERS))
                .filter(new LoanDataFilter(broadcastAreaIdByUserId));

        // Turn into AggregatedAreaLoanData
        JavaRDD<AggregatedAreaLoanDataExtended> snapshotsAsAggregated = snapshots.flatMap(
                new LoanDataToAggregatedAreaLoanData(broadcastAreaIdByUserId));

        // Key on area and bank
        JavaPairRDD<String, AggregatedAreaLoanDataExtended> dataByAreaAndBank = snapshotsAsAggregated.keyBy(
                SparkFunctions.Mappers.AGGREGATED_LOAN_BY_AREA_AND_BANK);
        JavaPairRDD<String, String> userIdByAreaAndBank = dataByAreaAndBank.mapValues(
                SparkFunctions.Mappers.AGGREGATED_AREA_LOAN_TO_USERID);

        // Count by area and bank
        Map<String, Object> loanCountByAreaAndBank = Maps.newHashMap(dataByAreaAndBank.countByKey());
        Map<String, Object> uniqueUserCountByAreaAndBank = Maps.newHashMap(userIdByAreaAndBank.distinct().countByKey());
        Broadcast<Map<String, Object>> broadcastLoanCount = sparkContext.broadcast(loanCountByAreaAndBank);
        Broadcast<Map<String, Object>> broadcastUniqueUserCount = sparkContext.broadcast(uniqueUserCountByAreaAndBank);

        // Reduce to sums
        JavaRDD<AggregatedAreaLoanDataExtended> reduced = dataByAreaAndBank.reduceByKey(SparkFunctions.Reducers.AGGREGATED_AREA_LOAN_SUM).values();

        // Fill out the rest of the data
        JavaRDD<AggregatedAreaLoanData> filled = reduced.map(new AggregatedAreaLoanDataApplier(broadcastLoanCount, broadcastUniqueUserCount));

        log.info("Ready to store data!");

        saveToCassandra(filled);

        log.info("Full job took " + watch.stop().elapsed(TimeUnit.MILLISECONDS) + "ms.");
    }

    private void saveToCassandra(JavaRDD<AggregatedAreaLoanData> data) {
        CassandraUtil<AggregatedAreaLoanData> containerUtil = new CassandraUtil<>(CASSANDRA_KEYSPACE, AggregatedAreaLoanData.class);
        containerUtil.save(TABLE_AGGREGATED_AREA_LOAN, AggregatedAreaLoanData.getColumnMap(), data);
    }

    private JavaRDD<LoanData> getLoans(SparkContextJavaFunctions context) {
        CassandraUtil<LoanData> util = new CassandraUtil<>(CASSANDRA_KEYSPACE, LoanData.class);
        return util.read(context, TABLE_LOAN_DATA, LoanData.getColumnMap());
    }

    private JavaRDD<UserCoordinates> getUserCoordinates(SparkContextJavaFunctions context) {
        CassandraUtil<UserCoordinates> util = new CassandraUtil<>(CASSANDRA_KEYSPACE, UserCoordinates.class);
        return util.read(context, TABLE_USERS_COORDINATES, UserCoordinates.getColumnMap());
    }

    private JavaRDD<UserDemographics> getUserDemographics(SparkContextJavaFunctions context) {
        CassandraUtil<UserDemographics> util = new CassandraUtil<>(CASSANDRA_KEYSPACE, UserDemographics.class);
        return util.read(context, TABLE_USERS_DEMOGRAPHICS, UserDemographics.getColumnMap());
    }
}
