package se.tink.backend.main.controllers;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.ws.rs.Path;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.utils.SuggestProviderSearcher;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.DeviceConfiguration;
import se.tink.backend.core.Field;
import se.tink.backend.core.Market;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderStatuses;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.SearchResult;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.rpc.GetProvidersByDeviceCommand;
import se.tink.backend.utils.ProviderImageMap;
import se.tink.backend.utils.StringUtils;
import se.tink.backend.utils.guavaimpl.Predicates;

@Path("/api/v1/providers")
public class ProviderServiceController {
    private static final ImmutableSet<String> DEFAULT_LINK_PROVIDERS_FOR_SE = ImmutableSet.of(
            "handelsbanken-bankid", "danskebank", "seb-bankid", "nordea-bankid", "lansforsakringar-bankid",
            "swedbank-bankid", "savingsbank-bankid");

    private static final Function<Provider, Provider> CLEAN_PROVIDER = provider -> {
        Provider providerClone = provider.clone();

        providerClone.setClassName(null);
        providerClone.setPayload(null);

        providerClone.setFields(provider.getFields().stream().filter(Field::isExposed).collect(Collectors.toList()));

        // Make the client not show the provider when adding new ones if it's obsolete.

        if (provider.getStatus() == ProviderStatuses.OBSOLETE) {
            providerClone.setStatus(ProviderStatuses.TEMPORARY_DISABLED);
        }

        return providerClone;
    };

    private final CredentialsRepository credentialsRepository;
    private final DeviceServiceController deviceServiceController;
    private final SuggestProviderSearcher suggestProviderSearcher;
    private final TransactionServiceController transactionServiceController;
    private final ProviderDao providerDao;
    private final Supplier<ProviderImageMap> providerImageMapSupplier;

    @Inject
    public ProviderServiceController(CredentialsRepository credentialsRepository,
            DeviceServiceController deviceServiceController,
            SuggestProviderSearcher suggestProviderSearcher,
            TransactionServiceController transactionServiceController,
            ProviderDao providerDao,
            Supplier<ProviderImageMap> providerImageMapSupplier) {
        this.credentialsRepository = credentialsRepository;
        this.deviceServiceController = deviceServiceController;
        this.suggestProviderSearcher = suggestProviderSearcher;
        this.transactionServiceController = transactionServiceController;
        this.providerDao = providerDao;
        this.providerImageMapSupplier = providerImageMapSupplier;
    }

    public List<Provider> list(String userId, String market, Provider.Capability capability) {
        List<Provider> providers = list(userId, market);

        if (Objects.isNull(capability)) {
            return providers;
        }

        return providers.stream()
                .filter(p -> p.getCapabilities().contains(capability))
                .collect(Collectors.toList());
    }

    public List<Provider> list(String userId, String market) {

        final Set<String> providerNamesForUser = getUserProviderNames(userId);

        // Return all providers belonging to the user's credentials or where status != DISABLED.
        return list(market, providersNamesIn(providerNamesForUser)
                .or(Predicates.providersOfStatus(ProviderStatuses.DISABLED).negate()));
    }

    private List<Provider> list(String market, Predicate<Provider> predicate) {
        return providerDao.getProviders().stream()
                .filter(Predicates.providersByMarket(market))
                .filter(predicate)
                .map(CLEAN_PROVIDER)
                .peek(providerImageMapSupplier.get()::populateImagesForProvider)
                .collect(Collectors.toList());
    }

    public List<Provider> listByMarket(final Optional<String> providersList, final String market) {
        /* THIS IS AN METHOD FOR UNAUTHORIZED CLIENTS */
        return list(market, providersNamesIn(getProviderNames(providersList)));
    }

    public List<Provider> list(UUID deviceId, String market) {
        DeviceConfiguration deviceConfiguration = deviceServiceController.getDeviceConfiguration(deviceId);
        boolean isMortgageOnboarding = deviceConfiguration.getFeatureFlags() != null
                && deviceConfiguration.getFeatureFlags().contains(FeatureFlags.ONBOARDING_MORTGAGE);

        return list(market, Predicates.providersOfStatus(ProviderStatuses.DISABLED).negate()).stream()
                .peek(provider -> {
                    if (Objects.equals(market, Market.Code.SE.name())) {
                        if (isMortgageOnboarding) {
                            // Set true if provider provides mortgage, and false - if not. Using it to suggest providers
                            // for onboarding for users from mortgage campaign
                            provider.setPopular(
                                    provider.getCapabilities().contains(Provider.Capability.MORTGAGE_AGGREGATION));
                        } else {
                            // We set this to true if the provider is part of a list of commonly used banks for
                            // transactional accounts. (see: SE_TRANSACTIONAL_BANKS) We are also overriding database
                            // values as an interim solution until we have something more sustainable in place for
                            // dynamic changes.
                            provider.setPopular(SE_TRANSACTIONAL_BANKS.contains(provider.getName()));
                        }
                    }
                })
                .collect(Collectors.toList());
    }

    public List<Provider> list(GetProvidersByDeviceCommand command) {
        return list(command.getDeviceId(), command.getMarket());
    }

    private static final ImmutableList<String> SE_TRANSACTIONAL_BANKS = ImmutableList.of(
            "alandsbanken",
            "danskebank",
            "danskebank-bankid",
            "handelsbanken",
            "handelsbanken-bankid",
            "icabanken-bankid",
            "lansforsakringar",
            "lansforsakringar-bankid",
            "nordea-bankid",
            "savingsbank-bankid",
            "savingsbank-bankid-youth",
            "seb-bankid",
            "skandiabanken-bankid",
            "skandiabanken-ssn-bankid",
            "sparbankensyd-bankid",
            "swedbank-bankid",
            "swedbank-bankid-youth"
            );

    public Set<Provider> suggest(User user) {
        Set<String> userProviders = getUserProviderNames(user.getId());
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setLimit(Integer.MAX_VALUE);
        return transactionServiceController.searchTransactions(user, searchQuery).getResults().stream()
                .map(SearchResult::getTransaction)
                .map(suggestProviderSearcher::suggest)
                .filter(providers -> providers.stream().map(Provider::getName).noneMatch(userProviders::contains))
                .flatMap(Collection::stream)
                .peek(providerImageMapSupplier.get()::populateImagesForProvider)
                .collect(Collectors.toSet());
    }

    private ImmutableSet<String> getProviderNames(Optional<String> providersList) {
        return providersList.map(string -> ImmutableSet.copyOf(StringUtils.parseCSV(string)))
                .orElse(DEFAULT_LINK_PROVIDERS_FOR_SE);
    }

    private Set<String> getUserProviderNames(String userId) {
        return credentialsRepository.findAllByUserId(userId).stream()
                .map(Credentials::getProviderName)
                .collect(Collectors.toSet());
    }

    private Predicate<Provider> providersNamesIn(final Set<String> providerNamesToInclude) {
        return provider -> providerNamesToInclude.contains(provider.getName());
    }
}
