package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.ACCOUNT_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.BalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.IdBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    @JsonProperty private String resourceId;
    @JsonProperty private String iban;
    @JsonProperty private String bban;
    @JsonProperty private String msisdn;
    @JsonProperty private String currency;
    @JsonProperty private String name;
    @JsonProperty private String product;
    @JsonProperty private String cashAccountType;
    @JsonProperty private String status;
    @JsonProperty private String bic;
    @JsonProperty private String linkedAccounts;
    @JsonProperty private String accountType;
    @JsonProperty private String details;
    @JsonProperty private List<BalanceEntity> balances;

    @JsonProperty("_links")
    private Map<String, LinkEntity> links;

    @JsonIgnore
    public TransactionalAccount toTinkAccount(List<BalanceEntity> accountBalances) {
        TransactionalAccountType accountType =
                ACCOUNT_TYPE_MAPPER
                        .translate(cashAccountType)
                        .orElse(TransactionalAccountType.OTHER);
        if (cashAccountType == null) {
            // field is optional
            accountType = TransactionalAccountType.CHECKING;
        }

        final ExactCurrencyAmount balance =
                BalanceEntity.getBalanceOfType(
                        accountBalances,
                        BalanceType.EXPECTED,
                        BalanceType.INTERIM_AVAILABLE,
                        BalanceType.CLOSING_BOOKED,
                        BalanceType.OPENING_BOOKED);
        if (balance == null) {
            throw new IllegalStateException("Did not find balance for account.");
        }

        final IdBuildStep idBuilder =
                IdModule.builder()
                        .withUniqueIdentifier(iban)
                        .withAccountNumber(iban)
                        .withAccountName(Strings.nullToEmpty(name))
                        .addIdentifier(AccountIdentifier.create(Type.IBAN, iban));

        if (!Strings.isNullOrEmpty(product)) {
            idBuilder.setProductName(product);
        } else if (!Strings.isNullOrEmpty(details)) {
            idBuilder.setProductName(details);
        }

        TransactionalBuildStep builder =
                TransactionalAccount.nxBuilder()
                        .withType(accountType)
                        .withBalance(BalanceModule.of(balance))
                        .withId(idBuilder.build())
                        .setApiIdentifier(resourceId);
        if (links != null) {
            links.forEach((key, link) -> builder.putInTemporaryStorage(key, link.getHref()));
        }

        return builder.build();
    }

    @JsonIgnore
    public String getResourceId() {
        return resourceId;
    }

    @JsonIgnore
    public boolean hasBalances() {
        return balances != null;
    }

    @JsonIgnore
    public List<BalanceEntity> getBalances() {
        return balances;
    }

    @JsonIgnore
    public Optional<LinkEntity> getLink(String linkName) {
        if (links == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(links.get(linkName));
    }
}
