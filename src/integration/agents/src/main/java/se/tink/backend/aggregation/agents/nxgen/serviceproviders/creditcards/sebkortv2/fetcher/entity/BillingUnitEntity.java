package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.entity;

import java.util.List;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BillingUnitEntity {
    private String billingUnitId;
    private String billingUnitIdClear;
    private String billingUnitName;
    private String arrangementId;
    private String arrangementNumber;
    private String cutOffDate;
    private String nextInvoiceDate;
    private String creditAmount;
    private String creditAmountNumber;
    private boolean showCreditAmount;
    private String unInvoicedAmount;
    private String balance;
    private List<ContractEntity> contracts;
    private boolean showInvoices;
    private String disposableAmount;
    private String latestPaymentDate;
    private String latestPaymentAmount;
    private int cardType;
    private boolean financeServiceAllowed;
    private boolean salesFinancing;

    public String getBillingUnitId() {
        return billingUnitId;
    }

    public String getBillingUnitIdClear() {
        return billingUnitIdClear;
    }

    public String getBillingUnitName() {
        return billingUnitName;
    }

    public String getArrangementId() {
        return arrangementId;
    }

    public String getArrangementNumber() {
        return arrangementNumber;
    }

    public String getCutOffDate() {
        return cutOffDate;
    }

    public String getNextInvoiceDate() {
        return nextInvoiceDate;
    }

    public String getCreditAmount() {
        return creditAmount;
    }

    public String getCreditAmountNumber() {
        return creditAmountNumber;
    }

    public boolean isShowCreditAmount() {
        return showCreditAmount;
    }

    public String getUnInvoicedAmount() {
        return unInvoicedAmount;
    }

    public String getBalance() {
        return balance;
    }

    public List<ContractEntity> getContracts() {
        return contracts;
    }

    public boolean isShowInvoices() {
        return showInvoices;
    }

    public String getDisposableAmount() {
        return disposableAmount;
    }

    public String getLatestPaymentDate() {
        return latestPaymentDate;
    }

    public String getLatestPaymentAmount() {
        return latestPaymentAmount;
    }

    public int getCardType() {
        return cardType;
    }

    public boolean isFinanceServiceAllowed() {
        return financeServiceAllowed;
    }

    public boolean isSalesFinancing() {
        return salesFinancing;
    }

    public CreditCardAccount createCreditCardAccount() {
        return CreditCardAccount.builder(arrangementNumber)
                .setAvailableCredit(
                        Amount.inSEK(AgentParsingUtils.parseAmountTrimCurrency(disposableAmount)))
                .setAccountNumber(arrangementNumber)
                .setBankIdentifier(billingUnitIdClear)
                .setBalance(Amount.inSEK(-AgentParsingUtils.parseAmountTrimCurrency(balance)))
                .setName(billingUnitName)
                .build();
    }
}
