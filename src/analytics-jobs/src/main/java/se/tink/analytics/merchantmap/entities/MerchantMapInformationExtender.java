package se.tink.analytics.merchantmap.entities;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;

/**
 * Function that decorates a merchant map with more information about merchants.
 * Broadcast functionality is used for better Spark performance
 */
public class MerchantMapInformationExtender implements Function<MerchantMap, MerchantMap> {
    private Broadcast<ImmutableMap<String, Merchant>> map;

    public MerchantMapInformationExtender(Broadcast<ImmutableMap<String, Merchant>> map) {
        this.map = map;
    }

    @Override
    public MerchantMap call(MerchantMap t) throws Exception {

        // Get used merchants
        Set<UUID> ids = t.getMerchantIds();

        HashMap<UUID, Merchant> result = new HashMap<>();

        for (UUID id : ids) {
            Merchant m = map.value().get(id.toString().replace("-", ""));

            if (m != null) {
                result.put(id, m);
            }
        }

        t.setExtendedMerchantInformation(result);

        return t;
    }
}
