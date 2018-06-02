package se.tink.backend.main.providers.transfer.dto;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.main.utils.TransferUtils;
import se.tink.backend.utils.guavaimpl.comparators.AccountIdentifierComparator;
import se.tink.libraries.account.AccountIdentifierPredicate;

public abstract class Destination {
    public static final Predicate<? super Destination> HAS_IDENTIFIER = (Predicate<Destination>) destination ->
            destination.getIdentifiers().size() > 0;

    private List<AccountIdentifier> identifiers;

    private String name;

    public Destination(List<AccountIdentifier> identifiers) {
        if (identifiers != null) {
            this.identifiers = Lists.newArrayList(FluentIterable
                    .from(identifiers)
                    .filter(AccountIdentifierPredicate.IS_VALID));
        }
        else {
            this.identifiers = Lists.newArrayList();
        }
    }

    public Destination(AccountIdentifier identifier) {
        if (identifier != null && identifier.isValid()) {
            this.identifiers = Lists.newArrayList(identifier);
        }
        else {
            this.identifiers = Lists.newArrayList();
        }
    }

    public abstract Destination copyOf();

    protected Optional<AccountIdentifier> getFirstIdentifier() {
        if (identifiers.size() == 0) {
            return Optional.empty();
        } else if (identifiers.size() == 1) {
            return Optional.of(identifiers.get(0));
        }

        return Optional.of(FluentIterable.from(identifiers)
                .toSortedList(AccountIdentifierComparator.IDENTIFIERS_PRIORITIZED_HIGH_TO_LOW)
                .get(0));
    }

    public abstract Optional<AccountIdentifier> getPrimaryIdentifier();

    public List<AccountIdentifier> getIdentifiers() {
        return identifiers != null ? identifiers : Lists.<AccountIdentifier>newArrayList();
    }

    /**
     * Function to be used when filtering out valid identifiers. This overrides the previously known identifiers
     * for this destination so that one single identifier is given.
     */
    public void setIdentifierExclusive(AccountIdentifier identifierExclusive) {
        if (identifierExclusive.isValid()) {
            this.identifiers = Lists.newArrayList(identifierExclusive);
        } else {
            this.identifiers = Lists.newArrayList();
        }
    }

    /**
     * Function to be used when filtering out valid account identifier types. This overrides the previously known
     * identifiers for this destination so that only those matching the types sent in to this function remains.
     */
    public <T extends Set<AccountIdentifier.Type>> void setTypesExclusive(final T typesExclusive) {
        Preconditions.checkArgument(typesExclusive != null);

        FluentIterable<AccountIdentifier> identifiersMatchingTypesExclusive = FluentIterable.from(this.identifiers)
                .filter(identifier -> Iterables.contains(typesExclusive, identifier.getType()));

        this.identifiers = Lists.newArrayList(identifiersMatchingTypesExclusive);
    }

    public abstract Optional<AccountIdentifier> getDisplayIdentifier();

    public Optional<String> getName() {
        if (Strings.isNullOrEmpty(name)) {
            return Optional.empty();
        }

        return Optional.of(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public <T extends Destination> boolean is(Class<T> destinationType) {
        return this.getClass().equals(destinationType);
    }

    public <T extends Destination> T to(Class<T> destinationType) {
        return destinationType.cast(this);
    }

    public boolean isDefinedBy(Account account) {
        for (AccountIdentifier identifier : identifiers) {
            if (account.definedBy(identifier)) {
                return true;
            }
        }

        return false;
    }

    public void filterIdentifiersWithPatterns(Iterable<TransferDestinationPattern> accountPatterns) {
        List<AccountIdentifier> filteredIdentifiers = Lists.newArrayList();

        for (final AccountIdentifier identifier : identifiers) {
            boolean hasPatternMatch = Iterables.any(accountPatterns,
                    transferDestinationPattern -> TransferUtils.matches(transferDestinationPattern, identifier));

            if (hasPatternMatch) {
                filteredIdentifiers.add(identifier);
            }
        }

        identifiers = filteredIdentifiers;
    }

    public abstract AccountTypes getType();

    public boolean containsAnyOf(final Iterable<AccountIdentifier> accountIdentifiers) {
        for (AccountIdentifier identifier : accountIdentifiers) {
            if (this.getIdentifiers().contains(identifier)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Since we don't yet have a good lookup for banks on all kinds of destinations and that some identifiers
     * are e.g. SE-Internal we need to try get the bank name.
     *
     * TODO: Add a enum bank to the database instead to do lookups on, already in the agent class. And also
     * try to parse the bank enum from any bank name string in SE internal identifiers
     */
    public Optional<String> getDisplayBankName() {
        Optional<String> bankFromSwedishIdentifier = Optional.ofNullable(FluentIterable
                .from(getIdentifiers())
                .filter(AccountIdentifierPredicate.IS_VALID_SWEDISHIDENTIFIER)
                .transform(AccountIdentifierPredicate.SWEDISHIDENTIFIER_TO_BANKDISPLAYNAME)
                .filter(Predicates.notNull())
                .first().orNull());

        if (bankFromSwedishIdentifier.isPresent()) {
            return bankFromSwedishIdentifier;
        } else if (this.is(DestinationOfPattern.class)) {
            String bank = this.to(DestinationOfPattern.class).getBank();
            return Optional.ofNullable(bank);
        }

        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Destination that = (Destination) o;

        if (identifiers != null ? !identifiers.equals(that.identifiers) : that.identifiers != null) {
            return false;
        }
        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        int result = identifiers != null ? identifiers.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public abstract String toString();

    public MoreObjects.ToStringHelper toStringHelper(Class<? extends Destination> destinationClass) {
        return MoreObjects
                .toStringHelper(destinationClass)
                .add("identifiers", identifiers)
                .add("name", name);
    }
}
