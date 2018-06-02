package se.tink.analytics.merchantmap.functions;

import java.io.Serializable;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;
import se.tink.analytics.merchantmap.entities.MerchantMap;
import se.tink.analytics.merchantmap.entities.MerchantMapTransaction;

public class Filters implements Serializable {

    private static final long serialVersionUID = -2164953069816238843L;

    // Filter out the maps where we can pick a winning merchant
    public static final Function<Tuple2<String, MerchantMap>, Boolean> FILTER_RESOLVABLE_MERCHANTS =
            new Function<Tuple2<String, MerchantMap>, Boolean>() {
                @Override
                public Boolean call(Tuple2<String, MerchantMap> t) throws Exception {
                    return t._2().canResolveMerchant();
                }
            };

    // Filter out valid rows
    public static final Function<MerchantMapTransaction, Boolean> FILTER_EXPENSES_AND_MERCHANT =
            new Function<MerchantMapTransaction, Boolean>() {
                @Override
                public Boolean call(MerchantMapTransaction t) throws Exception {
                    return t.isValid();
                }
            };
}
