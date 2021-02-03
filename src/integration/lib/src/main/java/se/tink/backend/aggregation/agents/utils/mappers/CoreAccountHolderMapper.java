package se.tink.backend.aggregation.agents.utils.mappers;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import javax.annotation.Nullable;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import se.tink.backend.aggregationcontroller.v1.rpc.accountholder.AccountHolder;

public class CoreAccountHolderMapper {

    @VisibleForTesting
    static TypeMap<se.tink.backend.agents.rpc.AccountHolder, AccountHolder> fromAggregationTypeMap =
            new ModelMapper()
                    .typeMap(se.tink.backend.agents.rpc.AccountHolder.class, AccountHolder.class);

    public static Optional<AccountHolder> fromAggregation(
            @Nullable se.tink.backend.agents.rpc.AccountHolder holder) {
        if (holder == null) {
            return Optional.empty();
        }
        return Optional.of(fromAggregationTypeMap.map(holder));
    }
}
