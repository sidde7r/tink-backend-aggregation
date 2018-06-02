package se.tink.analytics.spark.functions;

import java.util.UUID;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;
import se.tink.backend.core.CassandraTransaction;

@SuppressWarnings("serial")
public class SeedDescriptionExtrapolationCorpusSparkFunctions extends SparkFunctions {

    public static class Filters extends SparkFunctions.Filters {
        /**
         * Filter transactions by minimum length of the (original) description.
         *
         * @param minLength
         */
        public static Function<CassandraTransaction, Boolean> minDescriptionLength(final int minLength) {
            return new Function<CassandraTransaction, Boolean>() {
                @Override
                public Boolean call(CassandraTransaction transaction) throws Exception {
                    String description = transaction.getOriginalDescription();
                    return description != null && description.length() >= minLength;
                }
            };
        }
    }

    public static class Mappers extends SparkFunctions.Mappers {
        /**
         * Transform a transaction into a tuple of user ID and (cleaned, original) description.
         */
        public final static PairFunction<CassandraTransaction, UUID, String> DESCRIPTION_BY_USER = new PairFunction<CassandraTransaction, UUID, String>() {
            @Override
            public Tuple2<UUID, String> call(CassandraTransaction transaction) throws Exception {
                String description = transaction
                        .getOriginalDescription()
                        .toLowerCase()
                        .replaceAll("[ \t]*[,]*[ \t]+", " ")    // Replace whitespace sequences (including comma) with a space
                        .replaceAll("^[\" ]+", "")              // Trim head
                        .replaceAll("[,\" ]+$", "");            // Trim tail

                return new Tuple2<UUID, String>(transaction.getUserId(), description);
            }
        };

        /**
         * Prepare for counting occurrences of user ID/description pairs.
         */
        public final static PairFunction<Tuple2<UUID, String>, String, Integer> PREPARE_FOR_COUNT = new PairFunction<Tuple2<UUID, String>, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(Tuple2<UUID, String> tuple) throws Exception {
                return new Tuple2<String, Integer>(tuple._2(), 1);
            }
        };
    }
}
