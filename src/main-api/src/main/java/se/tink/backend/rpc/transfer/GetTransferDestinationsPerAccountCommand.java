package se.tink.backend.rpc.transfer;

import com.google.common.base.Preconditions;
import se.tink.backend.core.User;

public class GetTransferDestinationsPerAccountCommand {
    private final User user;

    public GetTransferDestinationsPerAccountCommand(User user) {
        Preconditions.checkNotNull(user);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
