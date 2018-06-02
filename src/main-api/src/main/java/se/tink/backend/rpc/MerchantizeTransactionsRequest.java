package se.tink.backend.rpc;

import io.protostuff.Tag;

import java.util.List;

import se.tink.backend.core.Merchant;

public class MerchantizeTransactionsRequest {

    @Tag(1)
    private Merchant merchant;
    @Tag(2)
    private List<String> transactionIds;

    public Merchant getMerchant() {
        return merchant;
    }

    public List<String> getTransactionIds() {
        return transactionIds;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public void setTransactionIds(List<String> transactionIds) {
        this.transactionIds = transactionIds;
    }
}
