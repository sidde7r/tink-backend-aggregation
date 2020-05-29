package se.tink.backend.aggregation.agents.utils.mappers;

import org.assertj.core.util.VisibleForTesting;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import se.tink.libraries.credentials.rpc.Credentials;

public class CoreCredentialsMapper {

    private static CoreCredentialsMapper singleton;

    /**
     * ModelMapper for se.tink.libraries.credentials.rpc.Credentials to Aggregation RPC Credentials
     */
    @VisibleForTesting
    final TypeMap<Credentials, se.tink.backend.agents.rpc.Credentials> toAggregationMap;

    /** ModelMapper for converting a Aggregation RPR Credentials object to a core.Credentals */
    @VisibleForTesting
    final TypeMap<se.tink.backend.agents.rpc.Credentials, Credentials> fromAggregationMap;

    private CoreCredentialsMapper() {
        toAggregationMap =
                new ModelMapper()
                        .createTypeMap(
                                Credentials.class, se.tink.backend.agents.rpc.Credentials.class)
                        .addMappings(
                                mapper ->
                                        mapper.skip(
                                                se.tink.backend.agents.rpc.Credentials
                                                        ::setSensitivePayloadSerialized))
                        .addMappings(
                                mapper ->
                                        mapper.skip(
                                                se.tink.backend.agents.rpc.Credentials
                                                        ::setSensitivePayloadAsMap))
                        .addMappings(
                                mapper ->
                                        mapper.skip(
                                                se.tink.backend.agents.rpc.Credentials
                                                        ::setPersistentSession))
                        .addMappings(
                                mapper ->
                                        mapper.skip(
                                                se.tink.backend.agents.rpc.Credentials
                                                        ::setForceManualAuthentication));
        fromAggregationMap =
                new ModelMapper()
                        .createTypeMap(
                                se.tink.backend.agents.rpc.Credentials.class, Credentials.class);
    }

    @VisibleForTesting
    static CoreCredentialsMapper getInstance() {
        if (singleton == null) {
            singleton = new CoreCredentialsMapper();
        }
        return singleton;
    }

    /** Utility function to convert core User to the API user for the Aggregation service */
    public static se.tink.backend.agents.rpc.Credentials toAggregationCredentials(
            Credentials credentials) {
        return getInstance().toAggregationMap.map(credentials);
    }

    /** Utility function to convert core User to the API user for the Aggregation service */
    public static Credentials fromAggregationCredentials(
            se.tink.backend.agents.rpc.Credentials credentials) {
        return getInstance().fromAggregationMap.map(credentials);
    }
}
