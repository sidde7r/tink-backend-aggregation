package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.FormValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {
    public static AccessEntity ALL_PSD2 =
            new AccessEntity(null, null, null, null, FormValues.ALL_ACCOUNTS);

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty
    private List<AccountInfoEntity> accounts;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty
    private List<AccountInfoEntity> balances;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty
    private List<AccountInfoEntity> transactions;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty
    private String availableAccounts;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty
    private String allPsd2;

    public AccessEntity(
            List<AccountInfoEntity> accounts,
            List<AccountInfoEntity> balances,
            List<AccountInfoEntity> transactions,
            String availableAccounts,
            String allPsd2) {
        this.accounts = accounts;
        this.balances = balances;
        this.transactions = transactions;
        this.availableAccounts = availableAccounts;
        this.allPsd2 = allPsd2;
    }
}
