package se.tink.backend.system.mapper;

import org.assertj.core.util.VisibleForTesting;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import se.tink.backend.core.Portfolio;

public class CorePortfolioMapper {

    // ModelMapper for converting a Aggregation RPC Portfolio object to a core.Portfolio.
    @VisibleForTesting
    static final TypeMap<se.tink.backend.system.rpc.Portfolio, Portfolio> fromSystemToCoreMap = new ModelMapper()
            .createTypeMap(se.tink.backend.system.rpc.Portfolio.class, Portfolio.class)
            .addMappings(mapper -> mapper.skip(Portfolio::setId))
            .addMappings(mapper -> mapper.skip(Portfolio::setAccountId))
            .addMappings(mapper -> mapper.skip(Portfolio::setUserId))
            .addMappings(mapper -> mapper.skip(Portfolio::setCredentials));

    /**
     * Utility function to convert core Portfolio to the API Portfolio for the Aggregation service.
     */
    public static Portfolio fromSystemToCore(se.tink.backend.system.rpc.Portfolio portfolio) {
        return fromSystemToCoreMap.map(portfolio);
    }
}
