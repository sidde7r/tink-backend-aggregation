package se.tink.backend.rpc;

import io.protostuff.Tag;

import java.util.List;

import se.tink.backend.core.Merchant;

import com.google.common.collect.Lists;

public class MerchantQueryResponse {

    @Tag(1)
    private List<Merchant> merchants = Lists.newArrayList();

    public void addMerchant(Merchant merchant) {
        this.merchants.add(merchant);
    }

    public void addMerchants(List<Merchant> merchants) {
        this.merchants.addAll(merchants);
    }

    public List<Merchant> getMerchants() {
        return merchants;
    }

    public void setMerchants(List<Merchant> merchants) {
        this.merchants = merchants;
    }
}
