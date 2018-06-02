package se.tink.analytics.jobs.loandata;

import java.util.UUID;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

public class UserCoordinatesIdsToTuple implements PairFunction<UserCoordinates, UUID, UUID> {

    @Override
    public Tuple2<UUID, UUID> call(UserCoordinates uc) throws Exception {
        return new Tuple2<>(uc.getUserId(), uc.getAreaId());
    }
}
