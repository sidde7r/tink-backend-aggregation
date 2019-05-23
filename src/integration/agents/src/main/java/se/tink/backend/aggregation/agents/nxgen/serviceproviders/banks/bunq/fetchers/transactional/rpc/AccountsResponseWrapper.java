package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsResponseWrapper {
    @JsonProperty("Response")
    private List<AccountWrapper> response;

    public List<AccountWrapper> getResponse() {
        return response;
    }

    public List<TransactionalAccount> toTinkAccounts() {
        List<AccountWrapper> accountWrappers =
                Optional.ofNullable(response)
                        .orElseThrow(() -> new IllegalStateException("Response was null"));

        return accountWrappers.stream()
                .map(AccountWrapper::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
