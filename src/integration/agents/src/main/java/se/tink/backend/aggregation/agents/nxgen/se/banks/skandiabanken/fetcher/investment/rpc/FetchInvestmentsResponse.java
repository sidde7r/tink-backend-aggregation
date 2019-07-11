package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.SecuritiesAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.insurance.InsuranceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.pension.PensionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchInvestmentsResponse {

    @JsonProperty("SecuritiesAccounts")
    private List<SecuritiesAccountsEntity> securitiesAccounts;

    @JsonProperty("Pensions")
    private List<PensionEntity> pensions;

    @JsonProperty("Insurances")
    private List<InsuranceEntity> insurances;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setAccounts(List<SecuritiesAccountsEntity> securitiesAccounts) {
        this.securitiesAccounts = securitiesAccounts;
    }

    @JsonIgnore
    public List<SecuritiesAccountsEntity> getSecuritiesAccounts() {
        return securitiesAccounts;
    }

    @JsonIgnore
    public List<PensionEntity> getPensions() {
        return pensions;
    }

    @JsonIgnore
    public List<InsuranceEntity> getInsurances() {
        return insurances;
    }
}
