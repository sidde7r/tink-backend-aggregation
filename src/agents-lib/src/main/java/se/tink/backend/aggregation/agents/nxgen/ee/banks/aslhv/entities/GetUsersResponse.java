package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetUsersResponse {

    List<User> users;

    @JsonGetter
    List<User> getUsers() {
        return users;
    }
}
