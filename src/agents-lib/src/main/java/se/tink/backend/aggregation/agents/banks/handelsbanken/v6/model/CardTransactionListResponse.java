package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import java.util.List;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardTransactionListResponse extends AbstractResponse {
    protected List<CardTransactionEntity> transactions;
    private CardInvoiceInfo cardInvoiceInfo;

    public List<CardTransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<CardTransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public CardInvoiceInfo getCardInvoiceInfo() {
        return cardInvoiceInfo;
    }

    public void setCardInvoiceInfo(CardInvoiceInfo cardInvoiceInfo) {
        this.cardInvoiceInfo = cardInvoiceInfo;
    }

    public Account toAccount(CardEntity cardEntity) {
        Account account = new Account();

        account.setName(cardEntity.getName());
        account.setBankId(cardEntity.getNumberMasked());
        account.setAccountNumber(cardEntity.getNumberMasked());
        account.putIdentifier(new SwedishIdentifier(account.getAccountNumber()));
        account.setType(AccountTypes.CREDIT_CARD);

        if (cardEntity.getAmountAvailable() != null) {
            account.setAvailableCredit(StringUtils.parseAmount(cardEntity.getAmountAvailable().getAmountFormatted()));
        }

        if (cardEntity.getBalance() != null) {
            account.setBalance(StringUtils.parseAmount(cardEntity.getBalance().getAmountFormatted()));
        }

        if (this.cardInvoiceInfo != null) {

            // Update credit

            if (this.cardInvoiceInfo.getSpendable() != null) {
                String spendableAmount = this.cardInvoiceInfo.getSpendable().getAmountFormatted();

                if (!Strings.isNullOrEmpty(spendableAmount)) {
                    account.setAvailableCredit(StringUtils.parseAmount(spendableAmount));
                }
            }

            // Update balance

            if (this.cardInvoiceInfo.getUsedCredit() != null) {
                String usedCreditAmount = this.cardInvoiceInfo.getUsedCredit().getAmountFormatted();
                if (!Strings.isNullOrEmpty(usedCreditAmount)) {
                    account.setBalance(StringUtils.parseAmount(usedCreditAmount));
                }
            }

        }

        return account;
    }
}
