package se.tink.analytics.jobs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import se.tink.analytics.AnalyticsContext;
import se.tink.analytics.merchantmap.entities.CassandraMerchantMap;
import se.tink.analytics.merchantmap.entities.Merchant;
import se.tink.analytics.merchantmap.entities.MerchantMap;
import se.tink.analytics.merchantmap.entities.MerchantMapTransaction;
import se.tink.analytics.merchantmap.entities.MerchantMapInformationExtender;
import se.tink.analytics.merchantmap.functions.Filters;
import se.tink.analytics.merchantmap.functions.Mappers;
import se.tink.analytics.merchantmap.functions.Reducers;
import se.tink.analytics.utils.CassandraUtil;

/**
 * Spark job that calculates which transactions that we automatically should assign a merchant to.
 * <p/>
 * Calculation is done by grouping all transactions by description and look at which merchants users have assigned
 * to them. Logic for resolving a merchant is available in MerchantMap#getResolvedMerchant
 * <p/>
 * Inputs: Cassandra table transactions, Mysql table merchants
 * Outputs: Writes result to Cassandra table merchant_maps (table is used in the TransactionProcessor)
 */
public class MerchantMapJob {

    private static final String CASSANDRA_KEYSPACE = "tink";
    private static final String CASSANDRA_TRANSACTION_TABLE = "transactions";
    private static final String CASSANDRA_MERCHANT_TABLE = "merchant_maps";

    private AnalyticsContext analyticsContext;

    public MerchantMapJob(AnalyticsContext analyticsContext) {
        this.analyticsContext = analyticsContext;
    }

    public static void main(String[] args) throws Exception {

        Preconditions.checkArgument(args.length > 0, "Configuration file must be provided");

        AnalyticsContext context = AnalyticsContext.build("MERCHANT_MAP_JOB", args[0]);

        context.start();
        try {
            new MerchantMapJob(context).run();
        } finally {
            context.stop();
        }
    }

    private void run() throws SQLException {

        System.out.println("Generating merchant maps");

        System.out.println("Collecting merchants from mysql");

        ImmutableMap<String, Merchant> merchants = getMerchants();

        System.out.println(String.format("Fetched '%d' merchants from database", merchants.size()));

        JavaSparkContext sparkContext = analyticsContext.getSparkContext();

        System.out.println("Broadcasting merchants to workers");

        Broadcast<ImmutableMap<String, Merchant>> broadCastMerchantMap = sparkContext.broadcast(merchants);

        System.out.println("Broadcasting complete");

        // Get a list of merchant maps
        JavaPairRDD<String, MerchantMap> maps = getMerchantMaps(analyticsContext);

        // Reduce to one item for each transaction description
        maps = maps.reduceByKey(Reducers.MERGE_MERCHANT_MAPS);

        // Filter only resolvable merchants
        maps = maps.filter(Filters.FILTER_RESOLVABLE_MERCHANTS);

        // Add merchant information to each map
        maps = maps.mapValues(new MerchantMapInformationExtender(broadCastMerchantMap));

        // Convert to transaction
        JavaRDD<CassandraMerchantMap> merchantTransactions = maps.map(Mappers.TO_CASSANDRA_MERCHANT_MAP);

        // Save back to Cassandra
        saveToCassandra(merchantTransactions);

        System.out.println("Done generating Merchant Map");
    }

    private JavaPairRDD<String, MerchantMap> getMerchantMaps(AnalyticsContext context) {
        CassandraUtil<MerchantMapTransaction> util = new CassandraUtil<>(CASSANDRA_KEYSPACE,
                MerchantMapTransaction.class);

        JavaRDD<MerchantMapTransaction> transactions = util.read(context.getJavaFunc(), CASSANDRA_TRANSACTION_TABLE,
                MerchantMapTransaction.getColumnMap());

        return transactions.filter(Filters.FILTER_EXPENSES_AND_MERCHANT).mapToPair(
                Mappers.MERCHANT_MAP_TRANSACTION_TO_MERCHANT_MAP);
    }

    private void saveToCassandra(JavaRDD<CassandraMerchantMap> transactions) {
        new CassandraUtil<>(CASSANDRA_KEYSPACE, CassandraMerchantMap.class)
                .save(CASSANDRA_MERCHANT_TABLE, CassandraMerchantMap.getColumnMap(), transactions);
    }

    private ImmutableMap<String, Merchant> getMerchants() throws SQLException {

        ImmutableMap.Builder<String, Merchant> merchantMap = ImmutableMap.builder();

        try (Statement stmt = analyticsContext.getMysqlConnection().createStatement()) {
            if (stmt.execute("SELECT id, name, source, parentId  FROM merchants")) {
                try (ResultSet resultset = stmt.getResultSet()) {
                    while (resultset.next()) {

                        String id = resultset.getString(1);

                        Merchant merchant = new Merchant();
                        merchant.setName(resultset.getString(2));
                        merchant.setSource(resultset.getString(3));
                        merchant.setParentId(resultset.getString(4));

                        merchantMap.put(id, merchant);
                    }
                }
            }
        }

        return merchantMap.build();
    }
}
