package se.tink.backend.system.cli.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Map;
import java.util.Optional;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.rpc.SeedPersonDataRequest;
import se.tink.backend.aggregation.rpc.SeedPersonDataResponse;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.i18n.SocialSecurityNumber.Sweden;
import se.tink.backend.common.repository.cassandra.CassandraUserDemographicsRepository;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.FollowItemRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.DeletedUserRepository;
import se.tink.backend.common.repository.mysql.main.UserDemographicsRepository;
import se.tink.backend.common.repository.mysql.main.UserFacebookProfileRepository;
import se.tink.backend.common.repository.mysql.main.UserOriginRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.utils.CassandraUserDemographicsConverter;
import se.tink.backend.core.DeletedUser;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.Market;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.core.UserDemographics;
import se.tink.backend.firehose.v1.queue.DummyFirehoseQueueProducer;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserIdTraverser;
import se.tink.backend.system.workers.statistics.StatisticsGeneratorWorker;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.MetricRegistry;

public class CalculateUserDemographicsCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(CalculateUserDemographicsCommand.class);
    private static ObjectMapper mapper = new ObjectMapper();

    public CalculateUserDemographicsCommand() {
        super("calculate-user-demographics", "Calculates all item demographics values");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        log.info("Calculating item demographics.");

        final String market = System.getProperty("market", "all");

        final StatisticsGeneratorWorker statisticsWorker = new StatisticsGeneratorWorker(serviceContext,
                new DummyFirehoseQueueProducer(), injector.getInstance(MetricRegistry.class));

        final CredentialsEventRepository credentialsEventRepository = serviceContext
                .getRepository(CredentialsEventRepository.class);
        final CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        final DeletedUserRepository deletedUserRepository = serviceContext
                .getRepository(DeletedUserRepository.class);
        final FraudDetailsRepository fraudDetailsRepository = serviceContext
                .getRepository(FraudDetailsRepository.class);
        final TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
        final UserDemographicsRepository userDemographicsRepository = serviceContext
                .getRepository(UserDemographicsRepository.class);
        final CassandraUserDemographicsRepository cassandraUserDemographicsRepository = serviceContext
                .getRepository(CassandraUserDemographicsRepository.class);
        final UserFacebookProfileRepository userFacebookProfileRepository = serviceContext
                .getRepository(UserFacebookProfileRepository.class);
        final UserOriginRepository userOriginRepository = serviceContext.getRepository(UserOriginRepository.class);
        final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        final UserStateRepository userStateRepository = serviceContext.getRepository(UserStateRepository.class);
        final AggregationControllerCommonClient aggregationControllerCommonClient = serviceContext
                .getAggregationControllerCommonClient();
        final AggregationServiceFactory aggregationServiceFactory = serviceContext.getAggregationServiceFactory();
        final FollowItemRepository followItemsRepository = serviceContext.getRepository(FollowItemRepository.class);
        final ProviderRepository providerRepository = serviceContext.getRepository(ProviderRepository.class);

        final Map<String, Provider> providersByName;
        if (serviceContext.isProvidersOnAggregation()) {
            providersByName = Maps.uniqueIndex(aggregationControllerCommonClient.listProviders(), Provider::getName);
        } else {
            providersByName = Maps.uniqueIndex(providerRepository.findAll(), Provider::getName);
        }


        log.info("Calculating item demographics for users.");

        rx.Observable<String> existingAndDeletedUsers = rx.Observable
                .concat(
                        // Go through the deletedUsers before streaming users to avoid holding them in memory while
                        // iteratingover all the other users.
                        rx.Observable.from(deletedUserRepository.findAll())
                                .filter(input11 -> !Strings.isNullOrEmpty(input11.getUsernameHash()))
                                .map(DeletedUser::getUserId),
                        userRepository.streamAll()
                                .filter(u -> Objects.equal(market, "all") || Objects
                                        .equal(u.getProfile().getMarket(), market))
                                .map(User::getId)
                )
                .compose(new CommandLineInterfaceUserIdTraverser(10));

        existingAndDeletedUsers.forEach((userId) -> {
            try {

                UserData userData = new UserData();

                userData.setTransactions(transactionDao.findAllByUserId(userId));
                userData.setCredentials(credentialsRepository.findAllByUserId(userId));
                userData.setUser(userRepository.findOne(userId));
                userData.setUserOrigin(userOriginRepository.findOneByUserId(userId));
                userData.setUserState(userStateRepository.findOne(userId));
                userData.setCredentialsEvents(Lists.newArrayList(Iterables.concat(Iterables.transform(
                        credentialsRepository.findAllByUserId(userId),
                        input -> credentialsEventRepository
                                .findByUserIdAndCredentialsId(input.getUserId(), input.getId())))));
                userData.setUserFacebookProfile(userFacebookProfileRepository.findByUserId(userId));
                userData.setFraudDetails(fraudDetailsRepository.findAllByUserId(userId));
                userData.setFollowItems(followItemsRepository.findByUserId(userId));

                UserDemographics oldDemographics = userDemographicsRepository.findOne(userId);

                if (oldDemographics == null) {
                    oldDemographics = new UserDemographics();
                    oldDemographics.setUserId(userId);
                }

                UserDemographics userDemographics = statisticsWorker.calculateUserDemographics(
                        oldDemographics, userData,
                        Optional.ofNullable(deletedUserRepository.findOneByUserId(oldDemographics.getUserId())),
                        providersByName);

                // Seed address information for SE users from Creditsafe if not set.

                if (userData.getUser() != null && Objects
                        .equal(userData.getUser().getProfile().getMarket(), Market.Code.SE.toString())
                        && (Strings.isNullOrEmpty(userDemographics.getPostalCode())
                        || Strings.isNullOrEmpty(userDemographics.getCommunity()))) {

                    Sweden personNumber = Sweden.findPersonNumberFromCredentials(userData
                            .getCredentials());

                    if (personNumber != null) {
                        log.info("\t\tSeeding item data from Creditsafe for userId: " + userId);

                        SeedPersonDataRequest request = new SeedPersonDataRequest();
                        request.setPersonNumner(personNumber.asString());

                        SeedPersonDataResponse response = aggregationServiceFactory.getCreditSafeService()
                                .seedPersonData(request);

                        if (response == null || response.getFraudDetailsContent() == null) {
                            log.info("\t\tNo fraud detials seeded: " + mapper.writeValueAsString(response));
                        } else {
                            FraudDetailsContent addressDetails = Iterables.find(response.getFraudDetailsContent(),
                                    input -> input.getContentType() == FraudDetailsContentType.ADDRESS, null);

                            if (addressDetails != null) {
                                FraudAddressContent content = (FraudAddressContent) addressDetails;
                                userDemographics.setPostalCode(content.getPostalcode());
                                userDemographics.setCity(content.getCity());
                                userDemographics.setCommunity(content.getCommunity());
                            }
                        }
                    }
                }

                userDemographicsRepository.save(userDemographics);

                // Save to Cassandra
                cassandraUserDemographicsRepository.save(CassandraUserDemographicsConverter
                        .toCassandra(userDemographics));

            } catch (Exception e) {
                log.error("Failed to calculate demographics for userId: " + userId, e);
            }
        });

    }

}
