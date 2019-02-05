package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts.UserOverviewDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserOverviewResponse extends BaseResponse {
    private UserOverviewDataEntity data;

    public UserOverviewDataEntity getData() {
        return data;
    }
}
