package se.tink.backend.common.merchants;

import se.tink.backend.core.Merchant;

public class MerchantDuplicateResult {
    private Merchant duplicate;
    private Merchant replacedBy;

    public Merchant getDuplicate() {
        return duplicate;
    }

    public void setDuplicate(Merchant duplicate) {
        this.duplicate = duplicate;
    }

    public Merchant getReplacedBy() {
        return replacedBy;
    }

    public void setReplacedBy(Merchant replacedBy) {
        this.replacedBy = replacedBy;
    }
}
