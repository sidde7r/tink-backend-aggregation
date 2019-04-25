package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

public enum PaymentScheme {
    BBAN("UK.OBIE.BBAN"),
    IBAN("UK.OBIE.IBAN"),
    PAN("UK.OBIE.PAN"),
    PAYM("UK.OBIE.Paym"),
    SORT_CODE_ACCOUNT_NUMBER("UK.OBIE.SortCodeAccountNumber");

    private final String value;

    PaymentScheme(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
