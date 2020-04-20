package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.rpc;

import java.util.ArrayList;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountResponse extends ArrayList<AccountEntity> {
    @Override
    public Stream<AccountEntity> stream() {
        return super.stream();
    }
}
