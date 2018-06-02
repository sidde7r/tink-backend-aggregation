package se.tink.backend.main.providers.transfer;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ListMultimap;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.account.UserTransferDestination;
import se.tink.backend.main.providers.transfer.dto.AccountDestinations;
import se.tink.backend.main.providers.transfer.dto.Destination;
import se.tink.backend.main.providers.transfer.dto.utils.DestinationBuilder;
import se.tink.backend.utils.ProviderDisplayNameFinder;
import se.tink.backend.utils.ProviderImageMap;
import se.tink.backend.utils.guavaimpl.predicates.AccountPredicate;
import se.tink.backend.utils.guavaimpl.predicates.OnlyIncludeAccountsWhosCredentialsAreActive;
import se.tink.backend.utils.guavaimpl.predicates.OnlyIncludeAccountsWhosProviderHasCapability;

public class TransferSourceAccountProviderImpl implements TransferSourceAccountProvider {
    private final TransferDestinationPatternProvider destinationPatternProvider;
    private final ProviderImageMap providerImages;
    private final AccountRepository accountRepository;
    private final CredentialsRepository credentialsRepository;
    private final UserTransferDestinationProvider userTransferDestinationProvider;
    private final ProviderDao providerDao;

    @Inject
    public TransferSourceAccountProviderImpl(
            TransferDestinationPatternProvider destinationPatternProvider,
            ProviderImageMap providerImages,
            AccountRepository accountRepository,
            CredentialsRepository credentialsRepository,
            UserTransferDestinationProvider userTransferDestinationProvider,
            ProviderDao providerDao) {

        this.destinationPatternProvider = destinationPatternProvider;
        this.providerImages = providerImages;
        this.accountRepository = accountRepository;
        this.credentialsRepository = credentialsRepository;
        this.userTransferDestinationProvider = userTransferDestinationProvider;
        this.providerDao = providerDao;
    }

    @Override
    public List<Account> getSourceAccountsExplicitTypes(User user, Set<AccountIdentifier.Type> explicitTypes) {
        return getSourceAccounts(user, Optional.of(explicitTypes), Optional.empty());
    }

    @Override
    public List<Account> getSourceAccountsAll(User user) {
        return getSourceAccounts(user,
                Optional.empty(), Optional.empty());
    }

    @Override
    public List<Account> getSourceAccounts(User user,
            Optional<? extends Set<AccountIdentifier.Type>> explicitTypes,
            Optional<? extends Set<AccountIdentifier>> explicitIdentifiers) {
        ProviderDisplayNameFinder displayNameFinder = createDisplayNameFinder(user);

        List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId());

        // Places to look for known destinations
        Iterable<Account> accounts = getEnabledAccountsWithIdentifiers(user, credentials);
        List<UserTransferDestination> userTransferDestinations = getUserTransferDestinations(user);
        ListMultimap<String, TransferDestinationPattern> patternsByAccountId = getPatterns(user);

        Iterable<Destination> allKnownDestinations = DestinationBuilder.create()
                .withAccounts(accounts)
                .withDestinations(userTransferDestinations)
                .withAccountsPatterns(patternsByAccountId)
                .withExplicitDestinations(explicitIdentifiers)
                .filterExplicitTypesIfPresent(explicitTypes)
                .filterExplicitIdentifiersIfPresent(explicitIdentifiers)
                .removeDuplicates()
                .build();

        Iterable<AccountDestinations> sourceAccountDestinations = FluentIterable
                .from(accounts)
                .filter(new OnlyIncludeAccountsWhosProviderHasCapability(Provider.Capability.TRANSFERS, credentials,
                        providerDao.getProvidersByName()))
                .transform(new AccountDestinations.FromAccountTransform(patternsByAccountId, allKnownDestinations));

        return FluentIterable
                .from(sourceAccountDestinations)
                .transform(new AccountDestinations.ToAccountTransform(displayNameFinder, providerImages,
                        explicitIdentifiers.isPresent()))
                .filter(AccountPredicate.HAS_TRANSFER_DESTINATIONS)
                .toList();
    }

    private ProviderDisplayNameFinder createDisplayNameFinder(User user) {
        List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId());
        return new ProviderDisplayNameFinder(providerDao.getProvidersByName(), credentials);
    }

    private Iterable<Account> getEnabledAccountsWithIdentifiers(User user, List<Credentials> userCredentials) {
        FluentIterable<Account> accounts = FluentIterable.from(getAccounts(user));
        return accounts
                .filter(new OnlyIncludeAccountsWhosCredentialsAreActive(userCredentials))
                .filter(AccountPredicate.HAS_IDENTIFIER)
                .filter(AccountPredicate.IS_NOT_EXCLUDED);
    }

    private Iterable<Account> getAccounts(User user) {
        return accountRepository.findByUserId(user.getId());
    }

    private List<UserTransferDestination> getUserTransferDestinations(User user) {
        return userTransferDestinationProvider.getDestinations(user);
    }

    private ListMultimap<String, TransferDestinationPattern> getPatterns(User user) {
        return destinationPatternProvider.getDestinationPatternsByAccountId(user);
    }
}
