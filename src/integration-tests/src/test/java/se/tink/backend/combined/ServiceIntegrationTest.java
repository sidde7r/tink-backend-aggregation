package se.tink.backend.combined;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import java.io.File;
import java.util.List;
import java.util.Optional;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.AfterClass;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.AccountBalanceHistoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.FraudItemRepository;
import se.tink.backend.common.repository.mysql.main.StatisticRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.discovery.CoordinationModule;
import se.tink.libraries.discovery.ServiceDiscoveryHelper;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * This class is meant to be used for testing individual services by mocking all other services. The tests do therefore
 * not depend on asynchronous HTTP calls, which in turn makes it possible to avoid Thread.sleep(), or similar solutions,
 * in the tests.
 * TODO this is a unit test
 */
public class ServiceIntegrationTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private static ConfigurationFactory<ServiceConfiguration> CONFIGURATION_FACTORY = new ConfigurationFactory<>(
            ServiceConfiguration.class, VALIDATOR, MAPPER, "");
    protected static final LogUtils log = new LogUtils(ServiceIntegrationTest.class);
    protected static ServiceConfiguration configuration;
    protected static Injector injector;
    protected static WireMockServer server;
    private static List<ServiceDiscoveryHelper> serviceDiscoveryHelpers = Lists.newArrayList();

    protected static void preSetup(String configFilePath, int port, List<String> wantedServices) throws Exception {
        configuration = CONFIGURATION_FACTORY.build(new File(configFilePath));

        server = new WireMockServer(port);

        for (String serviceName : wantedServices) {
            registerService(serviceName, port);
        }

        server.start();
    }

    private static void registerService(String name, Integer port) throws Exception {
        ServiceDiscoveryHelper serviceDiscoveryHelper = new ServiceDiscoveryHelper(
                new CoordinationModule().buildAndStartCuratorFramework(configuration.getCoordination()),
                configuration.getCoordination(),
                name,
                Optional.of(port),
                Optional.empty()
        );

        serviceDiscoveryHelper.start();
        serviceDiscoveryHelpers.add(serviceDiscoveryHelper);
    }

    @AfterClass
    public static void postCleanUp() throws Exception {
        server.stop();
        cleanDB();
        for (ServiceDiscoveryHelper serviceDiscoveryHelper : serviceDiscoveryHelpers) {
            serviceDiscoveryHelper.stop();
        }
    }

    @After
    public void cleanUp() throws Exception {
        WireMock.reset();
    }

    private static void cleanDB() throws Exception {
        injector.getInstance(TransactionDao.class).deleteAll();
        injector.getInstance(StatisticRepository.class).deleteAllInBatch();
        injector.getInstance(AccountBalanceHistoryRepository.class).deleteAll();
        injector.getInstance(UserRepository.class).deleteAllInBatch();
        injector.getInstance(FraudItemRepository.class).deleteAllInBatch();
        injector.getInstance(FraudDetailsRepository.class).deleteAllInBatch();
    }

    public User getTestUser(String testName) {
        UserProfile profile = new UserProfile();
        profile.setCurrency("SEK");
        profile.setPeriodAdjustedDay(25);
        profile.setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);
        profile.setLocale("sv_SE");
        profile.setMarket("SE");

        User user = new User();
        user.setId(StringUtils.generateUUID());
        user.setPassword(testName);
        user.setUsername(testName);
        user.setProfile(profile);

        return user;
    }

    protected Credentials createCredentialsAndSaveToDB(User user, String providerName) {
        Credentials credentials = new Credentials();

        credentials.setUserId(user.getId());
        credentials.setProviderName(providerName);
        credentials.setStatus(CredentialsStatus.CREATED);
        credentials.setType(CredentialsTypes.PASSWORD);

        injector.getInstance(CredentialsRepository.class).save(credentials);
        return credentials;
    }

    protected User createUserAndSaveToDB() {
        User user = getTestUser(RandomStringUtils.randomAlphabetic(10) + "@tink.se");
        injector.getInstance(UserRepository.class).save(user);
        return user;
    }

    protected void successfulPostStubFor(String... paths) {
        for (String path : paths) {
            stubFor(post(urlEqualTo(path)).willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON)));
        }
    }
}
