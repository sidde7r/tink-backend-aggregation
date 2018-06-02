package se.tink.backend.main.providers.transfer;

import java.net.URI;
import java.util.List;
import se.tink.backend.core.User;
import se.tink.backend.core.account.UserTransferDestination;
import se.tink.backend.core.transfer.TransferDestination;

public interface UserTransferDestinationProvider {
    TransferDestination createDestination(User user, URI uri, String name);
    List<UserTransferDestination> getDestinations(User user);
}
