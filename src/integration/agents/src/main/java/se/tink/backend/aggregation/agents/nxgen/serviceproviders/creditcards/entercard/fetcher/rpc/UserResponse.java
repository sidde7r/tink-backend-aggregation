package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@SuppressWarnings("unused")
@JsonObject
public class UserResponse {

    private User user;

    public User getUser() {
        return user;
    }

    public List<String> getAccountIds() {
        if (user == null) return null;

        return user.getProducts().stream().map(Product::getAccountId).collect(Collectors.toList());
    }
}
