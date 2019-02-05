package se.tink.backend.aggregation.agents.utils.mappers;

import org.assertj.core.util.VisibleForTesting;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import se.tink.libraries.user.rpc.User;

/**
 * CoreUserMapper contains tools for converting {@link se.tink.libraries.user.rpc.User} objects to
 * API objects consumed by other services.
 */
public class CoreUserMapper {
    /** ModelMapper for {@link se.tink.libraries.user.rpc.User} to Aggregation RPC User */
    @VisibleForTesting
    static final TypeMap<User, se.tink.libraries.user.rpc.User> aggregationUserMap =
            new ModelMapper().createTypeMap(User.class, se.tink.libraries.user.rpc.User.class);

    /** Utility function to convert core User to the API user for the Aggregation service */
    public static se.tink.libraries.user.rpc.User toAggregationUser(User user) {
        return aggregationUserMap.map(user);
    }
}
