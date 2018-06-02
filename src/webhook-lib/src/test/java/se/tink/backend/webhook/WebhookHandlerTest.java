package se.tink.backend.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import se.tink.backend.common.providers.OAuth2ClientProvider;
import se.tink.backend.common.repository.mysql.main.OAuth2WebHookRepository;
import se.tink.backend.core.Activity;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.enums.SignableOperationTypes;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.core.oauth2.OAuth2WebHook;
import se.tink.backend.core.oauth2.OAuth2WebHookEvent;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.firehose.v1.queue.FirehoseModelConverters;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.webhook.rpc.WebHookRequest;
import se.tink.backend.webhook.rpc.WebhookActivity;
import se.tink.backend.webhook.rpc.WebhookSignableOperation;
import se.tink.backend.webhook.rpc.WebhookTransaction;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.net.BasicJerseyClientFactory;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebhookHandlerTest {

    @Rule public WireMockRule server = new WireMockRule(WireMockConfiguration.options().dynamicHttpsPort());

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static ModelMapper modelMapper;
    private static WebHookExecutor executor;

    private String url;
    private UUID userId;
    private String clientId;
    private OAuth2WebHookRepository webHookRepositoryMock;
    private WebhookHandler handler;

    @BeforeClass
    public static void beforeClassSetup() {
        modelMapper = new ModelMapper();
        FirehoseModelConverters.addConverters(modelMapper);
        MetricRegistry metricRegistry = mock(MetricRegistry.class);
        when(metricRegistry.meter(any())).thenReturn(mock(Counter.class));
        executor = new WebHookExecutor(new BasicJerseyClientFactory().createCookieClientWithoutSSL(),
                StopStrategies.stopAfterAttempt(10), WaitStrategies.fixedWait(7, TimeUnit.SECONDS), metricRegistry);
    }

    @Before
    public void setUp() throws JsonProcessingException {
        url = "https://localhost:" + server.httpsPort();
        userId = UUID.randomUUID();
        clientId = "testClientId";

        webHookRepositoryMock = mock(OAuth2WebHookRepository.class);

        OAuth2Client oAuth2Client = new OAuth2Client();
        oAuth2Client.setScope(OAuth2AuthorizationScopeTypes.USER_WEB_HOOKS);
        oAuth2Client.setPayloadSerialized(new ObjectMapper().writeValueAsString(ImmutableMap.of(
                OAuth2Client.PayloadKey.WEBHOOK_DOMAINS, url)));

        MetricRegistry metricRegistryMock = mock(MetricRegistry.class);
        when(metricRegistryMock.meter(any())).thenReturn(mock(Counter.class));
        handler = new WebhookHandler(webHookRepositoryMock, getoAuth2ClientProvider(oAuth2Client), executor,
                metricRegistryMock);
    }

    @Test
    public void signableOperationFirehoseMessage_generatesWebhook_withCorrectObject() {
        SignableOperation signableOperation = getTestSignableOperation();

        // Convert the signable operation to a Firehose message.
        FirehoseMessage firehoseMessage = FirehoseMessage.newBuilder()
                .setType(FirehoseMessage.Type.UPDATE)
                .setUserId(UUIDUtils.toTinkUUID(userId))
                .addSignableOperations(mapToFirehose(signableOperation))
                .build();

        OAuth2WebHook oAuth2WebHook = getOAuth2WebHook(Sets.newHashSet(OAuth2WebHookEvent.SIGNABLE_OPERATION_UPDATE));
        when(webHookRepositoryMock.findByUserIdAndClientId(eq(UUIDUtils.toTinkUUID(userId)), eq(clientId)))
                .thenReturn(Lists.newArrayList(oAuth2WebHook));
        successfulPostStubFor("/");

        // Execute the Firehose message.
        handler.handle(firehoseMessage, Instant.now());

        verify(1, postRequestedFor(urlEqualTo("/")));

        // Verify that we get the same object back from the webhook.
        LoggedRequest request = findAll(postRequestedFor(urlEqualTo("/"))).get(0);
        WebHookRequest webHookRequest = SerializationUtils
                .deserializeFromString(request.getBodyAsString(), WebHookRequest.class);
        WebhookSignableOperation content = MAPPER
                .convertValue(webHookRequest.getContent(), WebhookSignableOperation.class);
        verifyEqual(signableOperation, content);
    }

    @Test
    public void transactionFirehoseMessage_generatesWebhook_withCorrectObject() {
        Transaction transaction = getTestTransaction();

        // Convert the transaction to a Firehose message.
        FirehoseMessage firehoseMessage = FirehoseMessage.newBuilder()
                .setType(FirehoseMessage.Type.UPDATE)
                .addTransactions(mapToTransaction(getTestTransaction()))
                .setUserId(UUIDUtils.toTinkUUID(userId))
                .build();

        OAuth2WebHook oAuth2WebHook = getOAuth2WebHook(Sets.newHashSet(OAuth2WebHookEvent.TRANSACTION_UPDATE));
        when(webHookRepositoryMock.findByUserIdAndClientId(eq(UUIDUtils.toTinkUUID(userId)), eq(clientId)))
                .thenReturn(Lists.newArrayList(oAuth2WebHook));
        successfulPostStubFor("/");

        // Execute the Firehose message.
        handler.handle(firehoseMessage, Instant.now());

        verify(1, postRequestedFor(urlEqualTo("/")));

        // Verify that we get the same object back from the webhook.
        LoggedRequest request = findAll(postRequestedFor(urlEqualTo("/"))).get(0);
        WebHookRequest webHookRequest = SerializationUtils
                .deserializeFromString(request.getBodyAsString(), WebHookRequest.class);
        WebhookTransaction content = MAPPER.convertValue(webHookRequest.getContent(), WebhookTransaction.class);
        verifyEqual(transaction, content);
    }

    @Test
    public void transactionFirehoseMessage_webhookWithoutTransactionEvent_doesNotGenerateWebhook() {

        // Convert the transaction to a Firehose message.
        FirehoseMessage firehoseMessage = FirehoseMessage.newBuilder()
                .setType(FirehoseMessage.Type.UPDATE)
                .addTransactions(mapToTransaction(getTestTransaction()))
                .setUserId(UUIDUtils.toTinkUUID(userId))
                .build();

        OAuth2WebHook oAuth2WebHook = getOAuth2WebHook(Sets.newHashSet(OAuth2WebHookEvent.SIGNABLE_OPERATION_UPDATE));
        when(webHookRepositoryMock.findByUserIdAndClientId(eq(UUIDUtils.toTinkUUID(userId)), eq(clientId)))
                .thenReturn(Lists.newArrayList(oAuth2WebHook));
        successfulPostStubFor("/");

        // Execute the Firehose message.
        handler.handle(firehoseMessage, Instant.now());

        verify(0, postRequestedFor(urlEqualTo("/")));
    }

    @Test
    public void activityFirehoseMessage_generatesWebhook_withCorrectObject() {
        Activity activity = getTestActivity();

        // Convert the activity to a Firehose message.
        FirehoseMessage firehoseMessage = FirehoseMessage.newBuilder()
                .setType(FirehoseMessage.Type.UPDATE)
                .setUserId(UUIDUtils.toTinkUUID(userId))
                .addActivities(mapToActivity(activity))
                .build();

        OAuth2WebHook oAuth2WebHook = getOAuth2WebHook(Sets.newHashSet(OAuth2WebHookEvent.ACTIVITY_UPDATE));
        when(webHookRepositoryMock.findByUserIdAndClientId(eq(UUIDUtils.toTinkUUID(userId)), eq(clientId)))
                .thenReturn(Lists.newArrayList(oAuth2WebHook));
        successfulPostStubFor("/");

        // Execute the Firehose message.
        handler.handle(firehoseMessage, Instant.now());

        verify(1, postRequestedFor(urlEqualTo("/")));

        // Verify that we get the same object back from the webhook.
        LoggedRequest request = findAll(postRequestedFor(urlEqualTo("/"))).get(0);
        WebHookRequest webHookRequest = SerializationUtils
                .deserializeFromString(request.getBodyAsString(), WebHookRequest.class);
        WebhookActivity content = MAPPER.convertValue(webHookRequest.getContent(), WebhookActivity.class);
        verifyEqual(activity, content);
    }

    private se.tink.backend.firehose.v1.models.Activity mapToActivity(Activity activity) {
        return FirehoseModelConverters.fromCoreToFirehose(activity);
    }

    private se.tink.backend.firehose.v1.models.Transaction.Builder mapToTransaction(Transaction transaction) {
        return modelMapper.map(transaction, se.tink.backend.firehose.v1.models.Transaction.Builder.class);
    }

    private se.tink.backend.firehose.v1.models.SignableOperation.Builder mapToFirehose(
            SignableOperation signableOperation) {
        return modelMapper.map(signableOperation, se.tink.backend.firehose.v1.models.SignableOperation.Builder.class);
    }

    private void verifyEqual(Activity activity, WebhookActivity content) {
        assertEquals(activity.getId(), content.getId());
        assertEquals(activity.getDate().getTime(), content.getDate());
        assertEquals(activity.getKey(), content.getKey());
        assertEquals(activity.getMessage(), content.getMessage());
        assertEquals(activity.getImportance(), content.getImportance(), 0.001);
        assertEquals(activity.getTitle(), content.getTitle());
        assertEquals(activity.getType(), content.getType());
        assertEquals(activity.getUserId(), content.getUserId());
    }

    private void verifyEqual(SignableOperation signableOperation, WebhookSignableOperation content) {
        assertEquals(UUIDUtils.toTinkUUID(signableOperation.getId()), content.getId());
        assertEquals(signableOperation.getCreated().getTime(), content.getCreated());
        assertEquals(signableOperation.getUpdated().getTime(), content.getUpdated());
        assertEquals(signableOperation.getStatus().name(), content.getStatus());
        assertEquals(signableOperation.getType().name(), content.getType());
        assertEquals(signableOperation.getStatusMessage(), content.getStatusMessage());
        assertEquals(UUIDUtils.toTinkUUID(signableOperation.getUnderlyingId()), content.getUnderlyingId());
        assertEquals(UUIDUtils.toTinkUUID(signableOperation.getUserId()), content.getUserId());
        assertEquals(UUIDUtils.toTinkUUID(signableOperation.getCredentialsId()), content.getCredentialsId());
    }

    private void verifyEqual(Transaction transaction, WebhookTransaction content) {
        assertEquals(transaction.getAccountId(), content.getAccountId());
        assertEquals(transaction.getAmount(), content.getAmount(), 0.001);
        assertEquals(transaction.getCategoryId(), content.getCategoryId());
        assertEquals(transaction.getCategoryType().name(), content.getCategoryType());
        assertEquals(transaction.getDate().getTime(), content.getDate());
        assertEquals(transaction.getDescription(), content.getDescription());
        assertEquals(transaction.getLastModified().getTime(), content.getLastModified());
        assertEquals(transaction.getOriginalAmount(), content.getOriginalAmount(), 0.001);
        assertEquals(transaction.getNotes(), content.getNotes());
        assertEquals(transaction.getOriginalDate().getTime(), content.getOriginalDate());
        assertEquals(transaction.getOriginalDescription(), content.getOriginalDescription());
        assertEquals(transaction.isPending(), content.isPending());
        assertEquals(transaction.getTimestamp(), content.getTimestamp());
        assertEquals(transaction.getType().name(), content.getType());
        assertEquals(transaction.getUserId(), content.getUserId());
        assertEquals(transaction.getMerchantId(), content.getMerchantId());
        assertEquals(transaction.isUpcoming(), content.isUpcoming());
        assertEquals(transaction.getPayload().get(TransactionPayloadTypes.EXTERNAL_ID), content.getExternalId());
    }

    private OAuth2WebHook getOAuth2WebHook(Set<String> events) {
        OAuth2WebHook oAuth2WebHook = new OAuth2WebHook();

        oAuth2WebHook.setUrl(url);
        oAuth2WebHook.setClientId(clientId);
        oAuth2WebHook.setSecret("testSecret");
        oAuth2WebHook.setEvents(events);
        oAuth2WebHook.setUserId(UUIDUtils.toTinkUUID(userId));

        return oAuth2WebHook;
    }

    private OAuth2ClientProvider getoAuth2ClientProvider(OAuth2Client oAuth2Client) {
        OAuth2ClientProvider oAuth2ClientProviderMock = mock(OAuth2ClientProvider.class);
        ImmutableMap<String, OAuth2Client> clientsById = ImmutableMap.of(clientId, oAuth2Client);
        when(oAuth2ClientProviderMock.get()).thenReturn(clientsById);
        return oAuth2ClientProviderMock;
    }

    private void successfulPostStubFor(String... paths) {
        for (String path : paths) {
            server.stubFor(post(urlEqualTo(path)).willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON)));
        }
    }

    private Activity getTestActivity() {
        Activity activity = new Activity();

        activity.setKey("testKey");
        activity.setUserId(UUIDUtils.toTinkUUID(userId));
        activity.setType(se.tink.backend.core.Activity.Types.DOUBLE_CHARGE);
        activity.setDate(new Date());
        activity.setId(UUIDUtils.generateUUID());
        activity.setImportance(0.9);
        activity.setMessage("You were double charged");
        activity.setTitle("Double charge");

        return activity;
    }

    private SignableOperation getTestSignableOperation() {
        SignableOperation signableOperation = new SignableOperation();

        signableOperation.setCreated(DateUtils.parseDate("2017-05-01"));
        signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
        signableOperation.setStatusDetailsKey(
                se.tink.backend.core.signableoperation.SignableOperation.StatusDetailsKey.BANKID_FAILED);
        signableOperation.setStatusMessage("The transfer has been sent to your bank.");
        signableOperation.setType(SignableOperationTypes.TRANSFER);
        signableOperation.setUnderlyingId(UUID.randomUUID());
        signableOperation.setUpdated(DateUtils.parseDate("2017-07-01"));
        signableOperation.setUserId(userId);
        signableOperation.setCredentialsId(UUID.randomUUID());
        signableOperation.setSignableObject("signableObject");

        return signableOperation;
    }

    private Transaction getTestTransaction() {
        Transaction transaction = new Transaction();

        transaction.setAccountId("accountId");
        transaction.setAmount(1000.);
        transaction.setCategory("categoryId", CategoryTypes.INCOME);
        transaction.setDate(DateUtils.parseDate("2017-06-01"));
        transaction.setDescription("description");
        transaction.setFormattedDescription("formattedDescription");
        transaction.setLastModified(DateUtils.parseDate("2017-05-01"));
        transaction.setOriginalAmount(1001);
        transaction.setNotes("notes");
        transaction.setOriginalDate(DateUtils.parseDate("2017-05-11"));
        transaction.setOriginalDescription("originalDescription");
        transaction.setPayload(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID, "transferId");
        transaction.setPayload(TransactionPayloadTypes.EXTERNAL_ID, "123");
        transaction.setPending(true);
        transaction.setTimestamp(1000);
        transaction.setType(TransactionTypes.PAYMENT);
        transaction.setUserId(UUIDUtils.toTinkUUID(userId));
        transaction.setCredentialsId("credentialsId");
        transaction.setMerchantId("merchantId");
        transaction.setUpcoming(true);
        return transaction;
    }
}
