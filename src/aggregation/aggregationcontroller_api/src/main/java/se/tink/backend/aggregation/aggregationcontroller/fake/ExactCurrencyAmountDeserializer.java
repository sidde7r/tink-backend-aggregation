package se.tink.backend.aggregation.aggregationcontroller.fake;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class ExactCurrencyAmountDeserializer extends StdDeserializer<ExactCurrencyAmount> {
    public ExactCurrencyAmountDeserializer() {
        this(null);
    }

    public ExactCurrencyAmountDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ExactCurrencyAmount deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        double value = node.get("doubleValue").doubleValue();
        String currencyCode = node.get("currencyCode").asText();
        return ExactCurrencyAmount.of(value, currencyCode);
    }
}
