package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.domestic;

public enum PaymentContextCode {
    BILL_PAYMENT("BillPayment"),
    PARTY_TO_PARTY("PartyToParty"),
    E_COMMERCE_GOODS("EcommerceGoods"),
    E_COMMERCE_SERVICES("EcommerceServices"),
    OTHER("Other");

    private final String value;

    PaymentContextCode(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
