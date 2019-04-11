package se.tink.backend.aggregation.agents.abnamro.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.agents.rpc.Account;

public class AbnAmroAgentUtilsTest {
    @Test
    public void testNotSubscribedWhenNullPayload() throws Exception {
        Account account = new Account();
        account.setPayload(null);

        assertThat(AbnAmroAgentUtils.isSubscribed(account)).isFalse();
    }

    @Test
    public void testNotSubscribedWhenNullPayloadValue() throws Exception {
        Account account = new Account();
        account.putPayload(AbnAmroUtils.InternalAccountPayloadKeys.SUBSCRIBED, null);

        assertThat(AbnAmroAgentUtils.isSubscribed(account)).isFalse();
    }

    @Test
    public void testNotSubscribedWhenPayloadIsFalse() throws Exception {
        Account account = new Account();
        account.putPayload(
                AbnAmroUtils.InternalAccountPayloadKeys.SUBSCRIBED, String.valueOf(false));

        assertThat(AbnAmroAgentUtils.isSubscribed(account)).isFalse();
    }

    @Test
    public void testSubscribedWhenPayloadIsTrue() throws Exception {
        Account account = new Account();
        account.putPayload(
                AbnAmroUtils.InternalAccountPayloadKeys.SUBSCRIBED, String.valueOf(true));

        assertThat(AbnAmroAgentUtils.isSubscribed(account)).isTrue();
    }
}
