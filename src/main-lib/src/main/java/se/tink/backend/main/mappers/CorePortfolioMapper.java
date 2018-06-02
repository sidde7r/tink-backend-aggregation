package se.tink.backend.main.mappers;

import org.assertj.core.util.VisibleForTesting;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import se.tink.backend.core.Portfolio;

public class CorePortfolioMapper {

     // ModelMapper for converting a core Portfolio object to a Main RPC object.
    @VisibleForTesting
    static final TypeMap<Portfolio, se.tink.backend.rpc.Portfolio> fromCoreToMainMap = new ModelMapper()
            .createTypeMap(Portfolio.class, se.tink.backend.rpc.Portfolio.class);

    /**
     * Utility function to convert a core Portfolio to the API Portfolio for the Main service.
     */
    public static se.tink.backend.rpc.Portfolio fromCoreToMain(Portfolio portfolio) {
        return fromCoreToMainMap.map(portfolio);
    }
}
