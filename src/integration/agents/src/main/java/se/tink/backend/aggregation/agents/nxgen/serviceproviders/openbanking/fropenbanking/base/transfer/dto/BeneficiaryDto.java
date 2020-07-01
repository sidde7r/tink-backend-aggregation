package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class BeneficiaryDto implements BeneficiaryDtoBase {

    private String id;

    private boolean isTrusted;

    private CreditorAccountDto creditorAccount;

    private CreditorAgentDto creditorAgent;

    private CreditorDto creditor;
}
