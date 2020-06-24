package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transfer.rpc;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.BeneficiaryDtoBase;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class BeneficiaryDto implements BeneficiaryDtoBase {

    private CreditorAgentDto creditorAgent;

    private CreditorDto creditor;

    private CreditorAccountDto creditorAccount;
}
