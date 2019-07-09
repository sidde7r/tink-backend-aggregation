package se.tink.backend.integration.agentcapabilitytracker.transmitter;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;

@Ignore
public class CapabilityTransmitterTest {

    @Test
    public void test() {

        CapabilityTransmitter transmitter = new CapabilityTransmitter("192.168.99.100", 30789);

        transmitter.beginStream();
        transmitter.sendAccount("TestBank", buildAccount(), new AccountFeatures());
        transmitter.endStreamBlocking();
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
