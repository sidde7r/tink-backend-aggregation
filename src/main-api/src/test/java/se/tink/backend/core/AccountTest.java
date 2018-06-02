package se.tink.backend.core;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountTest {

    @Test
    public void testPutPayloadWhenPayloadIsNull() {
        Account account = new Account();

        account.putPayload("key", "value");

        assertThat(account.getPayload("key")).isEqualTo("value");
    }

    @Test
    public void testPutPayloadWhenPayloadIsNotNull() {
        Account account = new Account();

        account.putPayload("key1", "value1");
        account.putPayload("key2", "value2");

        assertThat(account.getPayload("key1")).isEqualTo("value1");
        assertThat(account.getPayload("key2")).isEqualTo("value2");
    }

    @Test
    public void testRemovePayload() {
        Account account = new Account();

        assertThat(account.getPayloadAsMap()).isEmpty();

        account.putPayload("foo", "bar");

        account.removePayload("foo");

        assertThat(account.getPayloadAsMap()).isEmpty();
    }

    @Test
    public void testRemovePayloadWhenNull() {
        Account account = new Account();

        account.removePayload("foo");

        assertThat(account.getPayloadAsMap()).isEmpty();
    }
}
