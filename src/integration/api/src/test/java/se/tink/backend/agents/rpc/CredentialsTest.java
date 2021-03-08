package se.tink.backend.agents.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Map;
import org.junit.Test;

public class CredentialsTest {

    @Test
    public void clearSensitiveInformation() {
        // given
        Credentials credentials = new Credentials();
        Map<String, String> fields =
                new ImmutableMap.Builder<String, String>()
                        .put("field1", "value")
                        .put("field3", "value")
                        .build();
        credentials.setFields(fields);

        Provider provider = new Provider();
        Field field1 =
                Field.builder().description("description").name("field1").value("value").build();
        Field field2 =
                Field.builder().description("description").name("field2").value("value").build();
        provider.setFields(Arrays.asList(field1, field2));

        // when
        credentials.clearSensitiveInformation(provider);

        // then
        assertThat(credentials.getSensitivePayloadSerialized()).isNull();
        Map.Entry<String, String> field1Entry = new SimpleEntry<>("field1", "value");
        assertThat(credentials.getFields()).containsExactly(field1Entry);
    }
}
