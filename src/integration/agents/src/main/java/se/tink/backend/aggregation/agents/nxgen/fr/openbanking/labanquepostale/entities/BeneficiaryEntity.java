package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.CreditorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BeneficiaryEntity {

    private CreditorAgentEntity creditorAgent;
    private CreditorEntity creditor;
    private AccountEntity creditorAccount;
}
