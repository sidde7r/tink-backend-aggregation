package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

public class SdcPayload {
    private String bankcode;

    public SdcPayload(final String bankcode) {
        this.bankcode = bankcode;
    }

    public String getBankcode() {
        return bankcode;
    }
}
