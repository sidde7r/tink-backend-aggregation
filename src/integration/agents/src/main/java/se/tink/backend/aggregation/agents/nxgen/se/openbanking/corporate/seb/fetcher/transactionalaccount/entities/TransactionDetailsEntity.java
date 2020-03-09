package se.tink.backend.aggregation.agents.nxgen.se.openbanking.corporate.seb.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionDetailsEntity {
    private Object transactionAmount;
    private Object proprietaryBankTransactionCode;
    private Object valueDate;
    private Object bookingDate;
    private Object accountServicerReference;
    private Object thirdPartyProviderName;
    private Object thirdPartyProviderReference;
    private Object thirdPartyProviderRegTimestamp;
    private Object debtorAccount;
    private Object creditorAccount;
    private Object remittanceInformationStructured;
    private Object remittanceInformationUnstructured;
    private Object remittanceInformationUnstructuredArray;
    private Object debtorName;
    private Object debtorAgentName;
    private Object interbankSettlementAmount;
    private Object currencyExchange;
    private Object entryReference;
    private Object counterValueAmount;
    private Object creditorAgentName;
    private Object creditorAgentBic;
    private Object creditorNameAndAddress;
    private Object creditorCountryOfResidence;
    private String cardAcceptorId;
    private Object transactionDate;
    private Object originalAmount;
    private Object debtorId;
    private Object debtorNameAndAddress;
    private Object debtorCountryOfResidence;
    private Object ultimateDebtor;
    private Object ultimateDebtorId;
    private Object creditorId;
    private Object ultimateCreditorId;
    private Object ultimateCreditor;
    private Object debtorReference;

    public String getCardAcceptorId() {
        return cardAcceptorId;
    }
}
