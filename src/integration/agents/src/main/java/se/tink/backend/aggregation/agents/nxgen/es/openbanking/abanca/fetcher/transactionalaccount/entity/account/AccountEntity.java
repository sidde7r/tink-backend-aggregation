package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.account;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    private String type;
    private String id;
    private AccountAttributesEntity attributes;
    private RelationshipsEntity relationships;
    private AccountLinksEntity links;

    public Optional<TransactionalAccount> toTinkAccount(Amount balance) {

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(balance))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(attributes.getIdentifier().getNumber())
                                .withAccountNumber(attributes.getIdentifier().getNumber())
                                .withAccountName("")
                                .addIdentifier(
                                        new IbanIdentifier(attributes.getIdentifier().getNumber()))
                                .build())
                .setApiIdentifier(id)
                .setBankIdentifier(attributes.getIdentifier().getBic())
                .build();
    }

    public String getId() {
        return id;
    }
}
