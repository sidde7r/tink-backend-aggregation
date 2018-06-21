package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class PfmAccount {
    private String iban;
    private String currency;
    private Amount balance;
    private String externalAccId;
    private boolean pfmOptInFlag;
    private String alias;
    private String defaultName;
    private String accType;
    private String accTypeFullName;
    private int subProdType;
    private boolean isNewlyAdded;

    public String getIban() {
        return iban;
    }

    public String getExternalAccId() {
        return externalAccId;
    }

    public boolean getPfmOptInFlag() {
        return pfmOptInFlag;
    }
}
