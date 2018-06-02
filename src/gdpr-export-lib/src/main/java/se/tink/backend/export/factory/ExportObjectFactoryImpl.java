package se.tink.backend.export.factory;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.dao.ApplicationDAO;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.AccountBalanceHistoryRepository;
import se.tink.backend.common.repository.cassandra.ApplicationEventRepository;
import se.tink.backend.common.repository.cassandra.DAO.LoanDAO;
import se.tink.backend.common.repository.cassandra.DocumentRepository;
import se.tink.backend.common.repository.cassandra.InstrumentHistoryRepository;
import se.tink.backend.common.repository.cassandra.InstrumentRepository;
import se.tink.backend.common.repository.cassandra.PortfolioHistoryRepository;
import se.tink.backend.common.repository.cassandra.PortfolioRepository;
import se.tink.backend.common.repository.cassandra.SignableOperationRepository;
import se.tink.backend.common.repository.cassandra.TransferEventRepository;
import se.tink.backend.common.repository.cassandra.TransferRepository;
import se.tink.backend.common.repository.cassandra.UserLocationRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.BooliEstimateRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.DeletedUserRepository;
import se.tink.backend.common.repository.mysql.main.FollowItemRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsContentRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.common.repository.mysql.main.PropertyRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserDeviceRepository;
import se.tink.backend.common.repository.mysql.main.UserEventRepository;
import se.tink.backend.common.repository.mysql.main.UserFacebookFriendRepository;
import se.tink.backend.common.repository.mysql.main.UserFacebookProfileRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.consent.repository.cassandra.UserConsentRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountBalance;
import se.tink.backend.core.DeletedUser;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudStatus;
import se.tink.backend.core.Instrument;
import se.tink.backend.core.LoanDetails;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.NotificationSettings;
import se.tink.backend.core.Portfolio;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.core.UserFacebookProfile;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.enums.SignableOperationTypes;
import se.tink.backend.core.follow.ExpensesFollowCriteria;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.core.follow.SavingsFollowCriteria;
import se.tink.backend.core.follow.SearchFollowCriteria;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.TransferEvent;
import se.tink.backend.export.helper.ExportStringFormatter;
import se.tink.backend.export.helper.UserNotFoundException;
import se.tink.backend.export.model.AccountHistory;
import se.tink.backend.export.model.Accounts;
import se.tink.backend.export.model.ApplicationEvents;
import se.tink.backend.export.model.Applications;
import se.tink.backend.export.model.Booleans;
import se.tink.backend.export.model.Budgets;
import se.tink.backend.export.model.Consents;
import se.tink.backend.export.model.Credentials;
import se.tink.backend.export.model.Documents;
import se.tink.backend.export.model.FacebookDetails;
import se.tink.backend.export.model.FraudDetails;
import se.tink.backend.export.model.InstrumentHistory;
import se.tink.backend.export.model.Instruments;
import se.tink.backend.export.model.Loans;
import se.tink.backend.export.model.PortfolioHistory;
import se.tink.backend.export.model.Portfolios;
import se.tink.backend.export.model.Properties;
import se.tink.backend.export.model.PropertyEstimates;
import se.tink.backend.export.model.SavingsGoals;
import se.tink.backend.export.model.Transactions;
import se.tink.backend.export.model.Transfers;
import se.tink.backend.export.model.UserDetails;
import se.tink.backend.export.model.UserDevices;
import se.tink.backend.export.model.UserEvents;
import se.tink.backend.export.model.UserLocations;
import se.tink.backend.export.model.submodels.ExportAccount;
import se.tink.backend.export.model.submodels.ExportAccountEvent;
import se.tink.backend.export.model.submodels.ExportApplication;
import se.tink.backend.export.model.submodels.ExportApplicationEvent;
import se.tink.backend.export.model.submodels.ExportBudget;
import se.tink.backend.export.model.submodels.ExportConsent;
import se.tink.backend.export.model.submodels.ExportCredential;
import se.tink.backend.export.model.submodels.ExportDevice;
import se.tink.backend.export.model.submodels.ExportDocument;
import se.tink.backend.export.model.submodels.ExportEvent;
import se.tink.backend.export.model.submodels.ExportFacebookFriend;
import se.tink.backend.export.model.submodels.ExportFraud;
import se.tink.backend.export.model.submodels.ExportInstrument;
import se.tink.backend.export.model.submodels.ExportInstrumentEvent;
import se.tink.backend.export.model.submodels.ExportLoan;
import se.tink.backend.export.model.submodels.ExportLocation;
import se.tink.backend.export.model.submodels.ExportNotificationSettings;
import se.tink.backend.export.model.submodels.ExportPortfolio;
import se.tink.backend.export.model.submodels.ExportPortfolioEvent;
import se.tink.backend.export.model.submodels.ExportProperty;
import se.tink.backend.export.model.submodels.ExportPropertyEstimate;
import se.tink.backend.export.model.submodels.ExportSavingsGoal;
import se.tink.backend.export.model.submodels.ExportTransaction;
import se.tink.backend.export.model.submodels.ExportTransfer;
import se.tink.backend.export.model.submodels.ExportUserSettings;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class ExportObjectFactoryImpl implements ExportObjectFactory {

    private final UserRepository userRepository;
    private final DeletedUserRepository deletedUserRepository;
    private final FraudDetailsContentRepository fraudDetailsContentRepository;
    private final UserFacebookProfileRepository userFacebookProfileRepository;
    private final UserFacebookFriendRepository userFacebookFriendRepository;
    private final FraudDetailsRepository fraudDetailsRepository;
    private final UserConsentRepository userConsentRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final UserEventRepository userEventRepository;
    private final UserLocationRepository userLocationRepository;
    private final ApplicationDAO applicationDAO;
    private final ApplicationEventRepository applicationEventRepository;
    private final DocumentRepository documentRepository;
    private final CredentialsRepository credentialsRepository;
    private final ProviderRepository providerRepository;
    private final PropertyRepository propertyRepository;
    private final AccountRepository accountRepository;
    private final LoanDAO loanDAO;
    private final AccountBalanceHistoryRepository accountBalanceHistoryRepository;
    private final TransactionDao transactionDao;
    private final MerchantRepository merchantRepository;
    private final TransferRepository transferRepository;
    private final TransferEventRepository transferEventRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioHistoryRepository portfolioHistoryRepository;
    private final InstrumentRepository instrumentRepository;
    private final InstrumentHistoryRepository instrumentHistoryRepository;
    private final FollowItemRepository followItemRepository;
    private final SignableOperationRepository signableOperationRepository;
    private final AggregationControllerCommonClient aggregationControllerClient;
    private final boolean isProvidersOnAggregation;
    private final BooliEstimateRepository booliEstimateRepository;
    private final CategoryRepository categoryRepository;

    private static final LogUtils log = new LogUtils(ExportObjectFactory.class);

    @Inject
    public ExportObjectFactoryImpl(UserRepository userRepository,
            DeletedUserRepository deletedUserRepository,
            FraudDetailsContentRepository fraudDetailsContentRepository,
            UserFacebookProfileRepository userFacebookProfileRepository,
            UserFacebookFriendRepository userFacebookFriendRepository,
            FraudDetailsRepository fraudDetailsRepository,
            UserConsentRepository userConsentRepository,
            UserDeviceRepository userDeviceRepository,
            UserEventRepository userEventRepository,
            UserLocationRepository userLocationRepository, ApplicationDAO applicationDAO,
            ApplicationEventRepository applicationEventRepository,
            DocumentRepository documentRepository,
            CredentialsRepository credentialsRepository,
            ProviderRepository providerRepository,
            PropertyRepository propertyRepository,
            AccountRepository accountRepository, LoanDAO loanDAO,
            AccountBalanceHistoryRepository accountBalanceHistoryRepository,
            TransactionDao transactionDao,
            MerchantRepository merchantRepository,
            TransferRepository transferRepository,
            TransferEventRepository transferEventRepository,
            PortfolioRepository portfolioRepository,
            PortfolioHistoryRepository portfolioHistoryRepository,
            InstrumentRepository instrumentRepository,
            InstrumentHistoryRepository instrumentHistoryRepository,
            FollowItemRepository followItemRepository,
            SignableOperationRepository signableOperationRepository,
            AggregationControllerCommonClient aggregationControllerClient,
            BooliEstimateRepository booliEstimateRepository,
            @Named("isProvidersOnAggregation") boolean isProvidersOnAggregation,
            CategoryRepository categoryRepository) {

        this.userRepository = userRepository;
        this.deletedUserRepository = deletedUserRepository;
        this.fraudDetailsContentRepository = fraudDetailsContentRepository;
        this.userFacebookProfileRepository = userFacebookProfileRepository;
        this.userFacebookFriendRepository = userFacebookFriendRepository;
        this.fraudDetailsRepository = fraudDetailsRepository;
        this.userConsentRepository = userConsentRepository;
        this.userDeviceRepository = userDeviceRepository;
        this.userEventRepository = userEventRepository;
        this.userLocationRepository = userLocationRepository;
        this.applicationDAO = applicationDAO;
        this.applicationEventRepository = applicationEventRepository;
        this.documentRepository = documentRepository;
        this.credentialsRepository = credentialsRepository;
        this.providerRepository = providerRepository;
        this.propertyRepository = propertyRepository;
        this.accountRepository = accountRepository;
        this.loanDAO = loanDAO;
        this.accountBalanceHistoryRepository = accountBalanceHistoryRepository;
        this.transactionDao = transactionDao;
        this.merchantRepository = merchantRepository;
        this.transferRepository = transferRepository;
        this.transferEventRepository = transferEventRepository;
        this.portfolioRepository = portfolioRepository;
        this.portfolioHistoryRepository = portfolioHistoryRepository;
        this.instrumentRepository = instrumentRepository;
        this.instrumentHistoryRepository = instrumentHistoryRepository;
        this.followItemRepository = followItemRepository;
        this.signableOperationRepository = signableOperationRepository;
        this.aggregationControllerClient = aggregationControllerClient;
        this.isProvidersOnAggregation = isProvidersOnAggregation;
        this.booliEstimateRepository = booliEstimateRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Booleans createExportBooleans(String userId) {
        return new Booleans(false, false);
    }

    @Override
    public UserDetails createUserDetails(String userId) {
        User user = userRepository.findOne(userId);
        DeletedUser deleteUser = deletedUserRepository.findOneByUserId(userId);

        Date deleted = null;
        String comment = "";
        List<String> reasons = Lists.newArrayList();
        if (deleteUser != null) {
            comment = deleteUser.getComment();
            deleted = deleteUser.getInserted();
            reasons = deleteUser.getReasons();
        }

        UserProfile userProfile = user.getProfile();

        String userProfileName = null;
        String userGender = null;
        String userMarket = null;
        String userCurrency = null;
        ExportNotificationSettings notificationSettings = null;
        if (userProfile != null) {
            userProfileName = userProfile.getName();
            userGender = userProfile.getGender();
            userMarket = userProfile.getMarket();
            userCurrency = userProfile.getCurrency();

            NotificationSettings settings = user.getProfile().getNotificationSettings();
            notificationSettings = new ExportNotificationSettings(
                    settings.getBalance(),
                    settings.getBudget(),
                    settings.getDoubleCharge(),
                    settings.getIncome(),
                    settings.getLargeExpense(),
                    settings.getSummaryMonthly(),
                    settings.getSummaryWeekly(),
                    settings.getTransaction(),
                    settings.getUnusualAccount(),
                    settings.getUnusualCategory(),
                    settings.getEinvoices(),
                    settings.isFraud(),
                    settings.getLeftToSpend(),
                    settings.getLoanUpdate());
        }

        return new UserDetails(
                format(userProfileName),
                stringOrEmpty(user.getNationalId()),
                format(userGender),
                stringOrEmpty(userMarket),
                stringOrEmpty(userCurrency),
                stringOrEmpty(user.getUsername()),
                user.getCreated(),
                comment,
                deleted,
                reasons,
                new ExportUserSettings(notificationSettings),
                Collections.emptyList()
        );
    }

    @Override
    public FacebookDetails createFacebookDetails(String userId) {
        List<ExportFacebookFriend> friendsList = userFacebookFriendRepository.findByUserId(userId)
                .stream().map(friend -> new ExportFacebookFriend(
                        format(friend.getName())
                )).collect(Collectors.toList());
        UserFacebookProfile fbProfile = userFacebookProfileRepository.findByUserId(userId);
        if (fbProfile == null) {
            return null;
        }

        return new FacebookDetails(
                format(fbProfile.getFirstName()),
                format(fbProfile.getLastName()),
                fbProfile.getBirthday(),
                stringOrEmpty(fbProfile.getEmail()),
                format(fbProfile.getLocationName()),
                format(fbProfile.getState().toString()),
                friendsList);
    }

    @Override
    public FraudDetails createFraudDetails(String userId) {
        List<ExportFraud> exportFrauds = fraudDetailsRepository.findAllByUserId(userId).stream()
                .filter(fraudDetails -> !Objects.equals(fraudDetails.getStatus(), FraudStatus.EMPTY))
                .map(details -> {
                    details.getContent();   // Only done to set serialized Protobuf string
                    String detailsType = objectMessageCall(details.getType(), FraudDetailsContentType::toString);
                    String detailsStatus = objectMessageCall(details.getStatus(), FraudStatus::toString);
                    return new ExportFraud(
                            format(detailsType),
                            format(detailsStatus),
                            stringOrEmpty(details.getContentSerializedForProtobuf()),
                            details.getDate(),
                            details.getUpdated());
                }).collect(Collectors.toList());

        return new FraudDetails(exportFrauds);
    }

    @Override
    public Consents createUserConsents(String userId) {
        List<ExportConsent> consents = userConsentRepository.findAllByUserId(UUIDUtils.fromTinkUUID(userId))
                .stream().map(consent -> new ExportConsent(
                        stringOrEmpty(consent.getVersion()),
                        format(consent.getAction()),
                        format(consent.getLocale()),
                        consent.getTimestamp()))
                .collect(Collectors.toList());
        return new Consents(consents);
    }

    @Override
    public UserDevices createUserDevices(String userId) {
        List<ExportDevice> userDevices = userDeviceRepository.findByUserId(userId)
                .stream().map(device -> new ExportDevice(
                        device.getUpdated(),
                        device.getStatus(),
                        device.getUserAgent(),
                        device.getPayload()
                        )).collect(Collectors.toList());
        return new UserDevices(userDevices);
    }

    @Override
    public UserEvents createUserEvents(String userId) {
        List<ExportEvent> events = userEventRepository
                .findAllByUserIdOrderByDateDesc(userId, new PageRequest(0, Integer.MAX_VALUE))
                .stream().map(event -> new ExportEvent(
                        event.getDate(),
                        format(event.getType().toString()),
                        stringOrEmpty(event.getRemoteAddress())
                )).collect(Collectors.toList());
        return new UserEvents(events);
    }

    @Override
    public UserLocations createUserLocations(String userId) {
        List<ExportLocation> locations = userLocationRepository.findAllByUserId(userId)
                .stream().map(location -> new ExportLocation(
                        location.getDate(),
                        location.getLatitude(),
                        location.getLongitude()
                )).collect(Collectors.toList());

        return new UserLocations(locations);
    }

    @Override
    public Applications createApplications(String userId) {
        List<ExportApplication> applications = applicationDAO.findByUserId(UUIDUtils.fromTinkUUID(userId))
                .stream().map(application -> new ExportApplication(
                        format(application.getType().toString()),
                        format(application.getStatus().getMessage()),
                        application.getCreated(),
                        application.getStatus().getUpdated()
                )).collect(Collectors.toList());
        return new Applications(applications);
    }

    @Override
    public ApplicationEvents createApplicationEvents(String userId) {
        List<ExportApplicationEvent> events = applicationEventRepository.findAllByUserId(UUIDUtils.fromTinkUUID(userId))
                .stream().map(event -> new ExportApplicationEvent(
                        event.getApplicationUpdated(),
                        format(event.getApplicationType().toString()),
                        format(event.getApplicationStatus().toString())
                )).collect(Collectors.toList());
        return new ApplicationEvents(events);
    }

    @Override
    public Documents createDocuments(String userId) {
        List<ExportDocument> documents = documentRepository.findAllByUserId(UUIDUtils.fromTinkUUID(userId))
                .stream().map(compressedDocument ->
                        new ExportDocument(
                                compressedDocument.getMimeType()
                        )).collect(Collectors.toList());
        return new Documents(documents);
    }

    /*
     * Financial data:
     */

    @Override
    public Properties createProperties(String userId) {
        List<ExportProperty> properties = propertyRepository.findByUserId(userId)
                .stream().map(property -> new ExportProperty(
                        format(property.getAddress()),
                        format(property.getCity()),
                        format(property.getCommunity()),
                        format(property.getPostalCode()),
                        property.getLongitude(),
                        property.getLatitude(),
                        format(property.getType().toString()),
                        format(property.getStatus().toString()),
                        format(String.valueOf(property.isRegisteredAddress())),
                        property.getCreated(),
                        property.getNumberOfRooms(),
                        property.getNumberOfSquareMeters(),
                        property.getMostRecentValuation()
                )).collect(Collectors.toList());
        return new Properties(properties);
    }

    @Override
    public PropertyEstimates createPropertyEstimates(String userId) {
        List<ExportPropertyEstimate> estimates = propertyRepository.findByUserId(userId).stream()
                .filter(property -> Objects.nonNull(property.getBooliEstimateId()))
                .map(property -> booliEstimateRepository.findOne(property.getBooliEstimateId()))
                .filter(Objects::nonNull)
                .map(estimate -> new ExportPropertyEstimate(
                        estimate.getAdditionalAndLivingArea(),
                        estimate.getAdditionalArea(),
                        estimate.getApartmentNumber(),
                        estimate.getBalcony(),
                        estimate.getBathroomCondition(),
                        estimate.getBuildingHasElevator(),
                        estimate.getCanParkCar(),
                        estimate.getCeilingHeight(),
                        estimate.getConstructionEra(),
                        estimate.getConstructionYear(),
                        estimate.getFireplace(),
                        estimate.getFloor(),
                        estimate.getHasBasement(),
                        estimate.getKitchenCondition(),
                        estimate.getKnowledge(),
                        estimate.getLastGroundDrainage(),
                        estimate.getLastRoofRenovation(),
                        estimate.getLatitude(),
                        estimate.getListPrice(),
                        estimate.getLivingArea(),
                        estimate.getLongitude(),
                        estimate.getResidenceType(),
                        estimate.getOperatingCost(),
                        estimate.getOperatingCostPerSqm(),
                        estimate.getPatio(),
                        estimate.getPlotArea(),
                        estimate.getRent(),
                        estimate.getRentPerSqm(),
                        estimate.getRooms(),
                        estimate.getStreetAddress(),
                        estimate.getBiddingAveragePrediction(),
                        estimate.getBiddingAverageWeight(),
                        estimate.getDifferenceAverage(),
                        estimate.getDifferenceCv(),
                        estimate.getKnnPrediction(),
                        estimate.getKnnWeight(),
                        estimate.getPredictionDate(),
                        estimate.getPredictor(),
                        estimate.getPreviousSalePrediction(),
                        estimate.getPreviousSaleWeight(),
                        estimate.getPriceCv(),
                        estimate.getRecommendation(),
                        estimate.getAccuracy(),
                        estimate.getPrice(),
                        estimate.getPriceRangeHigh(),
                        estimate.getPriceRangeLow(),
                        estimate.getSqmPrice(),
                        estimate.getSqmPriceRangeHigh(),
                        estimate.getSqmPriceRangeLow(),
                        estimate.getNumberOfReferences()
                )).collect(Collectors.toList());

        return new PropertyEstimates(estimates);
    }

    @Override
    public Credentials createCredentials(String userId) {
        List<ExportCredential> credentials = credentialsRepository.findAllByUserId(userId)
                .stream().map(credential -> new ExportCredential(
                        format(credential.getType().toString()),
                        format(findProviderByName(credential.getProviderName()).getDisplayName()),
                        stringOrEmpty(credential.getPayload()),
                        credential.getFields()
                )).collect(Collectors.toList());

        return new Credentials(credentials);
    }

    @Override
    public Loans createLoans(String userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        List<ExportLoan> loans = accounts.stream()
                .flatMap(account -> loanDAO.getLoanDataByAccountId(account.getId()).stream().map(
                        loan -> {
                            LoanDetails loanDetails = loan.getLoanDetails();
                            List<String> applicants = Collections.emptyList();
                            String security = "";
                            if (loanDetails != null) {
                                applicants = loanDetails.getApplicants();
                                security = loanDetails.getLoanSecurity();
                            }
                            return new ExportLoan(
                                    account.getAccountNumber(),
                                    applicants,
                                    format(security),
                                    stringOrEmpty(loan.getLoanNumber()),
                                    loan.getAmortized(),
                                    loan.getBalance(),
                                    loan.getInitialBalance(),
                                    loan.getInterest(),
                                    loan.getMonthlyAmortization(),
                                    loan.getName(),
                                    loan.getInitialDate(),
                                    loan.getNextDayOfTermsChange(),
                                    loan.getNumMonthsBound(),
                                    findProviderByName(loan.getProviderName()).getDisplayName(),
                                    format(loan.getType().toString()),
                                    stringOrEmpty(loan.getSerializedLoanResponse()));
                        })).collect(Collectors.toList());

        return new Loans(loans);
    }

    @Override
    public Accounts createAccounts(String userId) {
        List<ExportAccount> accounts = accountRepository.findByUserId(userId)
                .stream().map(account -> new ExportAccount(
                        account.getAccountNumber(),
                        account.getName(),
                        format(account.getType().toString()),
                        account.getCertainDate(),
                        account.getAvailableCredit(),
                        account.getBalance(),
                        format(String.valueOf(account.isClosed())),
                        format(String.valueOf(account.isExcluded())),
                        format(String.valueOf(account.isFavored())),
                        account.getOwnership()
                )).collect(Collectors.toList());

        return new Accounts(accounts);
    }

    @Override
    public AccountHistory createAccountHistory(String userId) {
        Map<String, Account> accounts = accountRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(Account::getId, account -> account));

        List<ExportAccountEvent> exportAccountEvents = Lists.newArrayList();

        for (AccountBalance event : accountBalanceHistoryRepository.findByUserId(userId)) {
            String accountId = UUIDUtils.toTinkUUID(event.getAccountId());

            if (!accounts.containsKey(accountId)) {
                log.warn(UUIDUtils.toTinkUUID(event.getUserId()),
                        "Found account history event without matching account.");
                continue;
            }

            exportAccountEvents.add(new ExportAccountEvent(
                    event.getDate(),
                    stringOrEmpty(accounts.get(accountId).getAccountNumber()),
                    stringOrEmpty(accounts.get(accountId).getName()),
                    event.getBalance()));
        }

        return new AccountHistory(exportAccountEvents);
    }

    @Override
    public Transactions createTransactions(String userId) {
        List<ExportTransaction> transactions = transactionDao.findAllByUserId(userId)
                .stream().map(transaction -> {
                    String merchantId = transaction.getMerchantId();
                    String merchantName = "";
                    if (merchantId != null) {
                        Merchant merchant = merchantRepository.findOne(merchantId);
                        if (merchant != null) {
                            merchantName = format(merchant.getName());
                        }
                    }

                    String category = transaction.isUserModifiedCategory() ?
                            categoryRepository.findById(transaction.getCategoryId()).getDisplayName() : "";

                    return new ExportTransaction(
                            transaction.getDate(),
                            transaction.getOriginalDate(),
                            stringOrEmpty(transaction.getDescription()),
                            stringOrEmpty(transaction.getOriginalDescription()),
                            transaction.getAmount(),
                            transaction.getOriginalAmount(),
                            merchantName,
                            stringOrEmpty(transaction.getNotes()),
                            stringOrEmpty(transaction.getPayloadSerialized()),
                            format(objectToString(transaction.getType())),
                            category
                    );
                }).collect(Collectors.toList());

        return new Transactions(transactions);
    }

    /**
     * Fetches from both TransferEventRepository
     * Note: Maybe reformat ExportTransfer to -> transferdetails + events?
     *
     * @param userId
     * @return
     */
    @Override
    public Transfers createTransfers(String userId) {

        List<UUID> transferIds = signableOperationRepository.findAllByUserId(userId).stream()
                .filter(Predicates.signableOperationsOfType(SignableOperationTypes.TRANSFER)::apply)
                .map(SignableOperation::getUnderlyingId)
                .collect(Collectors.toList());

        // Reduce all transferEvents with the same ID to the latest event
        List<TransferEvent> transferEvents = transferIds.stream().map(transferId -> transferEventRepository
                .findAllByUserIdAndTransferId(UUIDUtils.fromTinkUUID(userId), transferId))
                .filter(events -> !events.isEmpty())
                .flatMap(List::stream)
                .collect(Collectors.toList());

        List<ExportTransfer> exportTransfers = transferEvents.stream()
                .map(transferEvent -> {

                    String transferType = objectToString(transferEvent.getTransferType());
                    String transferDestination = objectToString(transferEvent.getDestination());
                    String transferSource = objectToString(transferEvent.getSource());
                    String transferStatus = objectToString(transferEvent.getStatus());
                    return new ExportTransfer(
                                    format(transferType),
                                    transferEvent.getAmount(),
                                    transferEvent.getCurrency(),
                                    format(transferDestination),
                                    stringOrEmpty(transferEvent.getDestinationMessage()),
                                    format(transferSource),
                                    stringOrEmpty(transferEvent.getSourceMessage()),
                                    stringOrEmpty(transferEvent.getRemoteAddress()),
                                    format(transferStatus),
                                    transferEvent.getCreated(),
                                    transferEvent.getUpdated()
                            );
                }).collect(Collectors.toList());

        return new Transfers(exportTransfers);
    }

    @Override
    public Portfolios createPortfolios(String userId) {
        List<ExportPortfolio> portfolios = portfolioRepository.findAllByUserId(UUIDUtils.fromTinkUUID(userId))
                .stream().map(portfolio -> new ExportPortfolio(
                        stringOrEmpty(portfolio.getType().toString()),
                        format(portfolio.getRawType()),
                        portfolio.getTotalProfit(),
                        portfolio.getTotalValue()
                )).collect(Collectors.toList());

        return new Portfolios(portfolios);
    }

    @Override
    public PortfolioHistory createPortfolioHistory(String userId) {
        UUID id = UUIDUtils.fromTinkUUID(userId);

        List<ExportPortfolioEvent> events = portfolioHistoryRepository.findAllByUserId(id)
                .stream().map(event -> {
                    Portfolio portfolio = portfolioRepository
                            .findOneByUserIdAndAccountIdAndPortfolioId(id, event.getAccountId(),
                                    event.getPortfolioId());
                    return new ExportPortfolioEvent(
                            event.getTimestamp(),
                            stringOrEmpty(portfolio.getRawType()),
                            format(portfolio.getType().toString()),
                            event.getTotalProfit(),
                            event.getTotalValue()
                    );
                }).collect(Collectors.toList());

        return new PortfolioHistory(events);
    }

    @Override
    public Instruments createInstruments(String userId) {
        List<ExportInstrument> instruments = instrumentRepository.findAllByUserId(UUIDUtils.fromTinkUUID(userId))
                .stream().map(instrument -> new ExportInstrument(
                        stringOrEmpty(instrument.getName()),
                        format(instrument.getType().toString()),
                        format(instrument.getRawType()),
                        instrument.getAverageAcquisitionPrice(),
                        stringOrEmpty(instrument.getCurrency()),
                        stringOrEmpty(instrument.getIsin()),
                        stringOrEmpty(instrument.getMarketPlace()),
                        instrument.getMarketValue(),
                        instrument.getPrice(),
                        instrument.getProfit(),
                        instrument.getQuantity(),
                        stringOrEmpty(instrument.getTicker())
                )).collect(Collectors.toList());

        return new Instruments(instruments);
    }

    @Override
    public InstrumentHistory createInstrumentHistory(String userId) {
        UUID id = UUIDUtils.fromTinkUUID(userId);

        List<ExportInstrumentEvent> events = instrumentHistoryRepository.findAllByUserId(id)
                .stream().map(event -> {
                    Instrument instrument = instrumentRepository
                            .findOneByUserIdAndPortfolioIdAndInstrumentId(id, event.getPortfolioId(),
                                    event.getInstrumentId());
                    return new ExportInstrumentEvent(
                            event.getTimestamp(),
                            instrument.getName(),
                            event.getAverageAcquisitionPrice(),
                            event.getMarketValue(),
                            event.getProfit(),
                            event.getQuantity()
                    );
                }).collect(Collectors.toList());

        return new InstrumentHistory(events);
    }

    @Override
    public Budgets createBudgets(String userId) {
        List<FollowItem> followItems = followItemRepository.findByUserId(userId);

        // Create expense budgets
        List<ExportBudget> expenseBudgets = followItems.stream()
                .filter(followItem -> Objects.equals(followItem.getType(), FollowTypes.EXPENSES))
                .map(followItem -> {
                    ExpensesFollowCriteria followCriteria = SerializationUtils.deserializeFromString(followItem.getCriteria(),
                            ExpensesFollowCriteria.class);

                    Double targetAmount = null;
                    if (followCriteria != null) {
                        targetAmount = followCriteria.getTargetAmount();
                    }
                    return new ExportBudget(
                            stringOrEmpty(followItem.getName()),
                            targetAmount
                    );
                }).collect(Collectors.toList());


        // Create search budgets
        List<ExportBudget> searchBudgets = followItems.stream()
                .filter(followItem -> Objects.equals(followItem.getType(), FollowTypes.SEARCH))
                .map(followItem -> {
                    SearchFollowCriteria followCriteria = SerializationUtils.deserializeFromString(followItem.getCriteria(),
                            SearchFollowCriteria.class);

                    Double targetAmount = null;
                    String queryString = null;
                    if (followCriteria != null) {
                        targetAmount = followCriteria.getTargetAmount();
                        queryString = followCriteria.getQueryString();
                    }

                    return new ExportBudget(
                            String.format("%s (%s)", stringOrEmpty(followItem.getName()), stringOrEmpty(queryString)),
                            targetAmount
                    );
                }).collect(Collectors.toList());

        List<ExportBudget> budgets = Lists.newArrayList(Iterables.concat(expenseBudgets, searchBudgets));
        return new Budgets(budgets);
    }

    @Override
    public SavingsGoals createSavingsGoals(String userId) {
        List<ExportSavingsGoal> savingsGoals = followItemRepository.findByUserId(userId)
                .stream().filter(followItem -> Objects.equals(followItem.getType(), FollowTypes.SAVINGS))
                .map(followItem -> {
                    SavingsFollowCriteria followCriteria = SerializationUtils.deserializeFromString(followItem.getCriteria(),
                            SavingsFollowCriteria.class);
                    Double targetAmount = null;
                    String targetPeriod = null;

                    if (followCriteria != null) {
                        targetAmount = followCriteria.getTargetAmount();
                        targetPeriod = followCriteria.getTargetPeriod();
                    }

                    return new ExportSavingsGoal(
                            followItem.getName(),
                            targetAmount,
                            stringOrEmpty(targetPeriod)
                    );
                }).collect(Collectors.toList());

        return new SavingsGoals(savingsGoals);
    }

    @Override
    public void validateUser(String userId) throws UserNotFoundException {
        if (Strings.isNullOrEmpty(userId)) {
            throw new UserNotFoundException("No user id for supplied");
        }

        User user = userRepository.findOne(userId);

        if (user == null) {
            throw new UserNotFoundException(String.format("No user found for user id: %s", userId));
        }
    }

    private String stringOrEmpty(String string) {
        return Strings.nullToEmpty(string);
    }

    private String format(String string) {
        string = stringOrEmpty(string);
        return ExportStringFormatter.format(string);
    }

    private <T> String objectToString(T object) {
        if (object == null){
            return "";
        }
        return object.toString();
    }

    private Provider findProviderByName(String name) {
        if (isProvidersOnAggregation) {
            return aggregationControllerClient.getProviderByName(name);
        } else {
            return providerRepository.findByName(name);
        }
    }

    private <T> String objectMessageCall(T object, Function<T, String> messageMethod){
        if (object == null) {
            return "";
        }

        return messageMethod.apply(object);
    }

}
