package se.tink.analytics.merchantmap.functions;

import java.io.Serializable;
import org.apache.spark.api.java.function.Function2;
import se.tink.analytics.merchantmap.entities.MerchantMap;

public class Reducers implements Serializable {

    private static final long serialVersionUID = 5808540326395397588L;

    // Reduces a Merchant Map object
    public static final Function2<MerchantMap, MerchantMap, MerchantMap> MERGE_MERCHANT_MAPS =
            new Function2<MerchantMap, MerchantMap, MerchantMap>() {
                @Override
                public MerchantMap call(MerchantMap m1, MerchantMap m2) throws Exception {
                    return m1.merge(m2);
                }
            };
}
