package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.transactionalaccount.entity.transaction;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class RelatedParties {

    private DebtorAgent debtorAgent;
    private Debtor debtor;
    private DebtorAccount debtorAccount;
    private CreditorAgent creditorAgent;
    private Creditor creditor;
    private CreditorAccount creditorAccount;
}
