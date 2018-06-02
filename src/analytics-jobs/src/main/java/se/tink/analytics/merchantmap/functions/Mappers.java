package se.tink.analytics.merchantmap.functions;

import java.io.Serializable;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;
import se.tink.analytics.merchantmap.entities.CassandraMerchantMap;
import se.tink.analytics.merchantmap.entities.MerchantMap;
import se.tink.analytics.merchantmap.entities.MerchantMapTransaction;

public class Mappers implements Serializable {

    private static final long serialVersionUID = 2313196904173665469L;

    // Transform a merchant map transaction into a pair of original description and merchant map
    public static final PairFunction<MerchantMapTransaction, String, MerchantMap> MERCHANT_MAP_TRANSACTION_TO_MERCHANT_MAP =
            new PairFunction<MerchantMapTransaction, String, MerchantMap>() {
                @Override
                public Tuple2<String, MerchantMap> call(MerchantMapTransaction t) throws Exception {
                    return new Tuple2<>(t.getOriginalDescription().toLowerCase(),
                            new MerchantMap(t.getMerchantId(), t.getUserId()));
                }
            };

    // Transform a merchant map to a cassandra merchant map entity
    public static final Function<Tuple2<String, MerchantMap>, CassandraMerchantMap> TO_CASSANDRA_MERCHANT_MAP =
            new Function<Tuple2<String, MerchantMap>, CassandraMerchantMap>() {

                @Override
                public CassandraMerchantMap call(Tuple2<String, MerchantMap> t) throws Exception {
                    return new CassandraMerchantMap(t._1(), t._2().getResolvedMerchant(),
                            t._2().getExtendedInformation());
                }
            };
}
