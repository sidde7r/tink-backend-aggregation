package se.tink.backend.main.providers.transfer;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import se.tink.backend.common.exceptions.DuplicateException;
import se.tink.backend.common.repository.cassandra.UserTransferDestinationRepository;
import se.tink.backend.common.utils.SwedbankClearingNumberUtils;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.User;
import se.tink.backend.core.account.UserTransferDestination;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.backend.core.transfer.TransferDestination;
import se.tink.backend.main.providers.transfer.dto.DestinationOfUserTransferDestination;
import se.tink.backend.utils.ProviderImageMap;

public class UserTransferDestinationProviderImpl implements UserTransferDestinationProvider {
    public static final String ALPHANUMERIC_WORDS_WITH_SEPARATORS =
            "^[0-9a-zA-ZäöåÄÖÅ]+((( ?(&|-) )*|[& +\\-_\\/\\\\])?[0-9a-zA-ZäöåÄÖÅ]+)*+$";

    private static AccountIdentifierFormatter DISPLAY_FORMATTER = new DisplayAccountIdentifierFormatter();

    private final UserTransferDestinationRepository userTransferDestinationRepository;
    private ProviderImageMap providerImages;

    @Inject
    public UserTransferDestinationProviderImpl(UserTransferDestinationRepository userTransferDestinationRepository,
            ProviderImageMap providerImages) {
        this.userTransferDestinationRepository = userTransferDestinationRepository;
        this.providerImages = providerImages;
    }

    @Override
    public TransferDestination createDestination(User user, URI uri, String name)
            throws IllegalArgumentException, DuplicateException {
        AccountIdentifier identifier = AccountIdentifier.create(uri);
        identifier.setName(name);

        identifier = validateAndClean(identifier);

        UserTransferDestination userTransferDestination = createUserTransferDestination(user, identifier);

        validateNoConflicts(userTransferDestination);

        userTransferDestinationRepository.save(userTransferDestination);

        return getTransferDestination(userTransferDestination);
    }

    private AccountIdentifier validateAndClean(AccountIdentifier identifier)
            throws IllegalArgumentException {
        Preconditions.checkArgument(identifier.isValid(), "Account number is not valid");
        Preconditions.checkArgument(identifier.getName().isPresent(), "Name is empty");
        Preconditions.checkArgument(identifier.getName().get().matches(ALPHANUMERIC_WORDS_WITH_SEPARATORS),
                "Name can only contain alphanumerical characters");

        if (SwedbankClearingNumberUtils.isSwedbank8xxxAccountNumber(identifier)) {
            try {
                SwedbankClearingNumberUtils.validateIfSwedbank8xxxIdentifier(identifier);
                SwedishIdentifier swedbank8xxxxIdentifier = identifier.to(SwedishIdentifier.class);
                identifier = SwedbankClearingNumberUtils
                        .removeZerosBetweenClearingAndAccountNumber(swedbank8xxxxIdentifier);
            } catch (IllegalStateException illegalState) {
                throw new IllegalArgumentException("Not valid clearing or account number for Swedbank account");
            }
        }

        return identifier;
    }

    private UserTransferDestination createUserTransferDestination(User user, AccountIdentifier identifier) {
        UserTransferDestination userTransferDestination = new UserTransferDestination();

        userTransferDestination.setUserId(UUIDUtils.fromTinkUUID(user.getId()));
        userTransferDestination.setIdentifier(identifier.getIdentifier());
        userTransferDestination.setType(identifier.getType());
        userTransferDestination.setName(identifier.getName().get());

        return userTransferDestination;
    }

    private void validateNoConflicts(UserTransferDestination userTransferDestination) throws DuplicateException {
        Iterable<UserTransferDestination> userTransferDestinations =
                userTransferDestinationRepository.findAllByUserId(userTransferDestination.getUserId());

        boolean hasConflict = FluentIterable
                .from(userTransferDestinations)
                .anyMatch(Predicates.equalTo(userTransferDestination));

        if (hasConflict) {
            throw new DuplicateException("The destination already exists.");
        }
    }

    private TransferDestination getTransferDestination(UserTransferDestination userTransferDestination) {
        DestinationOfUserTransferDestination destination =
                DestinationOfUserTransferDestination.USERTRANSFERDESTINATION_TO_DESTINATION
                        .apply(userTransferDestination);

        if (destination == null) {
            throw new NullPointerException("Unexpectedly could not convert UserTransferDestination to Destination");
        }

        Optional<AccountIdentifier> displayIdentifier = destination.getDisplayIdentifier();

        TransferDestination transferDestination = new TransferDestination();
        transferDestination.setBalance(null);
        transferDestination.setDisplayBankName(destination.getDisplayBankName().orElse(null));
        transferDestination.setUri(destination.getPrimaryIdentifier().get().toURI());
        transferDestination.setName(destination.getName().orElse(null));
        transferDestination.setType(destination.getType().toString());
        transferDestination.setImages(destination.getImageUrls(providerImages));

        if (displayIdentifier.isPresent()) {
            transferDestination.setDisplayAccountNumber(displayIdentifier.get().getIdentifier(DISPLAY_FORMATTER));
        }

        return transferDestination;
    }

    @Override
    public List<UserTransferDestination> getDestinations(User user) {
        UUID userId = UUIDUtils.fromTinkUUID(user.getId());
        return userTransferDestinationRepository.findAllByUserId(userId);
    }
}
