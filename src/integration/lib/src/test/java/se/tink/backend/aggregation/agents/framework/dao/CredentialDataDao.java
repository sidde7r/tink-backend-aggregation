package se.tink.backend.aggregation.agents.framework.dao;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class CredentialDataDao {

    private final List<AccountDataDao> accountDataList;

    private List<Transfer> transfers;
    private IdentityData identityData;

    @JsonCreator
    public CredentialDataDao(
            @JsonProperty("accountDataList") List<AccountDataDao> accountDataList,
            @JsonProperty("transfers") List<Transfer> transfers,
            @JsonProperty("identityData") IdentityData identityData) {
        this.accountDataList = accountDataList;

        this.transfers = transfers;
        this.identityData = identityData;
    }

    public List<AccountDataDao> getAccountDataList() {
        return accountDataList;
    }

    public List<Transfer> getTransfers() {
        return transfers;
    }

    public IdentityData getIdentityData() {
        return identityData;
    }
}
