package se.tink.backend.aggregation.agents.utils.mappers;

import org.assertj.core.util.VisibleForTesting;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import se.tink.backend.core.Credentials;

public class CoreCredentialsMapper {
    /**
     * ModelMapper for se.tink.backend.core.Credentials to Aggregation RPC Credentials
     */
    @VisibleForTesting
    static final TypeMap<Credentials, se.tink.backend.aggregation.rpc.Credentials> toAggregationMap = new ModelMapper()
            .createTypeMap(Credentials.class, se.tink.backend.aggregation.rpc.Credentials.class)
            .addMappings(mapper -> mapper.skip(se.tink.backend.aggregation.rpc.Credentials::setPersistentSession));

    /**
     * ModelMapper for converting a Aggregation RPR Credentials object to a core.Credentals
     */
    @VisibleForTesting
    static final TypeMap<se.tink.backend.aggregation.rpc.Credentials, Credentials> fromAggregationMap = new ModelMapper()
            .createTypeMap(se.tink.backend.aggregation.rpc.Credentials.class, Credentials.class)
            .addMappings(mapper -> mapper.skip(Credentials::setPersistentSession));

    /**
     * Utility function to convert core User to the API user for the Aggregation service
     */
    public static se.tink.backend.aggregation.rpc.Credentials toAggregationCredentials(Credentials credentials) {
        return toAggregationMap.map(credentials);
    }

    /**
     * Utility function to convert core User to the API user for the Aggregation service
     */
    public static Credentials fromAggregationCredentials(se.tink.backend.aggregation.rpc.Credentials credentials) {
        return fromAggregationMap.map(credentials);
    }
}
