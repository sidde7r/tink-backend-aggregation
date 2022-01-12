package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.BeneficiaryDtoBase;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ArkeaTrustedBeneficiaryEntity implements BeneficiaryDtoBase {

    private ArkeaCreditorAgentEntity creditorAgent;
    private ArkeaCreditorEntity creditor;
    private ArkeaCreditorAccountEntity creditorAccount;
    private String iban;
    private boolean isTrusted;

    @JsonProperty("_links")
    private ArkeaTrustedBeneficiariesLinksEntity links;
}
