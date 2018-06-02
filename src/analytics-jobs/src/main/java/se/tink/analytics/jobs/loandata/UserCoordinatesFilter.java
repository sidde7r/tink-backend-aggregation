package se.tink.analytics.jobs.loandata;

import java.util.HashSet;
import java.util.UUID;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;

public class UserCoordinatesFilter implements Function<UserCoordinates, Boolean> {
    private Broadcast<HashSet<UUID>> users;

    public UserCoordinatesFilter(Broadcast<HashSet<UUID>> users) {
        this.users = users;
    }

    @Override
    public Boolean call(UserCoordinates uc) throws Exception {
        HashSet<UUID> users = this.users.value();

        if (uc.getAreaId() == null) {
            return false;
        }

        if (!users.contains(uc.getUserId())) {
            return false;
        }

        return true;
    }
}
