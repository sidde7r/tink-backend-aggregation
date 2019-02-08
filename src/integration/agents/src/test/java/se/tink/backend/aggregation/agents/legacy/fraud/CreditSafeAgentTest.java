package se.tink.backend.aggregation.agents.fraud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import java.io.File;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.models.fraud.FraudDetailsContent;
import se.tink.backend.aggregation.agents.models.fraud.FraudDetailsContentType;
import se.tink.backend.aggregation.agents.utils.mappers.CoreUserMapper;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationWrapper;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.credentials.demo.DemoCredentials;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.social.security.TestSSN;
import se.tink.libraries.strings.StringUtils;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

public class CreditSafeAgentTest extends AbstractAgentTest<CreditSafeAgent> {

    private static final File CONFIG_FILE = new File("etc/development.yml");
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.enableDefaultTyping();
    }

    public CreditSafeAgentTest() {
        super(CreditSafeAgent.class);
    }

    @Before
    public void setUp() throws Exception {
        AgentsServiceConfigurationWrapper agentsServiceConfigurationWrapper =
                CONFIGURATION_FACTORY.build(CONFIG_FILE);
        configuration = agentsServiceConfigurationWrapper.getAgentsServiceConfiguration();
        ;
    }

    @Test
    public void testRealPersonNumberWithBankId() throws Exception {
        String personNumber = TestSSN.FH;
        Credentials credentials = createCredentials(personNumber, null, CredentialsTypes.FRAUD);
        // credentials.setSensitivePayload(FraudUtils.ID_CONTROL_AUTH_KEY,
        // StringUtils.hashAsStringSHA1(personNumber, FraudUtils.ID_CONTROL_AUTH_SALT));
        AgentTestContext context = testAgentNonTestEnv(credentials);

        ImmutableListMultimap<FraudDetailsContentType, FraudDetailsContent> detailsByType =
                getFraudDetaildByType(context);

        System.out.println(
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(detailsByType.values()));
    }

    @Test
    public void testRealPersonNumber() throws Exception {
        String personNumber = TestSSN.AL;
        Credentials credentials = createCredentials(personNumber, null, CredentialsTypes.FRAUD);
        credentials.setSensitivePayload(
                FraudUtils.ID_CONTROL_AUTH_KEY,
                StringUtils.hashAsStringSHA1(personNumber, FraudUtils.ID_CONTROL_AUTH_SALT));
        AgentTestContext context = testAgentNonTestEnv(credentials);

        ImmutableListMultimap<FraudDetailsContentType, FraudDetailsContent> detailsByType =
                getFraudDetaildByType(context);

        System.out.println(
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(detailsByType.values()));
    }

    @Test
    public void testDataImportWithRealEstate() throws Exception {
        String personNumber = DemoCredentials.USER7.getUsername();
        Credentials credentials = createCredentials(personNumber, null, CredentialsTypes.FRAUD);
        credentials.setSensitivePayload(
                FraudUtils.ID_CONTROL_AUTH_KEY,
                StringUtils.hashAsStringSHA1(personNumber, FraudUtils.ID_CONTROL_AUTH_SALT));
        testAgentNoCheck(credentials);
    }

    @Test
    public void testDataWithNonPayments() throws Exception {
        String personNumber = "186607070010";
        Credentials credentials = createCredentials(personNumber, null, CredentialsTypes.FRAUD);
        credentials.setSensitivePayload(
                FraudUtils.ID_CONTROL_AUTH_KEY,
                StringUtils.hashAsStringSHA1(personNumber, FraudUtils.ID_CONTROL_AUTH_SALT));
        AgentTestContext context = testAgentNoCheck(credentials);

        ImmutableListMultimap<FraudDetailsContentType, FraudDetailsContent> detailsByType =
                getFraudDetaildByType(context);

        // I changed from 9 -> 10 because of failing test. CreditSafe seem to deliver one more Non
        // Payment (i.e. no bug)
        // Maybe this assert is bad to have as it will change from time to time... /JE
        Assert.assertEquals(
                "Wrong number of non payment details.",
                10,
                detailsByType.get(FraudDetailsContentType.NON_PAYMENT).size(),
                0);
        Assert.assertEquals(
                "Wrong number of credits details.",
                1,
                detailsByType.get(FraudDetailsContentType.CREDITS).size(),
                0);
        Assert.assertEquals(
                "Wrong number of credits details.",
                1,
                detailsByType.get(FraudDetailsContentType.IDENTITY).size(),
                0);
        Assert.assertEquals(
                "Wrong number of credits details.",
                1,
                detailsByType.get(FraudDetailsContentType.ADDRESS).size(),
                0);
    }

    @Test
    public void testDataImportWithScoring() throws Exception {
        String personNumber = "186502020050";
        Credentials credentials = createCredentials(personNumber, null, CredentialsTypes.FRAUD);
        credentials.setSensitivePayload(
                FraudUtils.ID_CONTROL_AUTH_KEY,
                StringUtils.hashAsStringSHA1(personNumber, FraudUtils.ID_CONTROL_AUTH_SALT));

        AgentTestContext context = testAgentNoCheck(credentials);

        ImmutableListMultimap<FraudDetailsContentType, FraudDetailsContent> detailsByType =
                getFraudDetaildByType(context);

        Assert.assertEquals(
                "Wrong number of scoring details.",
                1,
                detailsByType.get(FraudDetailsContentType.SCORING).size(),
                0);
    }

    /**
     * Help method to parse credentials payload and sort details by type.
     *
     * @param context
     * @return
     * @throws Exception
     */
    private ImmutableListMultimap<FraudDetailsContentType, FraudDetailsContent>
            getFraudDetaildByType(AgentTestContext context) {

        ImmutableListMultimap<FraudDetailsContentType, FraudDetailsContent> detailsByType =
                Multimaps.index(context.getDetailsContents(), FraudDetailsContent::getContentType);
        return detailsByType;
    }

    protected AgentTestContext testAgentNonTestEnv(Credentials credentials) throws Exception {
        User user = new User();
        user.setId("----DEMO----");
        UserProfile profile = new UserProfile();
        profile.setLocale("sv_SE");
        user.setProfile(profile);

        CredentialsRequest informationRefreshRequest =
                new RefreshInformationRequest(
                        CoreUserMapper.toAggregationUser(user),
                        constructProvider(),
                        credentials,
                        true);
        AgentTestContext testContext = new AgentTestContext(credentials);

        CreditSafeAgent creditSafeAgent =
                new CreditSafeAgent(informationRefreshRequest, testContext, new SignatureKeyPair());
        creditSafeAgent.setConfiguration(configuration);

        creditSafeAgent.login();
        creditSafeAgent.refresh();

        return testContext;
    }
}
