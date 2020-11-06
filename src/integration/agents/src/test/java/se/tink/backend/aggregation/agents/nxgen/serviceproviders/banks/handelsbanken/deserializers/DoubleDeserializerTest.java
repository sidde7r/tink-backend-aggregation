package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.deserializers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParser;
import java.io.IOException;
import org.junit.Test;

public class DoubleDeserializerTest {

    @Test
    public void shouldDeserializeUnicodeMinus() throws IOException {

        JsonParser jsonParser = mock(JsonParser.class);
        when(jsonParser.getText()).thenReturn("−200,00"); // Unicode minus sign: U+2212

        DoubleDeserializer doubleDeserializer = new DoubleDeserializer();
        Double result = doubleDeserializer.deserialize(jsonParser, null);

        assertThat(result).isEqualTo(-200.0);
    }
}
