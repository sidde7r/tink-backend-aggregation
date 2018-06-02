package se.tink.backend.main.providers.transfer;

import com.google.common.collect.ListMultimap;
import se.tink.backend.core.User;
import se.tink.backend.core.account.TransferDestinationPattern;

public interface TransferDestinationPatternProvider {
    ListMultimap<String,TransferDestinationPattern> getDestinationPatternsByAccountId(User user);
}
