package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer.entities.AccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchSavedTransferRecipientsResponse {
    private List<AccountsEntity> accounts;
}
