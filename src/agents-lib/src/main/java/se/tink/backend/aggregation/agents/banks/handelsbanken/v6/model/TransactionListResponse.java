package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.google.common.base.Joiner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.List;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionListResponse {

    private static final Joiner REGEXP_OR_JOINER = Joiner.on("|");

    protected AccountEntity account;
    protected List<TransactionEntity> transactions;
    protected CardInvoiceInfo cardInvoiceInfo;

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public CardInvoiceInfo getCardInvoiceInfo() {
        return cardInvoiceInfo;
    }

    public void setCardInvoiceInfo(CardInvoiceInfo cardInvoiceInfo) {
        this.cardInvoiceInfo = cardInvoiceInfo;
    }

    public Account toAccount() {
        Account account = new Account();

        account.setType(AccountTypes.CHECKING);
        account.setName(this.account.getName());
        account.setBankId(this.account.getNumber());
        account.setBalance(calculateBalance());

        if (!Strings.isNullOrEmpty(this.account.getClearingNumber())) {
            account.setAccountNumber(this.account.getClearingNumber() + "-" + this.account.getNumberFormatted());
        } else {
            account.setAccountNumber(this.account.getNumberFormatted());
        }

        account.putIdentifier(new SwedishIdentifier(account.getAccountNumber()));
        account.putIdentifier(new SwedishSHBInternalIdentifier(this.account.getNumber()));

        Preconditions.checkState(
                Preconditions.checkNotNull(account.getBankId()).matches(
                        REGEXP_OR_JOINER.join("[0-9]{8,9}", "[0-9]{13}", "[0-9]{2}-[0-9]{6}-[0-9]{6}",
                                "[0-9]{4}( \\*{4}){2} [0-9]{4}")),
                "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

        return account;
    }

    /**
     * 1. Some account have credit some does not. If there is credit,
     * subtract that from amountAvailable (disponibeltbelopp).
     * 2. If no credit but an available amount, use that.
     * 3. Default to balance which always exists.
     */
    private double calculateBalance() {
        if (cardInvoiceInfo != null && cardInvoiceInfo.getCredit() != null && this.account.getAmountAvailable() != null) {
            return this.account.getAmountAvailable().getAmount()
                    - StringUtils.parseAmount(cardInvoiceInfo.getCredit().getAmountFormatted());
        } else if (this.account.getAmountAvailable() != null) {
            return this.account.getAmountAvailable().getAmount();
        }
        return this.account.getBalance().getAmount();
    }
}
