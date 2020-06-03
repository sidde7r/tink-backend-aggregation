package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class BeneficiaryDto {

    private String id;

    private boolean isTrusted;

    private CreditorAccountDto creditorAccount;

    private CreditorAgentDto creditorAgent;

    private CreditorDto creditor;
}
