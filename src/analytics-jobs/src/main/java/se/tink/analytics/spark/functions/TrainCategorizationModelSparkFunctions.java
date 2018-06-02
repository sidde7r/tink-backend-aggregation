package se.tink.analytics.spark.functions;

import java.util.Map;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;
import se.tink.analytics.entites.CategorizationTransaction;
import se.tink.analytics.jobs.categorization.entities.TransactionGroup;
import se.tink.analytics.jobs.categorization.utils.DescriptionUtils;
import se.tink.backend.core.CassandraTransaction;
import se.tink.backend.core.Cities;

@SuppressWarnings("serial")
public class TrainCategorizationModelSparkFunctions extends SparkFunctions {
    
    public static class Mappers extends SparkFunctions.Mappers {

        public final static Function<CassandraTransaction, CategorizationTransaction> toCategorizationTransaction(
                final Broadcast<Map<String, String>> marketByUserId,
                final Broadcast<Map<String, String>> categoryCodeById,
                final Broadcast<Map<String, Cities>> citiesByMarketBroadcast) {
            return new Function<CassandraTransaction, CategorizationTransaction>() {
                @Override
                public CategorizationTransaction call(CassandraTransaction transaction) throws Exception {

                    String description = transaction.getDescription();
                    if (transaction.isUserModifiedDescription()) {
                        description = transaction.getFormattedDescription();
                    }
                    if (description == null || description.length() == 0) {
                        description = transaction.getOriginalDescription();
                    }

                    String categoryCode = categoryCodeById.getValue().get(transaction.getTinkCategoryId());
                    String market = marketByUserId.getValue().get(transaction.getTinkUserId());
                    String userId = transaction.getTinkUserId();

                    // Clean the description
                    description = DescriptionUtils
                            .cleanDescription(description, citiesByMarketBroadcast.getValue(), market);

                    return new CategorizationTransaction(description, categoryCode, market, userId);
                }
            };
        }

        public final static PairFunction<CategorizationTransaction, String, TransactionGroup> MAP_BY_DESCRIPTION =

                new PairFunction<CategorizationTransaction, String, TransactionGroup>() {
                    @Override
                    public Tuple2<String, TransactionGroup> call(CategorizationTransaction transaction)
                            throws Exception {

                        String key = String.format("%s|%s", transaction.getMarket(), transaction.getDescription());

                        TransactionGroup value = new TransactionGroup(transaction.getMarket(),
                                transaction.getDescription(),
                                transaction.getCategoryCode(),
                                transaction.getUserId());

                        return new Tuple2<>(key, value);
                    }
                };

    }

    public static class Reducers extends SparkFunctions.Reducers {

        /**
         * Merge a transaction group with another
         */
        public static final Function2<TransactionGroup, TransactionGroup, TransactionGroup> MERGE_TRANSACTION_GROUP =
                new Function2<TransactionGroup, TransactionGroup, TransactionGroup>() {
                    @Override
                    public TransactionGroup call(TransactionGroup left, TransactionGroup right) throws Exception {
                        return left.merge(right);
                    }
                };
    }

    public static class Filters extends SparkFunctions.Filters {

        public static Function<Tuple2<String, TransactionGroup>, Boolean> withMinUsers(final int minUsers) {

            return new Function<Tuple2<String, TransactionGroup>, Boolean>() {
                @Override

                public Boolean call(Tuple2<String, TransactionGroup> tuple) throws Exception {

                    TransactionGroup group = tuple._2;

                    group.filterByMinUsers(minUsers);

                    return !group.isEmpty();
                }
            };
        }

        public static Function<Tuple2<String, TransactionGroup>, Boolean> withMinOccurrences(
                final int minOccurrences) {

            return new Function<Tuple2<String, TransactionGroup>, Boolean>() {
                @Override

                public Boolean call(Tuple2<String, TransactionGroup> tuple) throws Exception {

                    TransactionGroup group = tuple._2;

                    group.filterByMinOccurrences(minOccurrences);

                    return !group.isEmpty();
                }
            };
        }
    }
}
