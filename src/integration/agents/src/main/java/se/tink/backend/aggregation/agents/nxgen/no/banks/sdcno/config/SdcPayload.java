package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config;

public class SdcPayload {
    private String bankcode;

    public SdcPayload(final String bankcode) {
        this.bankcode = bankcode;
    }

    public String getBankcode() {
        return bankcode;
    }
}
