package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;

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

    public Optional<TransactionalAccount> toTransactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(balance.toTinkAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(accType)
                                .addIdentifier(new IbanIdentifier(maskIban(iban)))
                                .build())
                .setBankIdentifier(externalAccId)
                .build();
    }

    private String maskIban(String iban) {
        StringBuilder buffer = new StringBuilder(iban.length());
        buffer.append(iban.substring(0, 2));
        for (int i = 0; i < iban.length() - 6; i++) {
            buffer.append("x");
        }
        buffer.append(iban.substring(iban.length() - 4));
        return buffer.toString();
    }
}
