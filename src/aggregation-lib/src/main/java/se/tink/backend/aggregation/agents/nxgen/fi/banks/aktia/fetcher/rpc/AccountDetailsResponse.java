package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class AccountDetailsResponse extends AbstractAccountEntity {
    private String bic;
    private String primaryOwnerName;
    private List<Object> mandates;

    public String getBic() {
        return bic;
    }

    public String getPrimaryOwnerName() {
        return primaryOwnerName;
    }

    public List<Object> getMandates() {
        return mandates;
    }
}
