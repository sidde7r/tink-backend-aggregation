package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer.rpc;

import com.google.api.client.util.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer.entities.TransferAccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchTransferrableResponse {
    private List<TransferAccountsEntity> accounts;

    public List<TransferAccountsEntity> getAccounts() {
        return Optional.ofNullable(accounts).orElse(Lists.newArrayList());
    }
}
