package se.tink.backend.aggregation.agents.contractproducer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.framework.context.NewAgentTestContext;

public class ContractProducer {

    private static final ImmutableSet BLACK_LIST_FOR_ACCOUNT_KEYS = ImmutableSet.of("id");
    private static final ImmutableSet BLACK_LIST_FOR_TRANSACTIONS_KEYS =
            ImmutableSet.of("id", "accountId");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Map<String, Object> convertObjectToMap(Object object) throws IOException {
        return MAPPER.readValue(MAPPER.writeValueAsString(object), Map.class);
    }

    private List<Map<String, Object>> convertObjectToList(Object object) throws IOException {
        return MAPPER.readValue(MAPPER.writeValueAsString(object), List.class);
    }

    /*
       From the map, removes fields if the key appears in blacklist or
       the value is null
    */
    private void cleanMap(Map<String, Object> map, Set<String> blackList) {
        blackList.forEach(key -> map.remove(key));
        Set<String> keysWithNullValue =
                map.keySet().stream()
                        .filter(key -> map.get(key) == null)
                        .collect(Collectors.toSet());
        keysWithNullValue.forEach(key -> map.remove(key));
    }

    public String produceFromContext(NewAgentTestContext context) throws IOException {
        Map<String, Object> data = new HashMap<>();
        if (context.getIdentityData().isPresent()) {
            data.put("identityData", convertObjectToMap(context.getIdentityData().get()));
        }
        data.put("accounts", convertObjectToList(context.getUpdatedAccounts()));
        data.put("transactions", convertObjectToList(context.getTransactions()));
        ((List<Map<String, Object>>) data.get("accounts"))
                .forEach(account -> cleanMap(account, BLACK_LIST_FOR_ACCOUNT_KEYS));
        ((List<Map<String, Object>>) data.get("transactions"))
                .forEach(transaction -> cleanMap(transaction, BLACK_LIST_FOR_TRANSACTIONS_KEYS));
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data);
    }
}
