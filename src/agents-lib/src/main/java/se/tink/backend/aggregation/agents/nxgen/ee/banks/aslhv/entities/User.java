package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class User {
    @JsonProperty("user_id")
    int userId;
    String name;
    List<String> privileges;
}
