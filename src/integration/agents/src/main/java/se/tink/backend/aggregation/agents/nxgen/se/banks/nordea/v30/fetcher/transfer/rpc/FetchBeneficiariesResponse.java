package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transfer.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transfer.entities.BeneficiariesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchBeneficiariesResponse {
    @JsonProperty private List<BeneficiariesEntity> beneficiaries;

    @JsonIgnore
    public List<BeneficiariesEntity> getBeneficiaries() {
        return beneficiaries;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setBeneficiaries(List<BeneficiariesEntity> beneficiaries) {
        this.beneficiaries = beneficiaries;
    }
}
