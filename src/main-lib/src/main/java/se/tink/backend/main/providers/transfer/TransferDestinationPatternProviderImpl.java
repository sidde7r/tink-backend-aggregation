package se.tink.backend.main.providers.transfer;

import com.google.common.collect.ListMultimap;
import com.google.inject.Inject;
import se.tink.backend.common.repository.cassandra.TransferDestinationPatternRepository;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.User;
import se.tink.backend.core.account.TransferDestinationPattern;

public class TransferDestinationPatternProviderImpl implements TransferDestinationPatternProvider {
    private final TransferDestinationPatternRepository transferDestinationPatternRepository;

    @Inject
    public TransferDestinationPatternProviderImpl(
            TransferDestinationPatternRepository transferDestinationPatternRepository) {
        this.transferDestinationPatternRepository = transferDestinationPatternRepository;
    }

    @Override
    public ListMultimap<String, TransferDestinationPattern> getDestinationPatternsByAccountId(User user) {
        return transferDestinationPatternRepository.findAllByUserId(UUIDUtils.fromTinkUUID(user.getId()));
    }
}
