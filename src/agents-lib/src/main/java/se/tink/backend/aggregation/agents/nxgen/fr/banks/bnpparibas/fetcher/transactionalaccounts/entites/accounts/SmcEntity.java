package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SmcEntity extends BaseResponse {
    private RibListEntity data;
    private String message;

    public RibListEntity getData() {
        return data;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
