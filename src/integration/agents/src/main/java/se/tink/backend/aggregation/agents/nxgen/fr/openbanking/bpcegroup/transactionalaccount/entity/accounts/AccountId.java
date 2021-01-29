package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountId {

    @Getter private String iban;
    @Getter private OtherInformationEntity other;
}
