package se.tink.backend.system.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.application.ApplicationProcessor;
import se.tink.backend.common.application.ApplicationProcessorFactory;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.config.BackOfficeConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.controllers.DeleteController;
import se.tink.backend.common.coordination.BarrierName;
import se.tink.backend.common.dao.AccountDao;
import se.tink.backend.common.dao.ActivityDao;
import se.tink.backend.common.dao.ApplicationDAO;
import se.tink.backend.common.dao.InvestmentDao;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.backend.common.dao.StatisticDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.loans.SwedishLoanNameInterpreter;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.common.mapper.CoreLoanMapper;
import se.tink.backend.common.product.targeting.TargetProductsRunnableFactory;
import se.tink.backend.common.providers.ProviderImageProvider;
import se.tink.backend.common.repository.cassandra.AccountBalanceHistoryRepository;
import se.tink.backend.common.repository.cassandra.ApplicationArchiveRepository;
import se.tink.backend.common.repository.cassandra.ApplicationEventRepository;
import se.tink.backend.common.repository.cassandra.ApplicationFormEventRepository;
import se.tink.backend.common.repository.cassandra.CassandraStatisticRepository;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.cassandra.DAO.LoanDAO;
import se.tink.backend.common.repository.cassandra.DocumentRepository;
import se.tink.backend.common.repository.cassandra.SignableOperationRepository;
import se.tink.backend.common.repository.cassandra.TransferDestinationPatternRepository;
import se.tink.backend.common.repository.cassandra.TransferEventRepository;
import se.tink.backend.common.repository.cassandra.TransferRepository;
import se.tink.backend.common.repository.mysql.main.AbnAmroBufferedAccountRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsContentRepository;
import se.tink.backend.common.repository.mysql.main.ProviderImageRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.StatisticRepository;
import se.tink.backend.common.repository.mysql.main.UserDeviceRepository;
import se.tink.backend.common.repository.mysql.main.UserOriginRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.common.tracking.application.ApplicationTracker;
import se.tink.backend.common.tracking.application.ApplicationTrackerImpl;
import se.tink.backend.common.tracking.appsflyer.AppsFlyerEventBuilder;
import se.tink.backend.common.tracking.appsflyer.AppsFlyerTracker;
import se.tink.backend.common.utils.AccountBalanceUtils;
import se.tink.backend.common.utils.CredentialsPredicate;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Application;
import se.tink.backend.core.CompressedDocument;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsEvent;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Field;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudDetailsContentContainer;
import se.tink.backend.core.Loan;
import se.tink.backend.core.Portfolio;
import se.tink.backend.core.Provider;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserDevice;
import se.tink.backend.core.UserDeviceStatuses;
import se.tink.backend.core.UserOrigin;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.product.ProductInstance;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.backend.core.product.ProductType;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferEvent;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.rpc.DeleteAccountRequest;
import se.tink.backend.rpc.DeleteUserRequest;
import se.tink.backend.rpc.SupplementalInformationRequest;
import se.tink.backend.rpc.SupplementalInformationResponse;
import se.tink.backend.system.api.UpdateService;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.controllers.AccountController;
import se.tink.backend.system.controllers.BackOfficeNotificationController;
import se.tink.backend.system.controllers.abnamro.AbnAmroController;
import se.tink.backend.system.document.DocumentCommandHandler;
import se.tink.backend.system.document.command.EmailDocumentsCommand;
import se.tink.backend.system.document.mapper.BackOfficeConfigurationToDocumentModeratorDetailsMapper;
import se.tink.backend.system.document.mapper.GenericApplicationToDocumentUserMapper;
import se.tink.backend.system.mapper.CorePortfolioMapper;
import se.tink.backend.system.rpc.ProcessAccountsRequest;
import se.tink.backend.system.rpc.UpdateAccountRequest;
import se.tink.backend.system.rpc.UpdateApplicationRequest;
import se.tink.backend.system.rpc.UpdateCredentialsSensitiveRequest;
import se.tink.backend.system.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.system.rpc.UpdateDocumentRequest;
import se.tink.backend.system.rpc.UpdateDocumentResponse;
import se.tink.backend.system.rpc.UpdateFraudDetailsRequest;
import se.tink.backend.system.rpc.UpdateProductInformationRequest;
import se.tink.backend.system.rpc.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.system.rpc.UpdateTransfersRequest;
import se.tink.backend.system.usecases.TransferUseCases;
import se.tink.backend.utils.BeanUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.backend.utils.guavaimpl.Functions;
import se.tink.backend.utils.guavaimpl.predicates.AccountPredicate;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

@Path("/update")
public class UpdateServiceResource implements UpdateService {
    private static final Pattern NEW_SWEDBANK_CREDIT_CARD_NUMBER_PATTERN = Pattern.compile("^\\d{4}\\s\\d{2}\\*{2}\\s\\*{4}\\s\\d{4}$");

    private static final String COOP_MEDMERA_EFTER_1 = "Coop MedMera Efter 1";
    private static final String COOP_MEDMERA_EFTER_2 = "Coop MedMera Efter";

    private final TransactionDao transactionDao;
    private final ActivityDao activityDao;
    private final StatisticDao statisticsDao;
    private final AccountController accountController;
    private final Cluster cluster;
    private final AbnAmroController abnAmroController;
    private final AggregationControllerCommonClient aggregationControllerClient;

    @Context
    private HttpHeaders headers;

    private static final ImmutableSet<String> IKANO_CARD_AGENT_PROVIDER_NAMES = ImmutableSet
            .of("shellmastercard-bankid");

    private static final String COOP_AGENT_PROVIDER_NAME = "coop";
    private static final List<String> SWEDBANK_PROVIDER_NAMES = ImmutableList.<String>builder()
            .add("savingsbank")
            .add("savingsbank-bankid")
            .add("savingsbank-bankid-youth")
            .add("savingsbank-youth")
            .add("swedbank")
            .add("swedbank-bankid")
            .add("swedbank-bankid-youth")
            .add("swedbank-youth")
            .build();
    private static final Set<String> NORDEA_MIGRATION_PROVIDER_NAMES = ImmutableSet.<String>builder()
            .add("no-nordea-lightlogin")
            .add("no-nordea-bankid")
            .add("dk-nordea-nemid")
            .build();
    private static final String NORWEGIAN_AGENT_PROVIDER_NAME = "norwegian-bankid";
    private static final Pattern NORWEGIAN_CARD_NUMBER_PATTERN = Pattern.compile("^\\d{6}\\*{6}\\d{4}$");

    private static final ImmutableSet<CredentialsStatus> LOG_CREDENTIALS_STATUSES = ImmutableSet.of(
            CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION,
            CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION,
            CredentialsStatus.AWAITING_OTHER_CREDENTIALS_TYPE,
            CredentialsStatus.AWAITING_THIRD_PARTY_APP_AUTHENTICATION
    );

    private static final ImmutableSet<CredentialsStatus> LOG_ERROR_CREDENTIALS_STATUSES = ImmutableSet.of(
            CredentialsStatus.TEMPORARY_ERROR, CredentialsStatus.AUTHENTICATION_ERROR);

    private static final ImmutableSet<CredentialsStatus> INTERESTING_CREDENTIALS_STATUSES = ImmutableSet.of(
            CredentialsStatus.TEMPORARY_ERROR, CredentialsStatus.AUTHENTICATION_ERROR, CredentialsStatus.UPDATED);

    private static final ImmutableSet<ApplicationStatusKey> NOTIFY_BACK_OFFICE_ABOUT_APPLICATION_STATUSES = ImmutableSet
            .of(ApplicationStatusKey.REJECTED, ApplicationStatusKey.ABORTED, ApplicationStatusKey.APPROVED,
                    ApplicationStatusKey.EXECUTED);

    private static final ObjectMapper mapper = new ObjectMapper();
    private final HttpResponseHelper httpResponseHelper;

    private static final LogUtils log = new LogUtils(UpdateServiceResource.class);
    private final SystemServiceFactory systemServiceFactory;
    private final CuratorFramework coordinationClient;
    private final CacheClient cacheClient;
    private final DeleteController deleteController;
    private final AnalyticsController analyticsController;
    private final AppsFlyerTracker appsFlyerTracker;

    private final ServiceContext serviceContext;

    private final AccountBalanceHistoryRepository accountBalanceHistoryRepository;
    private final AccountRepository accountRepository;
    private final ApplicationDAO applicationDAO;
    private final CredentialsEventRepository credentialsEventRepository;
    private final CredentialsRepository credentialsRepository;
    private final ProviderRepository providerRepository;
    private final DocumentRepository documentRepository;
    private final FraudDetailsContentRepository fraudDetailsContentRepository;
    private final ApplicationArchiveRepository applicationArchiveRepository;
    private final LoanDAO loanDAO;
    private final ProductDAO productDAO;
    private final SignableOperationRepository signableOperationRepository;
    private final TransferDestinationPatternRepository transferDestinationPatternRepository;
    private final TransferEventRepository transferEventRepository;
    private final TransferRepository transferRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final UserOriginRepository userOriginRepository;
    private final UserRepository userRepository;
    private final UserStateRepository userStateRepository;
    private final ApplicationTracker applicationTracker;
    private final FirehoseQueueProducer firehoseQueueProducer;
    private final InvestmentDao investmentDao;

    private final TargetProductsRunnableFactory targetProductsRunnableFactory;

    private final ApplicationProcessorFactory applicationProcessorFactory;
    private final MailSender mailSender;

    public UpdateServiceResource(ServiceContext serviceContext, FirehoseQueueProducer firehoseQueueProducer,
            MetricRegistry metricRegistry) {
        this.serviceContext = serviceContext;

        this.aggregationControllerClient = serviceContext.getAggregationControllerCommonClient();
        this.systemServiceFactory = serviceContext.getSystemServiceFactory();
        this.cacheClient = serviceContext.getCacheClient();
        this.coordinationClient = serviceContext.getCoordinationClient();
        this.deleteController = new DeleteController(serviceContext);
        this.analyticsController = new AnalyticsController(serviceContext.getEventTracker());
        this.appsFlyerTracker = new AppsFlyerTracker();
        this.accountBalanceHistoryRepository = serviceContext.getRepository(AccountBalanceHistoryRepository.class);
        this.accountRepository = serviceContext.getRepository(AccountRepository.class);
        this.applicationDAO = serviceContext.getDao(ApplicationDAO.class);
        this.credentialsEventRepository = serviceContext.getRepository(CredentialsEventRepository.class);
        this.credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        this.providerRepository = serviceContext.getRepository(ProviderRepository.class);
        this.documentRepository = serviceContext.getRepository(DocumentRepository.class);
        this.fraudDetailsContentRepository = serviceContext.getRepository(FraudDetailsContentRepository.class);
        this.applicationArchiveRepository = serviceContext.getRepository(ApplicationArchiveRepository.class);
        this.loanDAO = serviceContext.getDao(LoanDAO.class);
        this.productDAO = serviceContext.getDao(ProductDAO.class);
        this.signableOperationRepository = serviceContext.getRepository(SignableOperationRepository.class);
        this.transferDestinationPatternRepository = serviceContext
                .getRepository(TransferDestinationPatternRepository.class);
        this.transferEventRepository = serviceContext.getRepository(TransferEventRepository.class);
        this.transferRepository = serviceContext.getRepository(TransferRepository.class);
        this.userDeviceRepository = serviceContext.getRepository(UserDeviceRepository.class);
        this.userOriginRepository = serviceContext.getRepository(UserOriginRepository.class);
        this.userRepository = serviceContext.getRepository(UserRepository.class);
        this.userStateRepository = serviceContext.getRepository(UserStateRepository.class);
        this.transactionDao = serviceContext.getDao(TransactionDao.class);
        this.activityDao = serviceContext.getDao(ActivityDao.class);
        this.cluster = serviceContext.getConfiguration().getCluster();
        this.investmentDao = serviceContext.getDao(InvestmentDao.class);
        this.statisticsDao = new StatisticDao(serviceContext.getRepository(CassandraStatisticRepository.class),
                serviceContext.getCacheClient(), metricRegistry);
        this.httpResponseHelper = new HttpResponseHelper(log);

        this.applicationTracker = new ApplicationTrackerImpl(
                serviceContext.getRepository(ApplicationEventRepository.class),
                serviceContext.getRepository(ApplicationFormEventRepository.class),
                metricRegistry);

        if (Objects.equal(cluster, Cluster.ABNAMRO)) {
            this.abnAmroController = new AbnAmroController(
                    serviceContext.getRepository(AbnAmroBufferedAccountRepository.class),
                    serviceContext.getConfiguration().getAbnAmro(), metricRegistry);
        } else {
            this.abnAmroController = null;
        }

        this.firehoseQueueProducer = firehoseQueueProducer;
        this.targetProductsRunnableFactory = new TargetProductsRunnableFactory(serviceContext);

        ProviderImageProvider providerImageProvider = new ProviderImageProvider(serviceContext.getRepository(
                ProviderImageRepository.class));

        this.applicationProcessorFactory = new ApplicationProcessorFactory(serviceContext,
                providerImageProvider);
        this.mailSender = serviceContext.getMailSender();
        this.accountController = new AccountController(accountRepository, serviceContext.getDao(AccountDao.class),
                firehoseQueueProducer, userRepository,
                targetProductsRunnableFactory, serviceContext.getExecutorService(), credentialsRepository,
                metricRegistry);
    }

    @Override
    @Timed
    @Transactional
    public Account updateAccount(UpdateAccountRequest request) {
        log.debug("Account update requested: " + request.getUser() + ", credential ID " + request.getCredentialsId());
        try {
            log.debug(request.getUser(), request.getCredentialsId(),
                    "Updating account: " + mapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            log.debug("Cannot serialize UpdateAccountRequest to JSON", e);
        }

        Account account = request.getAccount();

        // Find any existing account.

        Account existingAccount = accountRepository.findByUserIdAndCredentialsIdAndBankId(account.getUserId(),
                account.getCredentialsId(), Preconditions.checkNotNull(account.getBankId()));

        Credentials credentials = credentialsRepository.findOne(request.getCredentialsId());

        if (credentials == null) {
            try {
                log.error(request.getUser(), request.getCredentialsId(),
                        "Account was not found: " + mapper.writeValueAsString(request));
            } catch (JsonProcessingException e) {
                // NOOP.
            }
            httpResponseHelper.error(Response.Status.NOT_FOUND, "Account was not found");
        }

        if (existingAccount != null) {
            if (COOP_AGENT_PROVIDER_NAME.equalsIgnoreCase(credentials.getProviderName())) {
                existingAccount = migrateDuplicateCoopAccounts(existingAccount, account, credentials);
            } else if (SWEDBANK_PROVIDER_NAMES.contains(credentials.getProviderName())) {
                existingAccount = migrateDuplicateSwedbankCreditCards(existingAccount, account, credentials);
            }

            updateAccountWithStaticInfo(account, existingAccount);
        } else {
            handleIkanoAgentMigration(request, account, credentials);
            handleNorwegianAgentMigration(request, account, credentials);
            handleCollectorAgentMigration(request, account, credentials);
            handleNordeaNoAgentMigration(request, account, credentials);

            // If we already have an account with the same provider name and
            // bank id, it's probably due to the fact that the user either have
            // linked the same credentials twice, or that it's a shared account
            // that two separate credentials have access to (ie. a household
            // wide Tink-account).

            List<Account> existingAccounts = accountRepository.findByUserId(account.getUserId());

            for (Account potentialDuplicateAccount : existingAccounts) {
                if (Objects.equal(potentialDuplicateAccount.getBankId(), account.getBankId())) {
                    account.setExcluded(true);
                    break;
                }
            }
        }

        if (account.getType() == null) {
            account.setType(AccountTypes.CHECKING);
        }

        FirehoseMessage.Type firehoseMessageType =
                existingAccount == null ? FirehoseMessage.Type.CREATE : FirehoseMessage.Type.UPDATE;

        Account savedAccount = accountRepository.save(account);

        firehoseQueueProducer.sendAccountMessage(request.getUser(), firehoseMessageType, savedAccount);

        // Update account features
        updatePortfolios(credentials, savedAccount, request.getAccountFeatures().getPortfolios());
        updateLoan(credentials, savedAccount, request.getAccountFeatures().getLoans());

        accountBalanceHistoryRepository.save(AccountBalanceUtils.createEntry(savedAccount));

        // The account is new. Evaluate whether that makes the user eligible for a product.
        if (existingAccount == null) {
            User user = userRepository.findOne(request.getUser());
            Runnable runnable = targetProductsRunnableFactory.createRunnable(user);

            if (runnable != null) {
                serviceContext.execute(runnable);
            }
        }

        // The account is new. Subscribe it towards ABN AMRO.
        if (existingAccount == null && Objects.equal(Cluster.ABNAMRO, cluster)) {
            boolean subscribed = abnAmroController.subscribeAccount(credentials, savedAccount);

            // We don't need to persist this but we need to signal back to aggregation that the account is subscribed
            // towards ABN AMRO so that aggregation knows that we are waiting on transactions to be ingested in the
            // connector.
            savedAccount.putPayload(AbnAmroUtils.InternalAccountPayloadKeys.SUBSCRIBED, String.valueOf(subscribed));
        }

        return savedAccount;
    }

    private void updatePortfolios(Credentials credentials, Account savedAccount, List<se.tink.backend.system.rpc.Portfolio> portfolios) {
        for (se.tink.backend.system.rpc.Portfolio rpcPortfolio : portfolios) {
            Portfolio portfolio = CorePortfolioMapper.fromSystemToCore(rpcPortfolio);

            portfolio.setUserId(UUIDUtils.fromTinkUUID(credentials.getUserId()));
            portfolio.setAccountId(UUIDUtils.fromTinkUUID(savedAccount.getId()));
            portfolio.setCredentials(credentials);

            portfolio.getInstruments()
                    .forEach(i -> i.setUserId(UUIDUtils.fromTinkUUID(credentials.getUserId())));

            investmentDao.save(portfolio);
        }
    }

        private void updateLoan(Credentials credentials, Account savedAccount, List<se.tink.backend.system.rpc.Loan> loans) {
        for (se.tink.backend.system.rpc.Loan rpcLoan : loans) {
            Loan loan = CoreLoanMapper.toCoreLoan(rpcLoan);

            loan.setAccountId(UUIDUtils.fromTinkUUID(savedAccount.getId()));
            loan.setCredentialsId(UUIDUtils.fromTinkUUID(savedAccount.getCredentialsId()));
            loan.setUserId(UUIDUtils.fromTinkUUID(savedAccount.getUserId()));
            loan.setProviderName(credentials.getProviderName());

            SwedishLoanNameInterpreter interpreter = new SwedishLoanNameInterpreter(loan.getName());
            // Only set guesses if agent didn't already set them
            if (loan.getType() == null) {
                loan.setType(interpreter.getGuessedLoanType());
            }
            if (loan.getNumMonthsBound() == null) {
                loan.setNumMonthsBound(interpreter.getGuessedNumMonthsBound());
            }

            loanDAO.saveIfUpdated(loan);
        }
    }

    /**
     * Temporarily added this method to migrate Collector's account identifiers from serial number to UUID. Collector
     * are switching their backend in the April/May month break, and will abandon the old identifiers. The OCR number
     * for deposits will remain, so the transfer identifiers will be used to identify the accounts in the transition
     * period.
     */
    private void handleCollectorAgentMigration(UpdateAccountRequest request, Account account, Credentials credentials) {
        if (Objects.equal(credentials.getProviderName(), "collector-bankid")) {
            List<Account> existingCollectorAccounts = accountRepository.findByUserIdAndCredentialsId(
                    request.getUser(), request.getCredentialsId());

            if (existingCollectorAccounts == null || existingCollectorAccounts.isEmpty()) {
                return;
            }

            List<AccountIdentifier> identifiers = account.getIdentifiers();

            // The incoming account may only have one identifier.
            if (identifiers == null || identifiers.size() != 1) {
                return;
            }

            final AccountIdentifier identifier = identifiers.get(0);

            Optional<Account> existingAccount = existingCollectorAccounts.stream()
                    .filter(candidate -> {
                        List<AccountIdentifier> candidateIdentifiers = candidate.getIdentifiers();

                        if (candidateIdentifiers == null || candidateIdentifiers.isEmpty()) {
                            return false;
                        }

                        return candidateIdentifiers.contains(identifier);
                    }).findFirst();

            if (existingAccount.isPresent()) {
                updateAccountWithStaticInfo(account, existingAccount.get());
            }
        }
    }

    /**
     * Temporary added this method to switch bankId on accounts for next generation Nordea Agents.
     * This is need since we will stop masking the bankid.
     */
    private void handleNordeaNoAgentMigration(UpdateAccountRequest request, Account account, Credentials credentials) {
        if (!NORDEA_MIGRATION_PROVIDER_NAMES.contains(credentials.getProviderName())) {
             return;
         }

         List<Account> existingNordeaAccounts = accountRepository.findByUserIdAndCredentialsId(
                request.getUser(), request.getCredentialsId());

         Optional<Account> originalAccount = existingNordeaAccounts.stream()
                 .filter(a -> !a.isExcluded())
                 .filter(a -> Objects.equal(a.getAccountNumber(), account.getAccountNumber()))
                 .filter(a -> a.getBankId().length() == 4)
                 .findFirst();

         originalAccount.ifPresent(a -> updateAccountWithStaticInfo(account, a));
    }

    /**
     * Temporary added this method to switch bankId on accounts for NorwegianAgent.
     * If bankId matches ( Pattern ex: "123456******1234" ) replace the old account with the new one.
     */
    private void handleNorwegianAgentMigration(UpdateAccountRequest request, Account account,
            Credentials credentials) {

        if (Objects.equal(credentials.getProviderName(), NORWEGIAN_AGENT_PROVIDER_NAME)) {
            List<Account> existingNorwegianAccounts = accountRepository.findByUserIdAndCredentialsId(
                    request.getUser(), request.getCredentialsId());

            if (existingNorwegianAccounts == null || existingNorwegianAccounts.isEmpty()) {
                return;
            }

            FluentIterable<Account> creditCardAccounts = FluentIterable.from(existingNorwegianAccounts)
                    .filter(AccountPredicate.IS_NOT_EXCLUDED)
                    .filter(account1 -> {
                        Matcher cardMatcher = NORWEGIAN_CARD_NUMBER_PATTERN.matcher(account1.getBankId());
                        return cardMatcher.matches();
                    });

            if (creditCardAccounts.isEmpty()) {
                return;
            }

            Account mostActiveAccount = Ordering.from(Comparator
                    .comparing(Account::getCertainDate, Comparator.nullsFirst(Comparator.naturalOrder())))
                    .max(creditCardAccounts);

            updateAccountWithStaticInfo(account, mostActiveAccount);
        }
    }

    /**
     * Temporary added this methid to switch bankId on accounts when moving from IkanoCardAgent to IkanoApiAgent.
     * If bankId = "1" replace the old account with the new one.
     */
    private void handleIkanoAgentMigration(UpdateAccountRequest request, Account account,
            Credentials credentials) {
        if (IKANO_CARD_AGENT_PROVIDER_NAMES.contains(credentials.getProviderName())) {

            // There is only one account per this credentials in db.
            List<Account> existingIkanoAgentAccounts = accountRepository.findByUserIdAndCredentialsId(
                    request.getUser(), request.getCredentialsId());

            if (existingIkanoAgentAccounts != null && existingIkanoAgentAccounts.size() > 0) {
                Account existingIkanoAgentAccount = existingIkanoAgentAccounts.get(0);

                if (Objects.equal(existingIkanoAgentAccount.getBankId(), "1")) {
                    updateAccountWithStaticInfo(account, existingIkanoAgentAccount);
                }
            }
        }
    }

    private Account migrateDuplicateSwedbankCreditCards(Account existingAccount, Account account, Credentials credentials) {
        if ((!SWEDBANK_PROVIDER_NAMES.contains(credentials.getProviderName())
                && !Objects.equal(account.getType(), AccountTypes.CREDIT_CARD))
                || !NEW_SWEDBANK_CREDIT_CARD_NUMBER_PATTERN.matcher(account.getBankId()).matches()) {
            return existingAccount;
        }

        final String firstSixDigits = account.getBankId().substring(0, 7);
        final String lastFourDigits = account.getBankId().substring(15);

        Optional<Account> originalAccount = accountRepository.findByUserIdAndCredentialsId(credentials.getUserId(),
                credentials.getId()).stream()
                .filter(a -> a.getBankId().matches("^(\\d{4}\\s){3}\\d{4}$")
                            && a.getBankId().startsWith(firstSixDigits)
                            && a.getBankId().endsWith(lastFourDigits))
                .findFirst();

        if (!originalAccount.isPresent()) {
            return existingAccount;
        }

        // Since we will change the bankId of the original account (the one that we want to use),
        // we need to change the bankId of the duplicated account
        existingAccount.setBankId(existingAccount.getBankId() + "-duplicate");
        existingAccount.setClosed(true);
        existingAccount.setExcluded(true);
        accountRepository.save(existingAccount);

        return originalAccount.get();
    }

    // Coop have removed AccountType.MEDMERA_EFTER_2 and migrated those accounts to AccountType.MEDMERA_EFTER_1
    private Account migrateDuplicateCoopAccounts(Account existingAccount, Account account, Credentials credentials) {
        if (!COOP_AGENT_PROVIDER_NAME.equalsIgnoreCase(credentials.getProviderName())
                || !Objects.equal(account.getBankId(), StringUtils.hashAsStringMD5(credentials.getId() + COOP_MEDMERA_EFTER_1))) {
            return existingAccount;
        }

        // Find the original account
        Optional<Account> originalAccount = accountRepository.findByUserIdAndCredentialsId(
                credentials.getUserId(), credentials.getId()).stream()
                .filter(a -> Objects.equal(a.getBankId(), StringUtils.hashAsStringMD5(credentials.getId() + COOP_MEDMERA_EFTER_2)))
                .findFirst();

        if (!originalAccount.isPresent()) {
            return existingAccount;
        }

        // Since we will change the bankId of the original account (the one that we want to use),
        // we need to change the bankId of the duplicated account
        existingAccount.setBankId(existingAccount.getBankId() + "-duplicate");
        existingAccount.setClosed(true);
        existingAccount.setExcluded(true);
        accountRepository.save(existingAccount);

        return originalAccount.get();
    }

    private void updateAccountWithStaticInfo(Account account, Account oldAccount) {
        if (oldAccount.isUserModifiedName()) {
            account.setName(oldAccount.getName());
            account.setUserModifiedName(true);
        }

        if (oldAccount.isUserModifiedType()) {
            account.setType(oldAccount.getType());
            account.setUserModifiedType(true);
        }

        if (oldAccount.isUserModifiedExcluded()) {
            account.setExcluded(oldAccount.isExcluded());
            account.setUserModifiedExcluded(true);
        }

        // Fallback to old account number of new account number is null or empty
        if (Strings.isNullOrEmpty(account.getAccountNumber())) {
            account.setAccountNumber(oldAccount.getAccountNumber());
        }

        // Fallback to old account name of new account name is null or empty
        if (Strings.isNullOrEmpty(account.getName())) {
            account.setName(oldAccount.getName());
        }

        if (account.getType() == null) {
            account.setType(oldAccount.getType());
        }

        account.setFavored(oldAccount.isFavored());
        account.setId(oldAccount.getId());
        account.setExcluded(oldAccount.isExcluded());
        account.setOwnership(oldAccount.getOwnership());
        account.setCertainDate(oldAccount.getCertainDate());
    }

    @Override
    public Response updateTransferDestinationPatterns(UpdateTransferDestinationPatternsRequest request) {
        Map<Account, List<TransferDestinationPattern>> incomingDestinationsByAccount = request.getDestinationsBySouce();

        final ListMultimap<String, TransferDestinationPattern> existingDestinationsByAccountId = transferDestinationPatternRepository
                .findAllByUserId(UUIDUtils.fromTinkUUID(request.getUserId()));

        // Remove all transfer destinations for closed accounts

        final ImmutableList<Account> closedAccounts = FluentIterable.from(
                accountRepository.findByUserId(request.getUserId()))
                .filter(Account::isClosed).toList();

        List<TransferDestinationPattern> toRemoveForClosedAccounts = Lists.newArrayList();
        for (Account closedAccount : closedAccounts) {
            toRemoveForClosedAccounts.addAll(existingDestinationsByAccountId.get(closedAccount.getId()));
        }

        if (Iterables.size(toRemoveForClosedAccounts) > 0) {
            log.info(
                    request.getUserId(),
                    "Removing transfer destinations: "
                            + Iterables.size(toRemoveForClosedAccounts));
            transferDestinationPatternRepository.delete(toRemoveForClosedAccounts);
        }

        // Loop incoming accounts per type and verify their destinations against what is in db.

        for (Account sourceAccount : incomingDestinationsByAccount.keySet()) {

            // Index by type and compare to incoming destinations for this type.

            final List<TransferDestinationPattern> incomingDestinations = incomingDestinationsByAccount
                    .get(sourceAccount);

            for (TransferDestinationPattern incomingDestination : incomingDestinations) {
                incomingDestination.setUserId(UUIDUtils.fromTinkUUID(sourceAccount.getUserId()));
                incomingDestination.setAccountId(UUIDUtils.fromTinkUUID(sourceAccount.getId()));
            }

            final List<TransferDestinationPattern> existingDestinations = existingDestinationsByAccountId
                    .get(sourceAccount.getId());

            Iterable<TransferDestinationPattern> toRemove = Iterables.filter(existingDestinations,
                    pattern -> !incomingDestinations.contains(pattern));

            Iterable<TransferDestinationPattern> toAdd = Iterables.filter(incomingDestinations,
                    pattern -> !existingDestinations.contains(pattern));

            if (Iterables.size(toRemove) > 0) {
                log.info(
                        request.getUserId(),
                        "Removing transfer destinations: "
                                + Iterables.size(toRemove));
                transferDestinationPatternRepository.delete(toRemove);
            }
            if (Iterables.size(toAdd) > 0) {
                log.info(request.getUserId(),
                        "Adding transfer destinations: " + Iterables.size(toAdd));
                transferDestinationPatternRepository.save(toAdd);
            }
        }

        return HttpResponseHelper.ok();
    }

    /**
     * Return the supplemental information that the user provided for a specific credential. This is called by the agent
     * when the barrier is destroyed in main-service when the user has supplied the supplemental information required.
     */
    @Override
    public SupplementalInformationResponse getSupplementalInformation(SupplementalInformationRequest request) {
        String supplementalInformation = (String) cacheClient
                .get(CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, request.getCredentialsId());

        cacheClient.delete(CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, request.getCredentialsId());

        SupplementalInformationResponse response = new SupplementalInformationResponse();
        response.setContent(supplementalInformation);

        return response;
    }

    @Override
    public Response processAccounts(ProcessAccountsRequest request) {
        accountController.process(request.getUserId(), request.getCredentialsId(), request.getAccountIds());

        return HttpResponseHelper.ok();
    }

    @Override
    public Response updateCredentialsSensitiveData(UpdateCredentialsSensitiveRequest request) {
        Credentials existingCredentials = credentialsRepository.findOne(request.getCredentialsId());
        if (existingCredentials == null) {
            log.warn(request.getUserId(), request.getCredentialsId(),
                    "Cannot find credentials to update sensitive data.");
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        existingCredentials.setSensitiveDataSerialized(request.getSensitiveData());
        credentialsRepository.save(existingCredentials);
        return HttpResponseHelper.ok();
    }

    @Override
    public Response updateCredentials(UpdateCredentialsStatusRequest request) {
        final Credentials credentials = request.getCredentials();
        Credentials existingCredentials = credentialsRepository.findOne(credentials.getId());

        // credentials probably removed while updated

        if (existingCredentials == null) {
            log.warn(request.getUserId(), request.getCredentials().getId(), "Cannot find credentials to update");
            return HttpResponseHelper.ok();
        }

        User user = userRepository.findOne(existingCredentials.getUserId());

        boolean updateContextTimestamp = false;

        CredentialsStatus oldCredentialsStatus = existingCredentials.getStatus();

        // Update credentials if update or create, or if status has changed.

        if (credentials.getStatus() != oldCredentialsStatus || (request.isUpdateContextTimestamp() &&
                !Objects.equal(credentials.getStatusPayload(), existingCredentials.getStatusPayload()))) {
            updateContextTimestamp = true;
            existingCredentials.setStatusUpdated(new Date());
        }

        BeanUtils.copyModifiableProperties(credentials, existingCredentials);

        existingCredentials.setPayload(credentials.getPayload());
        existingCredentials.setSupplementalInformation(credentials.getSupplementalInformation());
        existingCredentials.setStatus(credentials.getStatus());
        existingCredentials.setNextUpdate(credentials.getNextUpdate());
        existingCredentials.setType(credentials.getType());

        if ((credentials.getStatus() == CredentialsStatus.UPDATED)) {
            existingCredentials.setUpdated(new Date());
        }

        if (credentials.getStatus() == CredentialsStatus.UNCHANGED) {
            List<CredentialsEvent> latestAgentEvents = credentialsEventRepository
                    .findMostRecentByUserIdAndCredentialsIdAndStatusIn(credentials.getUserId(), credentials.getId(),
                            1, INTERESTING_CREDENTIALS_STATUSES);

            for (CredentialsEvent credentialsEventEvent : latestAgentEvents) {

                existingCredentials.setStatus(credentialsEventEvent.getStatus());
                existingCredentials.setStatusPayload(null);

                if (credentialsEventEvent.getStatus() == CredentialsStatus.UPDATED) {
                    existingCredentials.setUpdated(credentialsEventEvent.getTimestamp());
                }
            }

            if (existingCredentials.getStatus() == CredentialsStatus.UNCHANGED) {
                existingCredentials.setStatus(CredentialsStatus.AUTHENTICATION_ERROR);
            }

            if (existingCredentials.getStatus() == CredentialsStatus.CREATED) {
                existingCredentials.setStatus(CredentialsStatus.AUTHENTICATION_ERROR);
            }
        }

        String locale;
        if (user != null) {
            locale = user.getProfile().getLocale();
        } else {
            // This happens when we have imported the credentials only for testing purposes.
            log.warn("Could not fetch user for credential '" + existingCredentials.getId()
                    + "'. Using fallback locale.");
            locale = "sv_SE";
        }
        final Catalog catalog = Catalog.getCatalog(locale);

        if (existingCredentials.getStatus() == CredentialsStatus.AUTHENTICATING
                || existingCredentials.getStatus() == CredentialsStatus.CREATED) {
            existingCredentials.setStatusPayload(catalog.getString("Updating..."));
        } else if (Strings.isNullOrEmpty(existingCredentials.getStatusPayload())) {
            switch (existingCredentials.getStatus()) {
            case UPDATING:
                existingCredentials.setStatusPayload(catalog.getString("Updating..."));
                break;
            case UPDATED:
                existingCredentials.setStatusPayload(catalog.getString("Updated."));
                break;
            case AUTHENTICATION_ERROR:
                existingCredentials.setStatusPayload(catalog
                        .getString("Authentication error. Please check your credentials."));
                break;
            case TEMPORARY_ERROR:
                existingCredentials.setStatusPayload(catalog.getString("Temporary error. Please try again later."));
                break;
            default:
                break;
            }
        }

        log.info(existingCredentials.getUserId(), existingCredentials.getId(),
                "Updating credentials: " + existingCredentials.getStatus() + ", "
                        + existingCredentials.getStatusPayload() + ", updateContextTimestamp: "
                        + updateContextTimestamp);

        // Log MFA Q&As, log update latency, log errors and updated.

        if (LOG_CREDENTIALS_STATUSES.contains(existingCredentials.getStatus())) {
            credentialsEventRepository.save(new CredentialsEvent(existingCredentials, existingCredentials.getStatus(),
                    existingCredentials.getSupplementalInformation(), request.isManual()));

        } else if (existingCredentials.getStatus() == CredentialsStatus.UPDATING
                && credentials.getProviderLatency() != 0) {
            credentialsEventRepository.save(new CredentialsEvent(existingCredentials, CredentialsStatus.METRIC, String
                    .valueOf(credentials.getProviderLatency()), request.isManual()));

        } else if (LOG_ERROR_CREDENTIALS_STATUSES.contains(existingCredentials.getStatus())
                || (oldCredentialsStatus == CredentialsStatus.UPDATING
                && existingCredentials.getStatus() == CredentialsStatus.UPDATED)) {

            credentialsEventRepository.save(new CredentialsEvent(existingCredentials, existingCredentials.getStatus(),
                    existingCredentials.getStatusPayload(), request.isManual()));
        }

        credentialsRepository.save(existingCredentials);

        firehoseQueueProducer.sendCredentialMessage(existingCredentials.getUserId(), FirehoseMessage.Type.UPDATE,
                existingCredentials);

        if (updateContextTimestamp) {
            userStateRepository.updateContextTimestampByUserId(request.getUserId(), cacheClient);
        }

        if (updateContextTimestamp
                && (credentials.getStatus() == CredentialsStatus.UPDATING
                || credentials.getStatus() == CredentialsStatus.UPDATED)) {
            Provider provider;
            if (serviceContext.isProvidersOnAggregation()) {
                provider = aggregationControllerClient.getProviderByName(credentials.getProviderName());
            } else {
                provider = providerRepository.findByName(credentials.getProviderName());
            }

            if (provider != null && provider.isMultiFactor()) {
                // If the request was made manually from a specific user device, let's set the device as authorized if we've
                // successfully authorized a MFA credential.

                if (request.getUserDeviceId() != null) {
                    UserDevice userDevice = userDeviceRepository.findOneByUserIdAndDeviceId(user.getId(),
                            request.getUserDeviceId());

                    if (userDevice.getStatus() != UserDeviceStatuses.AUTHORIZED) {
                        userDevice.setStatus(UserDeviceStatuses.AUTHORIZED);
                        userDeviceRepository.save(userDevice);
                    }
                }

                // Set the user's national ID number if we've successfully authorized a MFA credential.
                populateNationalId(user, credentials);
            }
        }

        log.debug(existingCredentials, "Done updating credentials");
        return HttpResponseHelper.ok();
    }

    private void populateNationalId(User user, Credentials credentials) {
        if (user.getFlags().contains(FeatureFlags.DETECT_NATIONAL_ID) && Strings.isNullOrEmpty(user.getNationalId())
                && CredentialsPredicate.CREDENTIAL_HAS_SSN_USERNAME.test(credentials) && !credentials
                .isDemoCredentials() && Objects.equal(credentials.getType(), CredentialsTypes.MOBILE_BANKID)) {

            final String nationalId = credentials.getField(Field.Key.USERNAME);

            if (userRepository.findOneByNationalId(nationalId) != null) {
                log.warn(user.getId(), "Another user is already linked with the national id.");
                return;
            }

            user.setNationalId(nationalId);
            try {
                userRepository.save(user);
            } catch (DataIntegrityViolationException e) {
                log.warn(user.getId(), "Unable to persist national id.", e);
            }
        }
    }

    @Override
    public Response updateFraudDetails(UpdateFraudDetailsRequest request) {

        FraudDetailsContentContainer container = new FraudDetailsContentContainer();
        container.setUserId(request.getUserId());
        container.setDetailsContents(request.getDetailsContents());

        // Store to cache and details content table
        cacheClient.set(CacheScope.FRAUD_DETAILS_BY_USERID, request.getUserId(), FraudDetailsContent.CACHE_EXPIRY,
                container.getData());
        fraudDetailsContentRepository.save(container);

        return HttpResponseHelper.ok();
    }

    @Override
    public Response updateSignableOperation(SignableOperation signableOperation) {
        signableOperation.setUpdated(new Date());

        try {
            switch (signableOperation.getType()) {
            case APPLICATION:
                updateSignableApplicationOperation(signableOperation);
                break;
            case TRANSFER:
                updateSignableTransferOperation(signableOperation);
                break;
            default:
                // Do nothing.
            }
        } finally {
            signableOperationRepository.save(signableOperation);
            userStateRepository.updateContextTimestampByUserId(UUIDUtils.toTinkUUID(signableOperation.getUserId()),
                    cacheClient);

            firehoseQueueProducer.sendSignableOperationMessage(UUIDUtils.toTinkUUID(signableOperation.getUserId()),
                    FirehoseMessage.Type.UPDATE, signableOperation);
        }

        return HttpResponseHelper.ok();
    }

    private void updateSignableApplicationOperation(SignableOperation signableOperation) {
        switch (signableOperation.getStatus()) {
        case EXECUTED:
        case CANCELLED:
        case FAILED: {
            User user = userRepository.findOne(UUIDUtils.toTinkUUID(signableOperation.getUserId()));
            Application application = applicationDAO.findByUserIdAndId(signableOperation.getUserId(),
                    signableOperation.getUnderlyingId());

            if (application == null) {
                log.error(
                        UUIDUtils.toTinkUUID(signableOperation.getUserId()),
                        String.format("Application [applicationId:%s] could not be found.",
                                UUIDUtils.toTinkUUID(signableOperation.getUnderlyingId())));
                return;
            }

            switch (signableOperation.getStatus()) {
            case EXECUTED:
                setApplicationToSigned(application, signableOperation, user);
                break;
            case CANCELLED:
            case FAILED:
            default:
                setApplicationToError(application, signableOperation, user);
                break;
            }

            applicationDAO.save(application);
            applicationTracker.track(application);

            if (Objects.equal(ApplicationStatusKey.SIGNED, application.getStatus().getKey())) {
                systemServiceFactory.getProcessService()
                        .generateStatisticsAndActivitiesWithoutNotifications(
                                UUIDUtils.toTinkUUID(signableOperation.getUserId()), StatisticMode.SIMPLE);

                if (Objects.equal(application.getType(), ApplicationType.SWITCH_MORTGAGE_PROVIDER)) {
                    generateSwitchMortgageProviderDocuments(application, user, mailSender);
                }
            }

            break;
        }
        case CREATED:
        case EXECUTING:
        case AWAITING_CREDENTIALS:
        default:
            // Do nothing.
        }
    }

    private void generateSwitchMortgageProviderDocuments(Application application, User user, MailSender mailSender) {
        try {
            ServiceConfiguration configuration = serviceContext.getConfiguration();
            BackOfficeConfiguration backOfficeConfiguration = configuration.getBackOffice();

            if (configuration.isDevelopmentMode() || !backOfficeConfiguration.isEnabled()) {
                // Back office is disabled. Don't send any email.
                log.debug("Aborting since back office is disabled. Change the configuration to enable it.");
                return;
            }

            ApplicationProcessor processor = applicationProcessorFactory.create(application,
                    user, null);
            GenericApplication genericApplication = processor.getGenericApplication(application);

            EmailDocumentsCommand cmd = new EmailDocumentsCommand(
                    GenericApplicationToDocumentUserMapper.translate(genericApplication, DateUtils.getToday()),
                    BackOfficeConfigurationToDocumentModeratorDetailsMapper.translate(backOfficeConfiguration), user.getId());

            DocumentCommandHandler commandHandler = new DocumentCommandHandler(mailSender, documentRepository);
            commandHandler.on(cmd);
            log.info(user.getId(), String.format("Documents generated and sent successfully [applicationId:%s].",
                    UUIDUtils.toTinkUUID(application.getId())));
        } catch (Exception e) {
            log.error(user.getId(), String.format("Unable to generate documents [applicationId:%s].",
                    UUIDUtils.toTinkUUID(application.getId())), e);
        }
    }

    private void setApplicationToError(Application application, SignableOperation signableOperation, User user) {

        application.updateStatus(ApplicationStatusKey.ERROR);

        SignableOperation.StatusDetailsKey statusDetailsKey = signableOperation.getStatusDetailsKey();
        if (statusDetailsKey == null) {
            statusDetailsKey = SignableOperation.StatusDetailsKey.TECHNICAL_ERROR;
        }

        application.getProperties().put(ApplicationPropertyKey.EXTERNAL_STATUS_DESCRIPTION,
                signableOperation.getStatusMessage());
        application.getProperties().put(ApplicationPropertyKey.EXTERNAL_STATUS,
                statusDetailsKey.name());

        analyticsController.trackEvent(user, String.format("application.%s.sign.%s", application.getType(),
                statusDetailsKey.toString().toLowerCase()));
    }

    private void setApplicationToSigned(Application application, SignableOperation signableOperation, User user) {

        String externalId = signableOperation.getSignableObject();

        // Update the archived application.
        applicationArchiveRepository.setToSigned(application.getUserId(), application.getId(), externalId);

        // Update the application status and external id.
        application.updateStatus(ApplicationStatusKey.SIGNED);
        application.getProperties().put(ApplicationPropertyKey.EXTERNAL_APPLICATION_ID, externalId);

        // Reset eventual status details of failed signings from before to not confuse
        application.getProperties().remove(ApplicationPropertyKey.EXTERNAL_STATUS_DESCRIPTION);
        application.getProperties().remove(ApplicationPropertyKey.EXTERNAL_STATUS);

        analyticsController.trackEvent(user, String.format("application.%s.sign.success", application.getType()));

        // If the user has an origin with a set device type and external service id, attribute it with the signed
        // application!
        UserOrigin origin = userOriginRepository.findOneByUserId(user.getId());
        if (origin != null && !Strings.isNullOrEmpty(origin.getDeviceType())
                && !Strings.isNullOrEmpty(origin.getExternalServiceId())) {

            appsFlyerTracker.trackEvent(AppsFlyerEventBuilder
                    .client(origin.getDeviceType(), origin.getExternalServiceId())
                    .signedApplication(application.getType()).build());
        }

        // Disable all products of the same type, for the user.
        ProductType productType = null;

        switch (application.getType()) {
        case OPEN_SAVINGS_ACCOUNT:
            productType = ProductType.SAVINGS_ACCOUNT;
            break;
        case SWITCH_MORTGAGE_PROVIDER:
            productType = ProductType.MORTGAGE;
            break;
        case RESIDENCE_VALUATION:
            productType = null;
            break;
        }

        productDAO.disableProductInstancesOfType(application.getUserId(), productType);
    }

    private void updateSignableTransferOperation(SignableOperation signableOperation) {
        Transfer transfer = signableOperation.getSignableObject(Transfer.class);

        if (signableOperation.getStatus() == SignableOperationStatuses.EXECUTED) {
            setExecutedStatusMessage(signableOperation, transfer);
        }

        // Auditing
        transferEventRepository.save(new TransferEvent("system:update-signable-operation", transfer, signableOperation,
                Optional.empty()));

        // User tracking
        Map<String, Object> properties = Maps.newHashMap();
        properties.put("Status", signableOperation.getStatus());
        User user = userRepository.findOne(UUIDUtils.toTinkUUID(signableOperation.getUserId()));
        analyticsController.trackUserEvent(user, "transfer.done", properties, RequestHeaderUtils.getRemoteIp(headers));
    }

    private void setExecutedStatusMessage(SignableOperation signableOperation, Transfer transfer) {
        User user = userRepository.findOne(UUIDUtils.toTinkUUID(transfer.getUserId()));
        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());

        switch (transfer.getType()) {
        case BANK_TRANSFER:
            signableOperation.setStatusMessage(catalog.getString("The transfer has been sent to your bank."));
            break;
        case PAYMENT:
        case EINVOICE:
            String message = catalog.getString("The payment has been sent to your bank");
            Date date = transfer.getDueDate() != null ? transfer.getDueDate() : new Date();
            ThreadSafeDateFormat dayAndMonthName = new ThreadSafeDateFormat("d MMMMM",
                    Catalog.getLocale(user.getProfile().getLocale()));
            signableOperation.setStatusMessage(message + ", " + Catalog
                    .format(catalog.getString("paid {0}."), dayAndMonthName.format(date)));
            break;
        }
    }

    @Override
    public Response processEinvoices(final UpdateTransfersRequest request) {
        List<Transfer> incomingTransfers = FluentIterable.from(request.getTransfers())
                .transform(Functions.populateUserAndCredentialsIdAndOriginals(
                        request.getUserId(),
                        request.getCredentialsId()))
                .toList();

        TransferUseCases useCases = new TransferUseCases(transferRepository);
        useCases.syncTransfersWithDatabase(request.getUserId(), request.getCredentialsId(), incomingTransfers);

        return HttpResponseHelper.ok();
    }

    @Override
    public Response updateProductInformation(UpdateProductInformationRequest request) {
        UUID productInstanceId = Preconditions.checkNotNull(request.getProductInstanceId());

        ProductInstance productInstance = productDAO.findInstanceByUserIdAndId(
                UUIDUtils.fromTinkUUID(request.getUserId()), productInstanceId);

        if (productInstance == null) {
            log.warn(request.getUserId(), "Product does not exist for user, maybe user was deleted.");
            return HttpResponseHelper.ok();
        }

        Map<String, Object> existingProperties = productInstance.getProperties();
        Map<ProductPropertyKey, Object> newProperties = request.getProductProperties();

        for (ProductPropertyKey key : newProperties.keySet()) {
            existingProperties.put(key.getKey(), newProperties.get(key));

            switch (key) {
            case VALIDITY_END_DATE:
                productInstance.setValidTo(new Date((Long) newProperties.get(key)));
                break;
            default:
                // Do nothing.
            }
        }

        productInstance.setProperties(existingProperties);
        productDAO.save(productInstance);

        DistributedBarrier barrier = new DistributedBarrier(coordinationClient,
                BarrierName.build(BarrierName.Prefix.PRODUCT_INFORMATION, productInstanceId.toString()));

        try {
            // release potentially taken barrier
            barrier.removeBarrier();
        } catch (Exception e) {
            log.warn(request.getUserId(), "Was not able to release barrier.", e);
        }

        return HttpResponseHelper.ok();
    }

    @Override
    public Response updateApplication(UpdateApplicationRequest request) {

        log.info(UUIDUtils.toTinkUUID(request.getUserId()),
                "Request: " + SerializationUtils.serializeToString(request));

        User user = userRepository.findOne(UUIDUtils.toTinkUUID(request.getUserId()));
        Application application = applicationDAO.findByUserIdAndId(request.getUserId(), request.getApplicationId());

        if (application == null) {
            httpResponseHelper.error(Response.Status.NOT_FOUND, String.format(
                    "[userid:%s, applicationid:%s] Application couldn't be found.",
                    request.getUserId(),
                    request.getApplicationId()));
        }

        boolean applicationStatusChanged = false;

        // If a status is supplied in the request, set it to the application.
        if (request.getApplicationState().getNewApplicationStatus().isPresent()) {
            ApplicationStatusKey newApplicationStatus = request.getApplicationState().getNewApplicationStatus().get();

            log.info(UUIDUtils.toTinkUUID(request.getUserId()), String.format(
                    "[applicationId:%s, status:%s] Properties %s", UUIDUtils.toTinkUUID(request.getApplicationId()),
                    newApplicationStatus.toString(),
                    SerializationUtils.serializeToString(request.getApplicationState().getApplicationProperties())));

            applicationStatusChanged = application.updateStatusIfChanged(newApplicationStatus);
        }

        // Update application properties from supplied state.
        application.getProperties().putAll(request.getApplicationState().getApplicationProperties());

        applicationDAO.save(application);
        applicationTracker.track(application);

        if (applicationStatusChanged) {
            ApplicationStatusKey applicationStatusKey = application.getStatus().getKey();

            analyticsController.trackEvent(user, String.format("application.%s.status.%s",
                    application.getType(), applicationStatusKey.toString().toLowerCase()));

            // Notify back-office only about statuses for switch mortgage provider applications.
            if (Objects.equal(ApplicationType.SWITCH_MORTGAGE_PROVIDER, application.getType())
                    && NOTIFY_BACK_OFFICE_ABOUT_APPLICATION_STATUSES.contains(applicationStatusKey)) {
                notifyBackOfficeAboutApplicationStatus(application, mailSender);
            }
        }

        return HttpResponseHelper.ok();
    }

    @Override
    public User updateUserFlags(String userId, List<String> userFlags) {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        sendFlagsChangesToFirehose(userId, user.getFlags(), userFlags);
        user.setFlags(userFlags);
        userRepository.save(user);
        return user;
    }

    private void sendFlagsChangesToFirehose(String userId, List<String> oldUserFlags, List<String> newUserFlags) {
        List<String> addedFlags = Lists.newArrayList(newUserFlags);
        addedFlags.removeAll(oldUserFlags);
        List<String> removedFlags = Lists.newArrayList(oldUserFlags);
        removedFlags.removeAll(newUserFlags);
        if (!addedFlags.isEmpty()) {
            firehoseQueueProducer.sendUserConfigurationMessage(userId, FirehoseMessage.Type.CREATE, addedFlags, null);
        }
        if (!removedFlags.isEmpty()) {
            firehoseQueueProducer.sendUserConfigurationMessage(userId, FirehoseMessage.Type.DELETE, removedFlags, null);
        }
    }

    private void notifyBackOfficeAboutApplicationStatus(Application application, MailSender mailSender) {

        ServiceConfiguration configuration = serviceContext.getConfiguration();
        BackOfficeConfiguration backOfficeConfiguration = configuration.getBackOffice();

        if (configuration.isDevelopmentMode() || !backOfficeConfiguration.isEnabled()) {
            // Back office is disabled. Don't send any email.
            log.info(UUIDUtils.toTinkUUID(application.getUserId()), "Back-office is disabled. Skipping.");
            return;
        }

        BackOfficeNotificationController controller = new BackOfficeNotificationController(mailSender,
                backOfficeConfiguration);

        if (controller.notifyBackOfficeAboutApplicationStatus(application)) {
            log.info(UUIDUtils.toTinkUUID(application.getUserId()),
                    String.format("Back-office successfully notified of cancellation [applicationId:%s].",
                            UUIDUtils.toTinkUUID(application.getId())));
        } else {
            log.error(UUIDUtils.toTinkUUID(application.getUserId()),
                    String.format("Unable to notify back-office of cancellation [applicationId:%s].",
                            UUIDUtils.toTinkUUID(application.getId())));
        }
    }

    @Override
    public String ping() {
        return "pong";
    }

    @Override
    public void deleteTransaction(@PathParam("userid") String userid,
            @PathParam("transactionid") String transactionid) {
        transactionDao.deleteByUserIdAndTransactionId(userid, transactionid);
    }

    @Override
    public Transaction updateTransaction(Transaction transaction) {
        transactionDao.saveAndIndex(transaction.getUserId(), Lists.newArrayList(transaction), true);
        return transaction;
    }

    @Override
    public void deleteAllActivitiesFor(@PathParam("userid") String userid) {
        activityDao.deleteByUserId(userid);
    }

    @Override
    public void deleteStatistics(@PathParam("userid") String userid) {
        statisticsDao.deleteByUserId(userid);
    }

    @Override
    public void deleteUser(final DeleteUserRequest deleteUserRequest) {
        deleteController.deleteUserAsynchronous(deleteUserRequest);
    }

    @Override
    @Timed
    public UpdateDocumentResponse updateDocument(UpdateDocumentRequest request) {

        /*

        NOTICE:
        With the current set-up (storing documents to C*) we have a hard limit of about ~100MB per partition key.
        The documents table is now set up with only userId as the partition key and hence we should not store
        more than a few documents per user. As of initial development of this document storage, the only use-case
        stores a maximum of one document per user. If the document database will be used for further use-cases,
        the solution might have to be re-thought.

        */

        Optional<CompressedDocument> oldDocument = documentRepository.findOneByUserIdAndIdentifier(
                UUIDUtils.fromTinkUUID(request.getUserId()),
                request.getDocumentContainer().getIdentifier());

        try {
            CompressedDocument document = documentRepository.save(new CompressedDocument(
                    request.getUserId(),
                    request.getDocumentContainer()));

            if (oldDocument.isPresent()) {
                // remove the old document after insertion was OK
                documentRepository.delete(oldDocument.get());
            }

            String format = serviceContext.getConfiguration().isDevelopmentMode() ?
                    "http://www.staging.tink.se/api/v1/documents/%s/%s" :
                    "https://www.tink.se/api/v1/documents/%s/%s";

            String url = String.format(format, request.getUserId(), document.getToken().toString());

            return UpdateDocumentResponse.createSuccessful(document.getIdentifier(), document.getToken(), url);
        } catch (IOException e) {
            log.error(request.getUserId(), "Could not store document", e);
        }

        return UpdateDocumentResponse.createUnSuccessful();
    }

    public void deleteAccount(DeleteAccountRequest deleteAccountRequest) {
        accountController.deleteAccounts(deleteAccountRequest);
    }
}
