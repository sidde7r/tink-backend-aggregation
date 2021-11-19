package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

@JsonObject
public class UserDataResponse {

    private Map<String, String> dataMap;

    // User data JSON has a ridiculous format, using this setter to avoid a tangle of classes.
    @JsonProperty("datos")
    private void setUserData(JsonNode node) {

        // Skip one level of JSON tree
        node = node.get("pair");

        // Extract key/value pairs from array and put them directly into map
        dataMap = new HashMap<>(node.size());
        for (JsonNode element : node) {
            dataMap.put(element.get("key").asText(), element.get("value").asText());
        }
    }

    @JsonIgnore
    public HolderName getHolderName() {
        return new HolderName(dataMap.get(LaCaixaConstants.UserData.FULL_HOLDER_NAME));
    }

    @JsonIgnore
    public String getDNI() {
        return dataMap.get(LaCaixaConstants.UserData.DNI);
    }

    @JsonIgnore
    public String getDateOfBirth() {
        return dataMap.get(LaCaixaConstants.UserData.DATE_OF_BIRTH);
    }

    @JsonIgnore
    public String getFirstName() {
        return dataMap.get(LaCaixaConstants.UserData.FIRST_NAME);
    }

    @JsonIgnore
    public String getFirstSurName() {
        return dataMap.get(LaCaixaConstants.UserData.FIRST_SUR_NAME);
    }

    @JsonIgnore
    public String getSecondSurName() {
        return dataMap.get(LaCaixaConstants.UserData.SECOND_SUR_NAME);
    }

    @JsonIgnore
    public String isScaNeeded() {
        return dataMap.get(LaCaixaConstants.UserData.IS_ENROLLMENT_NEEDED);
    }
}
