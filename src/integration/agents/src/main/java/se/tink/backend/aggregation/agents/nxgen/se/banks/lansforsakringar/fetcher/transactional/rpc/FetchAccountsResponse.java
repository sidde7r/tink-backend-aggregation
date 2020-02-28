package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity.MainAndCoAccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountsResponse {
    private List<MainAndCoAccountsEntity> mainAndCoAccounts;

    public List<MainAndCoAccountsEntity> getMainAndCoAccounts() {
        return mainAndCoAccounts;
    }
}
