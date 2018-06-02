package se.tink.backend.main.providers.transfer.dto;

import com.google.common.base.Function;
import java.util.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import org.apache.http.client.utils.URIBuilder;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.ImageUrls;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.backend.core.transfer.TransferDestination;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.ProviderDisplayNameFinder;
import se.tink.backend.utils.ProviderImageMap;

/**
 * Intermediate object to be used for filtering and sorting out the relation between an account and its valid
 * destinations.
 */
public class AccountDestinations {

    private static final AccountIdentifierFormatter DISPLAY_FORMATTER = new DisplayAccountIdentifierFormatter();
    private final Account account;
    private Iterable<Destination> destinations;
    private final Optional<? extends Iterable<TransferDestinationPattern>> accountPatterns;
    private static final LogUtils log = new LogUtils(AccountDestinations.class);

    public AccountDestinations(Account account, Iterable<Destination> destinations,
            Optional<? extends Iterable<TransferDestinationPattern>> accountPatterns) {
        this.account = account;
        this.destinations = removeThisAccount(account, destinations);
        this.accountPatterns = accountPatterns;
    }

    private static Iterable<Destination> removeThisAccount(final Account account, Iterable<Destination> destinations) {
        return Iterables.filter(destinations, destination -> !destination.isDefinedBy(account));
    }

    public Account getAccount() {
        return account;
    }

    public void removeDestinationsNotMatchingPatterns() {
        destinations = getValidDestinations();
    }

    public void removeDestinationsWithoutIdentifiers() {
        destinations = Iterables.filter(destinations, Destination.HAS_IDENTIFIER);
    }

    public Iterable<Destination> getDestinations() {
        return destinations;
    }

    private Iterable<Destination> getUnfilteredDestinations() {
        return destinations;
    }

    public Optional<? extends Iterable<TransferDestinationPattern>> getAccountPatterns() {
        return accountPatterns;
    }

    private Iterable<Destination> getValidDestinations() {
        if (!accountPatterns.isPresent()) {
            return getUnfilteredDestinations();
        }

        return FluentIterable
                .from(destinations)
                .transform(destination -> {
                    Destination copy = destination.copyOf();
                    copy.filterIdentifiersWithPatterns(accountPatterns.get());
                    return copy;
                })
                .filter(Destination.HAS_IDENTIFIER);
    }

    /**
     * Transform that uses patterns and destinations to form an AccountDestinations object, that is a composite
     * object of both accounts and destinations as intermediate object
     */
    public static class FromAccountTransform implements Function<Account, AccountDestinations> {
        private final ListMultimap<String, TransferDestinationPattern> patternsByAccountId;
        private final Iterable<Destination> destinations;

        public FromAccountTransform(
                final ListMultimap<String, TransferDestinationPattern> patternsByAccountId,
                final Iterable<Destination> destinations) {
            this.patternsByAccountId = patternsByAccountId;
            this.destinations = destinations;
        }

        @Override
        public AccountDestinations apply(Account account) {
            final Iterable<TransferDestinationPattern> accountPatterns = patternsByAccountId
                    .get(account.getId());

            final AccountDestinations accountDestinations = new AccountDestinations(
                    account,
                    destinations,
                    Optional.ofNullable(accountPatterns)
            );

            accountDestinations.removeDestinationsWithoutIdentifiers();
            accountDestinations.removeDestinationsNotMatchingPatterns();

            return accountDestinations;
        }
    }

    /**
     * Transform that turns AccountDestinations into ready for use Account objects, to be sent to
     * frontend. This complements all account objects with their corresponding destinations.
     */
    public static class ToAccountTransform implements Function<AccountDestinations, Account> {
        private final Map<String, String> providerNameByCredentialsId;
        private final ProviderImageMap providerImages;
        private boolean explicitDestinationsQueried;

        public ToAccountTransform(
                ProviderDisplayNameFinder displayNameFinder, ProviderImageMap providerImages,
                boolean explicitDestinationsQueried) {
            this.explicitDestinationsQueried = explicitDestinationsQueried;
            this.providerNameByCredentialsId = displayNameFinder.getIndexedByCredentialsId();
            this.providerImages = providerImages;
        }

        @Override
        public Account apply(AccountDestinations accountDestinations) {
            Account account = accountDestinations.getAccount();
            Iterable<Destination> destinations = accountDestinations.getDestinations();

            List<TransferDestination> transferDestinations = Lists.newArrayList();
            for (Destination destination : destinations) {
                TransferDestination transferDestination = toTransferDestination(destination);
                transferDestinations.add(transferDestination);
            }

            // Add multimatch patterns for this account.
            if (!explicitDestinationsQueried) {
                transferDestinations.addAll(createMultiMatchTransferDestinations(accountDestinations));
            }

            account.setTransferDestinations(transferDestinations);

            addImageUrlsToAccount(account);

            return account;
        }

        private List<TransferDestination> createMultiMatchTransferDestinations(AccountDestinations accountDestinations) {

            List<TransferDestination> transferDestination = Lists.newArrayList();

                if (accountDestinations.getAccountPatterns().isPresent()) {
                    for (TransferDestinationPattern pattern : accountDestinations.getAccountPatterns().get()) {
                        if (pattern != null && pattern.isMatchesMultiple()) {
                            TransferDestination destination = new TransferDestination();
                            destination.setMatchesMultiple(pattern.isMatchesMultiple());

                            URI uri = null;
                            try {
                                uri = new URIBuilder()
                                    .setScheme(pattern.getType().toString())
                                    .setHost(pattern.getPattern())
                                    .build();

                            } catch (URISyntaxException e) {
                                log.error(accountDestinations.getAccount().getUserId(), String.format(
                                        "Could not add multimatch transfer destinations for account %s", accountDestinations
                                        .getAccount().getName()), e);
                            }
                            destination.setUri(uri);

                            transferDestination.add(destination);
                        }
                    }
                }

            return transferDestination;
        }

        private void addImageUrlsToAccount(Account account) {
            String providerName = providerNameByCredentialsId.get(account.getCredentialsId());
            AccountTypes accountType = account.getType();
            ImageUrls accountImages = providerImages.getImagesForAccount(providerName, accountType);

            account.setImages(accountImages);
        }

        private TransferDestination toTransferDestination(Destination destination) {
            TransferDestination transferDestination = new TransferDestination();

            AccountIdentifier primaryIdentifier = destination.getPrimaryIdentifier().get();
            AccountIdentifier displayIdentifier = destination.getDisplayIdentifier().get();

            transferDestination.setDisplayAccountNumber(displayIdentifier.getIdentifier(DISPLAY_FORMATTER));
            transferDestination.setUri(primaryIdentifier.toURI());
            transferDestination.setName(destination.getName().orElse(null));
            transferDestination.setType(destination.getType().toString());
            transferDestination.setDisplayBankName(destination.getDisplayBankName().orElse(null));

            if (destination.is(DestinationOfAccount.class)) {
                DestinationOfAccount typedDestination = destination.to(DestinationOfAccount.class);
                transferDestination.setBalance(typedDestination.getBalance());
                transferDestination.setImages(
                        typedDestination.getImageUrls(providerNameByCredentialsId, providerImages));
            }

            if (destination.is(DestinationOfPattern.class)) {
                DestinationOfPattern typedDestination = destination.to(DestinationOfPattern.class);
                transferDestination.setImages(
                        typedDestination
                                .getImageUrls(providerImages));
            }

            if (destination.is(DestinationOfUserTransferDestination.class)) {
                DestinationOfUserTransferDestination typedDestination =
                        destination.to(DestinationOfUserTransferDestination.class);
                transferDestination.setImages(
                        typedDestination
                                .getImageUrls(providerImages));
            }

            if (destination.is(DestinationOfExplicitDestination.class)) {
                DestinationOfExplicitDestination typedDestination =
                        destination.to(DestinationOfExplicitDestination.class);

                transferDestination.setImages(typedDestination.getImageUrls(providerImages));
            }

            return transferDestination;
        }
    }
}
