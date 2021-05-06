package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.FormValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class AccessEntity {
    public static final AccessEntity ALL_PSD2 = AccessEntity.ofAllPsd2Accounts();

    @JsonProperty private List<AccountInfoEntity> accounts = null;

    @JsonProperty private List<AccountInfoEntity> balances = null;

    @JsonProperty private List<AccountInfoEntity> transactions = null;

    @JsonProperty private String availableAccounts = null;

    @JsonProperty private String allPsd2 = null;

    private static AccessEntity ofAllPsd2Accounts() {
        AccessEntity accessEntity = new AccessEntity();
        accessEntity.allPsd2 = FormValues.ALL_ACCOUNTS;
        return accessEntity;
    }
}
