package se.tink.backend.integration.agent_data_availability_tracker.client;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.grpc.ManagedChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.integration.agent_data_availability_tracker.client.configuration.AgentDataAvailabilityTrackerConfiguration;
import se.tink.backend.integration.agent_data_availability_tracker.module.TlsChannelProvider;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.AccountTrackingSerializer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class AgentDataAvailabilityTrackerClientTest {

    private CountDownLatch latch;

    private AgentDataAvailabilityTrackerClient client;

    @Before
    public void setup() throws Exception {
        String config = "{\"host\": \"192.168.99.100\", \"port\": 30789}";
        AgentDataAvailabilityTrackerConfiguration configuration =
                SerializationUtils.deserializeFromString(
                        config, AgentDataAvailabilityTrackerConfiguration.class);
        Injector injector = Guice.createInjector(new TestModule(configuration));
        client = injector.getInstance(AgentDataAvailabilityTrackerClient.class);
        client.start();
    }

    @Test
    public void test() throws Exception {

        final int numClients = 10;
        latch = new CountDownLatch(numClients);

        for (int i = 0; i < numClients; i++) {
            Thread client = new Thread(this::spamClientRunnable);
            client.start();
        }

        try {
            latch.await(120, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail();
        }

        System.out.println("Stopping...");
        client.stop();
    }

    private void spamClientRunnable() {

        for (int i = 0; i < 50; i++) {
            AccountTrackingSerializer serializer =
                    se.tink.backend.integration.agent_data_availability_tracker.serialization
                            .SerializationUtils.serializeAccount(
                            buildAccount(), new AccountFeatures());

            client.sendAccount("TestBank", "test-test", "SE", serializer);
        }

        System.out.println("Sent 50 accounts.");

        latch.countDown();
    }

    private Account buildAccount() {

        Account account = new Account();

        account.setType(AccountTypes.CHECKING);
        account.setAccountNumber("12345");
        account.setName("AccountName");
        account.setBankId("uid-456-132");
        account.putIdentifier(
                AccountIdentifier.create(AccountIdentifier.Type.IBAN, "DE89370400440532013000"));
        account.putIdentifier(
                AccountIdentifier.create(AccountIdentifier.Type.SORT_CODE, "40478470872490"));
        account.putFlag(AccountFlag.BUSINESS);
        account.putFlag(AccountFlag.MANDATE);

        return account;
    }

    private static class TestModule extends AbstractModule {

        private final AgentDataAvailabilityTrackerConfiguration configuration;

        private TestModule(final AgentDataAvailabilityTrackerConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        protected void configure() {
            bind(AgentDataAvailabilityTrackerClient.class)
                    .to(AgentDataAvailabilityTrackerClientImpl.class);
            bind(ManagedChannel.class).toProvider(TlsChannelProvider.class);
            bind(AgentDataAvailabilityTrackerConfiguration.class).toInstance(configuration);
        }
    }
}
