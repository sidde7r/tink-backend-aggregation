package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FlagsDTO {
    private boolean flagCloseAllowed;
    private boolean flagIsHidden;
    private boolean flagConsultSO;
    private boolean flagRejectedTransactions;
    private boolean flagPriv;
    private boolean flagBlue;
    private boolean flagCreditTransfer;
    private boolean flagPiggy;
    private boolean flagCreateSo;

    public boolean isFlagCloseAllowed() {
        return flagCloseAllowed;
    }

    public boolean isFlagIsHidden() {
        return flagIsHidden;
    }

    public boolean isFlagConsultSO() {
        return flagConsultSO;
    }

    public boolean isFlagRejectedTransactions() {
        return flagRejectedTransactions;
    }

    public boolean isFlagPriv() {
        return flagPriv;
    }

    public boolean isFlagBlue() {
        return flagBlue;
    }

    public boolean isFlagCreditTransfer() {
        return flagCreditTransfer;
    }

    public boolean isFlagPiggy() {
        return flagPiggy;
    }

    public boolean isFlagCreateSo() {
        return flagCreateSo;
    }
}
