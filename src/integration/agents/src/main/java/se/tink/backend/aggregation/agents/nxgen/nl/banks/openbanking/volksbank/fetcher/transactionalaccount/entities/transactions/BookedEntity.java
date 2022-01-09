package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BookedEntity {

    private CreditorAccountEntity creditorAccount;
    private String creditorName;
    private DebtorAccountEntity debtorAccount;
    private String debtorName;
    private String entryReference;
    private String remittanceInformationUnstructured;
    private TransactionAmountEntity transactionAmount;
}
