package se.tink.backend.aggregation.agents.framework.assertions.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class AccountDeserializer extends JsonDeserializer<List<Account>> {

    @Override
    public List<Account> deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        final JsonNode rawList = jsonParser.getCodec().readTree(jsonParser);
        final List<Account> entities = new ArrayList<>();
        if (rawList.isNull()) {
            return null;
        }
        for (JsonNode rawNode : rawList) {
            JsonNode exactBalance = rawNode.get("exactBalance");
            JsonNode exactAvailableCredit = rawNode.get("exactAvailableCredit");
            ObjectNode objectNode = (ObjectNode) rawNode;
            objectNode.remove("exactBalance");
            objectNode.remove("exactAvailableCredit");
            Account account = new ObjectMapper().readValue(objectNode.toString(), Account.class);
            account.setExactBalance(
                    new ExactCurrencyAmount(
                            new BigDecimal(exactBalance.get("value").asDouble()),
                            exactBalance.get("currencyCode").asText()));
            account.setExactAvailableCredit(
                    new ExactCurrencyAmount(
                            new BigDecimal(exactAvailableCredit.get("value").asDouble()),
                            exactAvailableCredit.get("currencyCode").asText()));
            entities.add(account);
        }
        return entities;
    }
}
