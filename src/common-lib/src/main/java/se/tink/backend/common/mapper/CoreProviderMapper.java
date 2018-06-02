package se.tink.backend.common.mapper;

import org.assertj.core.util.VisibleForTesting;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import se.tink.backend.core.Provider;

public class CoreProviderMapper {
    /**
     * ModelMapper for se.tink.backend.core.Provider to Aggregation RPC Provider
     */
    @VisibleForTesting
    static final TypeMap<Provider, se.tink.backend.aggregation.rpc.Provider> aggregationProviderMap = new ModelMapper()
            .createTypeMap(Provider.class, se.tink.backend.aggregation.rpc.Provider.class);

    /**
     * Utility function to convert core User to the API user for the Aggregation service
     */
    public static se.tink.backend.aggregation.rpc.Provider toAggregationProvider(Provider provider) {
        return aggregationProviderMap.map(provider);
    }
}
