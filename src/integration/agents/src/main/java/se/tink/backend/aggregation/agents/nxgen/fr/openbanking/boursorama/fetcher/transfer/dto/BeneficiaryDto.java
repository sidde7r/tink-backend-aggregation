package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.transfer.dto;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.BeneficiaryDtoBase;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class BeneficiaryDto implements BeneficiaryDtoBase {

    private String id;

    private CreditorAgentDto creditorAgent;

    private CreditorDto creditor;

    private CreditorAccountDto creditorAccount;
}
