package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity;

import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.IBANPortugal;

public class BpiIbanCalculator {
    private static final String BANK_ID = "0010";
    private String nuc;
    private String tipo;
    private String ordem;

    public BpiIbanCalculator(String nuc, String tipo, String ordem) {
        this.nuc = nuc;
        this.tipo = tipo;
        this.ordem = ordem;
    }

    private String getPspRefNumber() {
        if(tipo.substring(0, 1).equals("3")) {
            return "9999";
        }
        return "0000";
    }

    private String getInternalAccountIdPostfix() {
        return tipo.substring(1,3) + ordem.substring(1,3);
    }

    private String getAccountNumber() {
        return nuc + getInternalAccountIdPostfix();
    }

    private String getBankId() {
        return BANK_ID;
    }

    String calculateIban() {
        return IBANPortugal.generateIBAN(getBankId(), getPspRefNumber(), getAccountNumber());
    }
}
