package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HalBeneficiariesEntity {
    @JsonProperty("beneficiaries")
    private List<BeneficiaryEntity> beneficiaries = new ArrayList<BeneficiaryEntity>();

    @JsonProperty("_links")
    private BeneficiariesLinksEntity links = null;

    public List<BeneficiaryEntity> getBeneficiaries() {
        return beneficiaries;
    }

    public void setBeneficiaries(List<BeneficiaryEntity> beneficiaries) {
        this.beneficiaries = beneficiaries;
    }

    public BeneficiariesLinksEntity getLinks() {
        return links;
    }

    public void setLinks(BeneficiariesLinksEntity links) {
        this.links = links;
    }
}
