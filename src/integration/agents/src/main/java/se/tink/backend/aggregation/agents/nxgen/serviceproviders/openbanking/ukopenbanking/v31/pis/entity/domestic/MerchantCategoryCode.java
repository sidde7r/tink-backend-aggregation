package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

public enum MerchantCategoryCode {
    BILL_PAYMENT("BillPayment"),
    ECOMMERCE_GOODS("EcommerceGoods"),
    ECOMMERCE_SERVICES("EcommerceServices"),
    OTHER("Other"),
    PARTY_TO_PARTY("PartyToParty");

    private String value;

    MerchantCategoryCode(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
