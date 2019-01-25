package se.tink.backend.aggregation.agents.utils.mappers;

import org.assertj.core.util.VisibleForTesting;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import se.tink.backend.core.User;

/**
 * CoreUserMapper contains tools for converting {@link se.tink.backend.core.User} objects to API objects consumed by other
 * services.
 *
 * Historically (pre 2017-09) many services (system/main/aggregation) have used the same classes from the
 * {@link se.tink.backend.core} package. When making services more independent of each other it is increasingly
 * important to not share packages that have different purposes (client/server/database-models) for different services.
 *
 * This will also have the added bonus that the Aggregation service will not depend on common-lib
 * or main-api when the migration is complete.
 */
public class CoreUserMapper {
    /**
     * ModelMapper for {@link se.tink.backend.core.User} to Aggregation RPC User
     */
    @VisibleForTesting
    static final TypeMap<User, se.tink.backend.agents.rpc.User> aggregationUserMap = new ModelMapper()
            .createTypeMap(User.class, se.tink.backend.agents.rpc.User.class);

    /**
     * Utility function to convert core User to the API user for the Aggregation service
     */
    public static se.tink.backend.agents.rpc.User toAggregationUser(User user) {
        return aggregationUserMap.map(user);
    }
}
