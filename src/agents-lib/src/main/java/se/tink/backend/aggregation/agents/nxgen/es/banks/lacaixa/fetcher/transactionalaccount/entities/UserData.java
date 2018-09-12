package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.UserDataDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserData {

    @JsonProperty("pair")
    @JsonDeserialize(using = UserDataDeserializer.class)
    private Map<String, String> dataMap;
}
