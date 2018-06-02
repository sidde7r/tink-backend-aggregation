package se.tink.backend.main.providers.transfer.dto.utils;

import java.util.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.account.UserTransferDestination;
import se.tink.backend.main.providers.transfer.dto.Destination;
import se.tink.backend.main.providers.transfer.dto.DestinationOfAccount;
import se.tink.backend.main.providers.transfer.dto.DestinationOfExplicitDestination;
import se.tink.backend.main.providers.transfer.dto.DestinationOfPattern;
import se.tink.backend.main.providers.transfer.dto.DestinationOfUserTransferDestination;

public class DestinationBuilder {
    private Iterable<Destination> destinations;
    private Optional<? extends Set<AccountIdentifier.Type>> explicitTypeFilter;

    private DestinationBuilder() {
        destinations = Lists.newArrayList();
        explicitTypeFilter = Optional.empty();
    }

    public static DestinationBuilder create() {
        return new DestinationBuilder();
    }

    public Iterable<Destination> build() {
        return filterDestinationTypes();
    }

    private Iterable<Destination> filterDestinationTypes() {
        if (!explicitTypeFilter.isPresent()) {
            return destinations;
        }

        return FluentIterable.from(destinations)
                .filter(destination -> {
                    if (destination == null) {
                        return false;
                    } else {
                        destination.setTypesExclusive(explicitTypeFilter.get());
                        return destination.getIdentifiers().size() != 0;
                    }
                });
    }

    public DestinationBuilder withAccounts(Iterable<Account> accounts) {
        Iterable<Destination> toAdd = FluentIterable
                .from(accounts)
                .transform(DestinationOfAccount.ACCOUNT_TO_DESTINATION);

        destinations = Iterables.concat(destinations, toAdd);

        return this;
    }

    public DestinationBuilder withAccountsPatterns(
            ListMultimap<String, TransferDestinationPattern> transferDestinationPatterns) {
        Collection<TransferDestinationPattern> allPatterns = transferDestinationPatterns.values();

        Iterable<Destination> toAdd = FluentIterable
                .from(allPatterns)
                .filter(DestinationOfPattern.TRANSFERDESTINATIONPATTERN_IS_EXACT_ACCOUNTNUMBER)
                .transform(DestinationOfPattern.TRANSFERDESTINATIONPATTERN_TO_DESTINATION);

        destinations = Iterables.concat(destinations, toAdd);

        return this;
    }

    public DestinationBuilder withDestinations(Iterable<UserTransferDestination> userTransferDestinations) {
        Iterable<? extends Destination> toAdd = FluentIterable
                .from(userTransferDestinations)
                .transform(DestinationOfUserTransferDestination.USERTRANSFERDESTINATION_TO_DESTINATION);

        destinations = Iterables.concat(destinations, toAdd);

        return this;
    }

    public DestinationBuilder withExplicitDestinations(Optional<? extends Set<AccountIdentifier>> explicitIdentifiers) {
        if (explicitIdentifiers.isPresent()) {
            Iterable<? extends Destination> toAdd = FluentIterable
                    .from(explicitIdentifiers.get())
                    .transform(DestinationOfExplicitDestination.EXPLICITDESTINATION_TO_DESTINATION);

            destinations = Iterables.concat(destinations, toAdd);
        }

        return this;
    }

    public DestinationBuilder filterExplicitTypesIfPresent(
            Optional<? extends Set<AccountIdentifier.Type>> explicitTypeFilter) {
        this.explicitTypeFilter = explicitTypeFilter;

        return this;
    }

    /**
     * Filters the destinations added before with the given list of identifier strings.
     *
     * Side-effect: Removes all identifiers from the Destination object that doesn't match the included one
     *
     * @param optionalExplicitIdentifierFilter Set with identifier strings that should be the only ones included
     */
    public DestinationBuilder filterExplicitIdentifiersIfPresent(
            final Optional<? extends Set<AccountIdentifier>> optionalExplicitIdentifierFilter) {
        if (optionalExplicitIdentifierFilter.isPresent()) {
            final List<AccountIdentifier> explicitIdentifierFilter = Lists.newArrayList(
                    optionalExplicitIdentifierFilter.get());

            destinations = Iterables.filter(destinations, destination -> {
                for (AccountIdentifier identifier : destination.getIdentifiers()) {

                    boolean isIncluded = explicitIdentifierFilter.contains(identifier);

                    // If there is a match, we only want this identifier included
                    if (isIncluded) {
                        destination.setIdentifierExclusive(identifier);
                        return true;
                    }
                }

                // No match: Exclude the destination
                return false;
            });
        }

        return this;
    }

    public DestinationBuilder removeDuplicates() {
        final List<Destination> uniqueDestinations = Lists.newArrayList();
        final List<AccountIdentifier> identifiersInUniqueDestinations = Lists.newArrayList();

        for (final Destination possibleDuplicate : destinations) {
            if (!possibleDuplicate.containsAnyOf(identifiersInUniqueDestinations)) {
                identifiersInUniqueDestinations.addAll(possibleDuplicate.getIdentifiers());
                uniqueDestinations.add(possibleDuplicate);
            }
        }

        destinations = uniqueDestinations;

        return this;
    }
}
