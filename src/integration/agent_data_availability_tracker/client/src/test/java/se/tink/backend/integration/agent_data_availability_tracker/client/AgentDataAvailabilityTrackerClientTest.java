package se.tink.backend.integration.agent_data_availability_tracker.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;

@Ignore
public class AgentDataAvailabilityTrackerClientTest {

    private CountDownLatch latch;

    private AgentDataAvailabilityTrackerClientImpl client;

    @Before
    public void setup() throws Exception {
        client = new AgentDataAvailabilityTrackerClientImpl("192.168.99.100", 30789);
        client.start();
    }

    @Test
    public void test() throws Exception {

        final int numClients = 10;
        latch = new CountDownLatch(numClients);

        List<Thread> clients = new ArrayList<>();

        for (int i = 0; i < numClients; i++) {
            Thread client = new Thread(this::spamClientRunnable);
            client.run();
            clients.add(client);
        }

        try {
            latch.await(120, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

            e.printStackTrace();
            Assert.fail();
        }

        System.out.println("Stopping...");
        client.stop();
    }

    private void spamClientRunnable() {

        for (int i = 0; i < 50; i++) {

            client.sendAccount("TestBank", buildAccount(), new AccountFeatures());
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
}
