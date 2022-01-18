package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BookedEntity {

    private Long bankTransactionCode;
    private String bookingDate;
    private String creditorId;
    private CreditorAccountEntity creditorAccount;
    private String creditorName;
    private DebtorAccountEntity debtorAccount;
    private String debtorName;
    private String endToEndId;
    private String entryReference;
    private String mandateId;
    private String proprietaryBankTransactionCode;
    private String remittanceInformationUnstructured;
    private TransactionAmountEntity transactionAmount;
    private String ultimateCreditor;
    private String valueDate;
}
