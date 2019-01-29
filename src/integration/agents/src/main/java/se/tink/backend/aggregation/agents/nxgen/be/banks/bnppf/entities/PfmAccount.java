package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

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

    public TransactionalAccount toTransactionalAccount() {
        return CheckingAccount.builder(externalAccId, balance.toTinkAmount())
                .setAccountNumber(maskIban(iban))
                .setName(accType)
                .setBankIdentifier(externalAccId)
                .build();
    }

    private String maskIban(String iban) {
        StringBuffer buffer = new StringBuffer(iban.length());
        buffer.append(iban.substring(0, 2));
        for (int i = 0; i < iban.length()-6; i++) {
            buffer.append("x");
        }
        buffer.append(iban.substring(iban.length()-4));
        return buffer.toString();
    }
}
