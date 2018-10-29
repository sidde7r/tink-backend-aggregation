package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.User;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class GetUsersResponse {

    List<User> users;

    @JsonGetter
    List<User> getUsers() {
        return users;
    }
}
