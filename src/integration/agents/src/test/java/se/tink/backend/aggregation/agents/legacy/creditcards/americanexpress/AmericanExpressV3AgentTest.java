package se.tink.backend.aggregation.agents.creditcards.americanexpress;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.util.Collections;
import java.util.Date;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.AmericanExpressV3Agent;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.AmericanExpressV3ApiClient;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.LoginRequest;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.TimelineRequest;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.TransactionsRequest;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.framework.legacy.AbstractAgentTest;
import se.tink.backend.aggregation.agents.utils.mappers.CoreUserMapper;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.account_data_cache.AccountDataCache;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.user.rpc.User;

public class AmericanExpressV3AgentTest extends AbstractAgentTest<AmericanExpressV3Agent> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ThreadSafeDateFormat LOCAL_TIME_FORMAT =
            new ThreadSafeDateFormat("MM-dd-YYYY'T'HH:mm:ss");
    private static final String BASE_URL =
            "https://global.americanexpress.com/myca/intl/moblclient/emea/svc";

    private AgentTestContext context;
    private Client client;
    private Provider provider = new Provider();

    public AmericanExpressV3AgentTest() {
        super(AmericanExpressV3Agent.class);
    }

    @Override
    protected Provider constructProvider() {
        return provider;
    }

    @Test
    @Ignore("Broken test")
    public void testSEUser1() throws Exception {
        provider.setMarket("SE");
        testAgent(
                CommonAmericanExpress.USER1.getUsername(),
                CommonAmericanExpress.USER1.getPassword());
    }

    @Test
    @Ignore("Broken test")
    public void testSEUser2() throws Exception {
        provider.setMarket("SE");
        testAgent(
                CommonAmericanExpress.USER2.getUsername(),
                CommonAmericanExpress.USER2.getPassword());
    }

    @Test
    @Ignore("Broken test")
    public void testWithMockedResponseData() throws Exception {
        AmericanExpressV3Agent agent = instantiateAgentWithMockedResponses();

        agent.login();
        agent.refresh();

        AccountDataCache accountDataCache = context.getAccountDataCache();
        int count = accountDataCache.getTransactionsToBeProcessed().size();

        System.out.println(
                MAPPER.writeValueAsString(
                        accountDataCache.getTransactionsByAccountToBeProcessed()));
        System.out.println("Number of transactions: " + count);
    }

    private AmericanExpressV3Agent instantiateAgentWithMockedResponses() {
        client = mock(Client.class);

        mockLoginRequest();
        mockTimelineRequest(0);
        mockTimelineRequest(1);
        mockTransactionsRequest(0, 0);
        mockTransactionsRequest(1, 0);
        mockTransactionsRequest(1, 1);

        User user = new User();
        Credentials credentials = new Credentials();
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserAvailableForInteraction(true);
        userAvailability.setUserPresent(true);
        userAvailability.setOriginatingUserIp("127.0.0.1");
        String originatingUserIp = "127.0.0.1";

        context = new AgentTestContext(credentials);
        CredentialsRequest request =
                RefreshInformationRequest.builder()
                        .user(CoreUserMapper.toAggregationUser(user))
                        .provider(provider)
                        .credentials(credentials)
                        .userAvailability(userAvailability)
                        .originatingUserIp(originatingUserIp)
                        .manual(true)
                        .forceAuthenticate(false)
                        .build();
        AmericanExpressV3ApiClient apiClient =
                new AmericanExpressV3ApiClient(client, "SE", "Tink", credentials);

        return new AmericanExpressV3Agent(request, context, new SignatureKeyPair(), apiClient);
    }

    private String mockTransactionsResponse(int sortedIndex, int billingIndex) {
        if (sortedIndex == 0 && billingIndex == 0) {
            return "Replace with transactionDetails response data for sorted index 0 and billing index 0";

            // Add more billing index response data for sorted index 0 if needed
        }

        if (sortedIndex == 1) {
            if (billingIndex == 0) {
                return "Replace with transactionDetails response data for sorted index 1 and billing index 0";
            } else if (billingIndex == 1) {
                return "Replace with transactionDetails response data for sorted index 1 and billing index 1";
            }

            // Add more billing index response data for sorted index 1 if needed
        }

        // Add more sorted index response data if needed
        return "{\"transactionDetails\":{\"status\":0,\"messageType\":\"ERROR\",\"cardList\":[]}}";
    }

    private String mockTimelineResponse(int sortedIndex) {
        if (sortedIndex == 0) {
            return "Replace with timeline response data for sortedIndex 0";
        }

        return "Replace with timeline response data for sortedIndex 1";

        // Add more index responses if needed
    }

    private String mockLoginResponse() {
        return "Replace with login response data";
    }

    private void mockLoginRequest() {
        WebResource.Builder builder = mockResource("/v1/loginSummary.do");

        when(builder.post(eq(String.class), any(LoginRequest.class)))
                .thenReturn(mockLoginResponse());
    }

    private void mockTransactionsRequest(int sortedIndex, int billingIndex) {
        TransactionsRequest transactionsRequest = new TransactionsRequest();
        transactionsRequest.setBillingIndexList(
                Collections.singletonList(Integer.toString(billingIndex)));
        transactionsRequest.setSortedIndex(sortedIndex);

        WebResource.Builder builder = mockResource("/v1/transaction.do");
        when(builder.post(eq(String.class), any(TransactionsRequest.class)))
                .thenAnswer(
                        invocationOnMock -> {
                            TransactionsRequest request = invocationOnMock.getArgument(1);
                            return mockTransactionsResponse(
                                    request.getSortedIndex(),
                                    Integer.valueOf(request.getBillingIndexList().get(0)));
                        });
    }

    private void mockTimelineRequest(int sortedIndex) {
        WebResource.Builder builder = mockResource("/v2/timeline.do");

        Date date = new Date();
        TimelineRequest timelineRequest = new TimelineRequest();
        timelineRequest.setTimeZone("CEST");
        timelineRequest.setLocalTime(LOCAL_TIME_FORMAT.format(date));
        timelineRequest.setTimeZoneOffset("7200000");
        timelineRequest.setSortedIndex(sortedIndex);
        timelineRequest.setPendingChargeEnabled(true);

        when(builder.post(eq(String.class), any(TimelineRequest.class)))
                .thenAnswer(
                        invocationOnMock -> {
                            TimelineRequest request = invocationOnMock.getArgument(1);
                            return mockTimelineResponse(request.getSortedIndex());
                        });
    }

    private WebResource.Builder mockResource(String url) {
        WebResource resource = mock(WebResource.class);
        when(client.resource(BASE_URL + url)).thenReturn(resource);

        WebResource.Builder builder = mock(WebResource.Builder.class);
        when(resource.header(anyString(), anyString())).thenReturn(builder);
        when(builder.header(anyString(), anyString())).thenReturn(builder);
        when(builder.accept(anyString())).thenReturn(builder);
        when(builder.type(anyString())).thenReturn(builder);

        return builder;
    }
}
