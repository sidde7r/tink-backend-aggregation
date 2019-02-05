package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FlagsDTOEntity {
    private boolean flagBlue;
    private boolean flagCloseAllowed;
    private boolean flagPiggy;
    private boolean flagPriv;
    private boolean flagConsultSO;
    private boolean flagCreateSo;
    private boolean flagIsHidden;
    private boolean flagCreditTransfer;
    private boolean flagRejectedTransactions;
}
