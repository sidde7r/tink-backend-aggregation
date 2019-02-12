package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc;

import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;
import java.util.stream.Stream;

@JsonObject
public class AccountsResponse {

    private List<AccountEntity> accounts;

    public Stream<AccountEntity> stream() {
        return accounts.stream();
    }
}
