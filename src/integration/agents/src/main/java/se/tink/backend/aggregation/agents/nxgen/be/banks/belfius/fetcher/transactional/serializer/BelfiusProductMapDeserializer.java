package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusProduct;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusProductMap;

public class BelfiusProductMapDeserializer extends JsonDeserializer<BelfiusProductMap> {

    @Override
    public BelfiusProductMap deserialize(JsonParser jsonParser, DeserializationContext context)
            throws IOException {
        Map<String, BelfiusProduct> products = new HashMap<>();

        JsonNode json = jsonParser.getCodec().readTree(jsonParser);
        json.get("contentList")
                .get("dynamiccontent")
                .elements()
                .forEachRemaining(entry -> deserializeInto(products, jsonParser, entry));

        return new BelfiusProductMap(products);
    }

    public void deserializeInto(
            Map<String, BelfiusProduct> products, JsonParser jsonParser, JsonNode entry) {
        try {
            String key = entry.get("key").asText();
            JsonNode value = entry.get("repeatedPane_detail");
            BelfiusProduct product = jsonParser.getCodec().treeToValue(value, BelfiusProduct.class);
            products.put(key, product);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
