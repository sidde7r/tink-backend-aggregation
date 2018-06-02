package se.tink.backend.common.properties;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.PropertyRepository;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.common.workers.fraud.FraudUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudRealEstateEngagementContent;
import se.tink.backend.core.Loan;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.User;
import se.tink.backend.core.property.Property;
import se.tink.backend.core.property.PropertyStatus;
import se.tink.backend.core.property.PropertyType;
import se.tink.backend.guice.annotations.Now;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.utils.StringUtils;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.backend.utils.guavaimpl.predicates.AccountPredicate;

public class RefreshPropertiesController {
    private static final LogUtils log = new LogUtils(RefreshPropertiesController.class);

    private final PropertyRepository propertyRepository;
    private final SystemServiceFactory systemServiceFactory;
    private final Provider<Date> now;
    private final FraudDetailsRepository fraudDetailsRepository;
    private final AccountRepository accountRepository;
    private final LoanDataRepository loanDataRepository;

    @Inject
    public RefreshPropertiesController(PropertyRepository propertyRepository,
            FraudDetailsRepository fraudDetailsRepository, AccountRepository accountRepository,
            LoanDataRepository loanDataRepository, SystemServiceFactory systemServiceFactory,
            @Now Provider<Date> now) {
        this.propertyRepository = propertyRepository;
        this.fraudDetailsRepository = fraudDetailsRepository;
        this.accountRepository = accountRepository;
        this.loanDataRepository = loanDataRepository;
        this.systemServiceFactory = systemServiceFactory;
        this.now = now;
    }

    public List<Property> refresh(User user) {
        log.info(user.getId(), "Refreshing properties");

        FraudAddressContent address = getAddress(user).orElse(null);
        FraudRealEstateEngagementContent realEstateEngagement = getRealEstateEngagement(user).orElse(null);

        List<Property> properties = Lists.newArrayList(
                Iterables.filter(propertyRepository.findByUserId(user.getId()), Predicates.ACTIVE_PROPERTY));
        Set<String> loanAccountIds = getLoanAccountIds(user);

        boolean shouldCreateRegisteredAddressProperty = true;
        boolean shouldGenerateStatistics = false;

        for (Property property : properties) {
            if (property.isRegisteredAddress()) {
                if (address == null) {
                    log.info(user.getId(), "The user has no address anymore; expiring the registered address property");

                    property.setStatus(PropertyStatus.EXPIRED);
                    shouldGenerateStatistics = true;
                    continue;
                }

                // Check if we already have a property for the user's current registered address.

                if (Objects.equals(property.getAddress(), address.getAddress()) &&
                        Objects.equals(property.getPostalCode(), address.getPostalcode()) &&
                        Objects.equals(property.getCommunity(), address.getCommunity()) &&
                        Objects.equals(property.getCity(), address.getCity())) {
                    shouldCreateRegisteredAddressProperty = false;

                    if (!property.isUserModifiedLoanAccountIds()) {
                        if (!loanAccountIds.equals(property.getLoanAccountIds())) {
                            property.setLoanAccountIds(loanAccountIds);
                            shouldGenerateStatistics = true;
                        }
                    }
                } else {
                    // If the registered address does not equal that of the property, it means that the user
                    // has moved and that we should expire the property (and subsequently generate a new one).

                    log.info(user.getId(), "The user has moved; expiring the registered address property");

                    property.setStatus(PropertyStatus.EXPIRED);
                    shouldCreateRegisteredAddressProperty = true;
                    shouldGenerateStatistics = true;
                }
            }

            // Refresh the estimated value of the property if we should.

            if (property.getStatus() == PropertyStatus.ACTIVE) {
                if (refreshEstimatedValueIfNecessary(user, property)) {
                    shouldGenerateStatistics = true;
                }
            }
        }

        // Generate the default property for a user's registered address, if we have the address
        // and the user has some mortgage accounts.

        if (shouldCreateRegisteredAddressProperty) {
            if (address == null) {
                log.info(user.getId(), "Can't create registered address property because missing address");
            } else {
                log.info(user.getId(), "Creating a registered address property");

                properties.add(createRegisteredAddressProperty(user, address, realEstateEngagement));
                shouldGenerateStatistics = true;
            }
        }

        propertyRepository.save(properties);

        // Regenerate statistics for the user. SIMPLE used since we don't need/want to re-trigger property refresh.

        if (shouldGenerateStatistics) {
            systemServiceFactory.getProcessService()
                    .generateStatisticsAndActivitiesWithoutNotifications(user.getId(), StatisticMode.SIMPLE);
        }

        log.info(user.getId(), "Refreshed properties");

        return properties;
    }

    private boolean refreshEstimatedValueIfNecessary(User user, Property property) {
        return false;
    }

    private Property createRegisteredAddressProperty(User user, FraudAddressContent address,
            FraudRealEstateEngagementContent realEstateEngagement) {
        Property property = new Property();

        property.setCreated(now.get());
        property.setUserId(user.getId());
        property.setId(StringUtils.generateUUID());
        property.setAddress(address.getAddress());
        property.setCity(address.getCity());
        property.setPostalCode(address.getPostalcode());
        property.setCommunity(address.getCommunity());
        property.setRegisteredAddress(true);
        property.setStatus(PropertyStatus.ACTIVE);
        property.setLoanAccountIds(getLoanAccountIds(user));

        if (realEstateEngagement != null) {
            property.setType(PropertyType.HOUSE);
        } else {
            property.setType(PropertyType.APARTMENT);
        }

        return property;
    }

    private Set<String> getLoanAccountIds(User user) {
        return accountRepository.findByUserId(user.getId()).stream()
                .filter(AccountPredicate.IS_NOT_EXCLUDED::apply)
                .filter(AccountPredicate.IS_NOT_CLOSED::apply)
                .filter(Predicates.or(AccountPredicate.IS_LOAN, AccountPredicate.IS_MORTGAGE)::apply)
                .filter(this::accountWithValidMortgage)
                .map(Account::getId)
                .collect(Collectors.toSet());
    }

    private boolean accountWithValidMortgage(Account account) {
        Loan loan = loanDataRepository.findMostRecentOneByAccountId(account.getId());

        return loan != null && Objects.equals(loan.getType(), Loan.Type.MORTGAGE)
                && loan.getBalance() != null && loan.getId() != null &&
                loan.getInterest() != null;
    }

    private Optional<FraudAddressContent> getAddress(User user) {
        Optional<FraudDetails> addressDetails = FraudUtils.getLatestFraudDetailsOfType(fraudDetailsRepository, user,
                FraudDetailsContentType.ADDRESS);

        if (addressDetails.isPresent()) {
            return Optional.of((FraudAddressContent) addressDetails.get().getContent());
        } else {
            return Optional.empty();
        }
    }

    private Optional<FraudRealEstateEngagementContent> getRealEstateEngagement(User user) {
        Optional<FraudDetails> realEstateDetails = FraudUtils.getLatestFraudDetailsOfType(fraudDetailsRepository, user,
                FraudDetailsContentType.REAL_ESTATE_ENGAGEMENT);

        if (realEstateDetails.isPresent()) {
            return Optional.of((FraudRealEstateEngagementContent) realEstateDetails.get().getContent());
        } else {
            return Optional.empty();
        }
    }
}
