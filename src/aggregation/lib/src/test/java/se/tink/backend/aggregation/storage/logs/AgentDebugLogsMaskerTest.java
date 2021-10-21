package se.tink.backend.aggregation.storage.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.logmasker.LogMasker;

public class AgentDebugLogsMaskerTest {

    private Credentials credentials;
    private Provider provider;
    private LogMasker logMasker;

    private AgentDebugLogsMasker debugLogsMasker;

    @Before
    public void setup() {
        credentials = mock(Credentials.class);
        provider = mock(Provider.class);
        logMasker = mock(LogMasker.class);

        debugLogsMasker = new AgentDebugLogsMasker(credentials, provider, logMasker);
    }

    @Test
    public void should_mask_all_sensitive_fields_including_blank_values() {
        // given
        mockProviderFieldsNames("field1", "field2", "field3");
        mockCredentialsFields(
                ImmutableMap.of(
                        "field1", "{value1}",
                        "field2", "{value2}",
                        "field3", "  "));

        when(logMasker.mask(any())).thenReturn("MASKED_CONTENT");

        // when
        String maskedLog =
                debugLogsMasker.maskSensitiveOutputLog("{value1}sample{value2}unmasked  content");

        // then
        assertThat(maskedLog).isEqualTo("MASKED_CONTENT");

        verify(logMasker).mask("***field1***sample***field2***unmasked***field3***content");
    }

    @Test
    public void should_ignore_empty_and_null_values() {
        // given
        mockProviderFieldsNames("field1", "field2", "field3");
        mockCredentialsFields(
                ImmutableMap.of(
                        "field1", "{value123}",
                        "field2", ""));

        when(logMasker.mask(any())).thenReturn("MASKED_CONTENT");

        // when
        String maskedLog = debugLogsMasker.maskSensitiveOutputLog("unmasked{value123}content");

        // then
        assertThat(maskedLog).isEqualTo("MASKED_CONTENT");

        verify(logMasker).mask("unmasked***field1***content");
    }

    private void mockProviderFieldsNames(String... fieldNames) {
        List<Field> providerFields =
                Stream.of(fieldNames)
                        .map(
                                fieldName -> {
                                    Field field = mock(Field.class);
                                    when(field.getName()).thenReturn(fieldName);
                                    when(field.getValue())
                                            .thenReturn("some value for " + fieldName);
                                    return field;
                                })
                        .collect(Collectors.toList());
        when(provider.getFields()).thenReturn(providerFields);
    }

    private void mockCredentialsFields(Map<String, String> fields) {
        fields.forEach(
                (fieldName, fieldValue) ->
                        when(credentials.getField(fieldName)).thenReturn(fieldValue));
    }
}
