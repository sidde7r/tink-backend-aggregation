package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.entities.TransactionEntity;

public class AxaTransactionsDeserializerTest {

    @Test
    public void deserializeTest() { // shouldDeserializeToEmptyList() {
        JsonParser jsonParser = mock(JsonParser.class);
        ObjectCodec objectCodec = mock(ObjectCodec.class);

        when(jsonParser.getCodec()).thenReturn(objectCodec);

        AxaTransactionsDeserializer axaTransactionsDeserializer = new AxaTransactionsDeserializer();
        List<TransactionEntity> result = null;
        try {
            result = axaTransactionsDeserializer.deserialize(jsonParser, null);
        } catch (IOException e) {
            Assert.assertFalse("Internal error in test: shouldDeserializeToEmptyList", true);
        }

        assertThat(result).doesNotContainNull();
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldDeserializeToListOfTransactions() {
        JsonParser jsonParser = mock(JsonParser.class);
        ObjectCodec objectCodec = mock(ObjectCodec.class);
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "[{ },{ }]";

        when(jsonParser.getCodec()).thenReturn(objectCodec);
        try {
            when(objectCodec.readTree(jsonParser)).thenReturn(mapper.readTree(jsonString));
        } catch (IOException e) {
            Assert.assertFalse(
                    "Internal error in test: shouldDeserializeToListOfTransactions", true);
        }

        AxaTransactionsDeserializer axaTransactionsDeserializer = new AxaTransactionsDeserializer();
        List<TransactionEntity> result = null;
        try {
            result = axaTransactionsDeserializer.deserialize(jsonParser, null);
        } catch (IOException e) {
            Assert.assertFalse(
                    "Internal error in test: shouldDeserializeToListOfTransactions", true);
        }
        assertThat(result).doesNotContainNull();
        assertThat(result.size()).isEqualTo(2);
    }
}
