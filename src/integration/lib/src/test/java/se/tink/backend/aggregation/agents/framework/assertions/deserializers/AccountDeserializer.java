package se.tink.backend.aggregation.agents.framework.assertions.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
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
            JsonNode availableBalance = rawNode.get("availableBalance");
            JsonNode creditLimit = rawNode.get("creditLimit");
            ObjectNode objectNode = (ObjectNode) rawNode;
            objectNode.remove("exactBalance");
            objectNode.remove("exactAvailableCredit");
            objectNode.remove("availableBalance");
            objectNode.remove("creditLimit");
            Account account = new ObjectMapper().readValue(objectNode.toString(), Account.class);
            if (exactBalance != null) {
                account.setExactBalance(
                        ExactCurrencyAmount.of(
                                exactBalance.get("exactValue").asText(),
                                exactBalance.get("currencyCode").asText()));
            }
            if (exactAvailableCredit != null) {
                account.setExactAvailableCredit(
                        ExactCurrencyAmount.of(
                                exactAvailableCredit.get("exactValue").asText(),
                                exactAvailableCredit.get("currencyCode").asText()));
            }
            if (availableBalance != null) {
                account.setAvailableBalance(
                        ExactCurrencyAmount.of(
                                availableBalance.get("exactValue").asText(),
                                availableBalance.get("currencyCode").asText()));
            }
            if (creditLimit != null) {
                account.setCreditLimit(
                        ExactCurrencyAmount.of(
                                creditLimit.get("exactValue").asText(),
                                creditLimit.get("currencyCode").asText()));
            }
            entities.add(account);
        }
        return entities;
    }
}
