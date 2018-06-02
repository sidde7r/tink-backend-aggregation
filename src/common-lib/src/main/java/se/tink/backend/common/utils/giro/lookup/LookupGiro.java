package se.tink.backend.common.utils.giro.lookup;

import com.google.common.base.Objects;
import java.util.Optional;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import se.tink.backend.common.repository.cassandra.GiroRepository;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.backend.core.giros.Giro;

public class LookupGiro {
    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER = new DefaultAccountIdentifierFormatter();

    private GiroRepository giroRepository;
    private final BankGiroCrawler bankGiroCrawler;
    private final PlusGiroCrawler plusGiroCrawler;

    public LookupGiro(GiroRepository giroRepository, BankGiroCrawler bankGiroCrawler, PlusGiroCrawler plusGiroCrawler) {
        this.giroRepository = giroRepository;
        this.bankGiroCrawler = bankGiroCrawler;
        this.plusGiroCrawler = plusGiroCrawler;
    }

    public List<AccountIdentifier> lookup(final String accountNumber) throws LookupGiroException {
        Map<AccountIdentifier.Type, AccountIdentifier> lookupIdentifiers = GiroParser
                .createPossibleIdentifiersFor(accountNumber);

        ensureHasPossibleIdentifiers(lookupIdentifiers);

        List<AccountIdentifier> foundIdentifiers = find(lookupIdentifiers.values());
        for (AccountIdentifier foundIdentifier : foundIdentifiers) {
            lookupIdentifiers.put(foundIdentifier.getType(), foundIdentifier);
        }

        return Lists.newArrayList(lookupIdentifiers.values());
    }

    private void ensureHasPossibleIdentifiers(Map<AccountIdentifier.Type, AccountIdentifier> possibleIdentifiers)
            throws LookupGiroException {
        if (possibleIdentifiers.isEmpty()) {
            throw new LookupGiroException(LookupGiroException.Type.INVALID_FORMAT);
        }
    }

    private List<AccountIdentifier> find(Collection<AccountIdentifier> identifiers) {
        List<AccountIdentifier> foundIdentifiers = Lists.newArrayList();

        for (AccountIdentifier identifier : identifiers) {
            Optional<AccountIdentifier> foundIdentifier = find(identifier);

            if (foundIdentifier.isPresent()) {
                foundIdentifiers.add(foundIdentifier.get());
            }
        }

        return foundIdentifiers;
    }

    private Optional<AccountIdentifier> find(AccountIdentifier identifier) {
        Optional<AccountIdentifier> existingGiro = giroRepository
                .getIdentifierFor(identifier.getIdentifier(DEFAULT_FORMATTER), identifier.getType());

        return existingGiro.isPresent() ? existingGiro : crawl(identifier);
    }

    private Optional<AccountIdentifier> crawl(AccountIdentifier identifier) {
        Optional<AccountIdentifier> foundIdentifier;

        if (Objects.equal(identifier.getType(), AccountIdentifier.Type.SE_BG)) {
            foundIdentifier = bankGiroCrawler.find(identifier.getIdentifier(DEFAULT_FORMATTER));
        } else {
            foundIdentifier = plusGiroCrawler.find(identifier.getIdentifier(DEFAULT_FORMATTER));
        }

        if (!foundIdentifier.isPresent()) {
            return Optional.empty();
        }

        save(foundIdentifier.get());
        return foundIdentifier;
    }

    private void save(AccountIdentifier identifier) {
        Giro giro = Giro.fromIdentifier(identifier);
        giroRepository.save(giro);
    }
}
