package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public abstract class TransactionDetailsEntity {
    @JsonFormat(pattern = "yyyy-MM-dd")
    protected Date bookingDate;

    protected String additionalInformation;
    protected String bankTransactionCode;
    protected String checkId;
    protected String creditorName;
    protected DebtorAccountEntity debtorAccount;
    protected String debtorName;
    protected String endToEndId;
    protected String entryReference;
    protected String mandateId;
    protected String proprietaryBankTransactionCode;
    protected String purposeCode;
    protected String remittanceInformationStructured;
    protected String remittanceInformationUnstructured;
    protected String transactionId;
    protected String transactionDetails;
    protected String ultimateCreditor;
    protected String ultimateDebtor;
    protected CreditorAccountEntity creditorAccount;
    protected String creditorId;
    protected BalanceAmountBaseEntity transactionAmount;
    protected List<CurrencyExchange> currencyExchange;

    @JsonFormat(pattern = "yyyy-MM-dd")
    protected Date valueDate;

    public abstract Transaction toTinkTransaction();
}
