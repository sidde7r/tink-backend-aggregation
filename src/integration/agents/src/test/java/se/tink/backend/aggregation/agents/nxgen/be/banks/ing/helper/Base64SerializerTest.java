package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonGenerator;
import java.util.Base64;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class Base64SerializerTest {

    private final Base64Serializer base64Serializer = new Base64Serializer();

    private final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

    @Test
    public void shouldSerializeStringWithoutQuotes() throws Exception {
        JsonGenerator mockedGenerator = mock(JsonGenerator.class);

        base64Serializer.serialize("exampleString", mockedGenerator, null);

        verify(mockedGenerator).writeString(captor.capture());

        String value = captor.getValue();
        assertThat(value).isEqualTo(Base64.getEncoder().encodeToString("exampleString".getBytes()));
    }
}
