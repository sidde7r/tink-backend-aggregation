package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.account.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class FetchAccountResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private BigDecimal balance;

    private String currency;

    private String iban;

    private Boolean multicurrency;

    private String accountName;

    @JsonProperty("other_compartments")
    private List<Object> otherCompartments;

    private String type;

    public LinksEntity getLinks() {
        return links;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }

    public Optional<TransactionalAccount> toTinkAccount(String logicalId) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(new ExactCurrencyAmount(balance, currency)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(type)
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifierType.IBAN, iban))
                                .build())
                .addHolderName(accountName)
                .setApiIdentifier(logicalId)
                .addAccountFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .build();
    }
}
