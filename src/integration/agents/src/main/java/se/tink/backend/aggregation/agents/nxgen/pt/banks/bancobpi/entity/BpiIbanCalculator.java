package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity;

import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.IBANPortugal;

public class BpiIbanCalculator {

    private static final String PSP_REF_NUMBER_NOT_EURO_CURRENCY_DISCRIMINATOR = "3";
    private static final String BANK_ID = "0010";
    private static final String NOT_EURO_CURRENCY_PSP_REF_NUMBER = "9999";
    private static final String EURO_CURRENCY_PSP_REF_NUMBER = "0000";
    private String nuc;
    private String tipo;
    private String ordem;

    public BpiIbanCalculator(String nuc, String tipo, String ordem) {
        this.nuc = nuc;
        this.tipo = tipo;
        this.ordem = ordem;
    }

    private String getPspRefNumber() {
        if (tipo.startsWith(PSP_REF_NUMBER_NOT_EURO_CURRENCY_DISCRIMINATOR)) {
            return NOT_EURO_CURRENCY_PSP_REF_NUMBER;
        }
        return EURO_CURRENCY_PSP_REF_NUMBER;
    }

    private String getInternalAccountIdPostfix() {
        return tipo.substring(1, 3) + ordem.substring(1, 3);
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
