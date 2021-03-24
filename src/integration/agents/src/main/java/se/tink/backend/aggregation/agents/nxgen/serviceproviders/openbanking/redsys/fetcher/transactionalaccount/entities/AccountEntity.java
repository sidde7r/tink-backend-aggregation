package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.ACCOUNT_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.control.Option;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.BalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.AspspConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    private static final DisplayAccountIdentifierFormatter IBAN_FORMATTER =
            new DisplayAccountIdentifierFormatter();
    @JsonProperty private String resourceId;
    @JsonProperty private String iban;
    @JsonProperty private String bban;
    @JsonProperty private String msisdn;
    @JsonProperty private String currency;
    @JsonProperty private String ownerName;
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
    public Optional<TransactionalAccount> toTinkAccount(
            List<BalanceEntity> accountBalances, AspspConfiguration aspspConfiguration) {
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

        final AccountIdentifier ibanIdentifier =
                AccountIdentifier.create(AccountIdentifierType.IBAN, iban);
        final String formattedIban = IBAN_FORMATTER.apply(ibanIdentifier);
        final IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(getUniqueIdentifier(aspspConfiguration))
                        .withAccountNumber(formattedIban)
                        .withAccountName(
                                Option.of(name)
                                        .orElse(Option.of(product))
                                        .orElse(Option.of(details))
                                        .getOrElse(formattedIban))
                        .addIdentifier(ibanIdentifier)
                        .setProductName(Option.of(product).getOrElse(details))
                        .build();

        TransactionalBuildStep builder =
                TransactionalAccount.nxBuilder()
                        .withTypeAndFlagsFrom(
                                ACCOUNT_TYPE_MAPPER,
                                cashAccountType,
                                TransactionalAccountType.CHECKING)
                        .withBalance(BalanceModule.of(balance))
                        .withId(idModule)
                        .addHolderName(ownerName)
                        .setApiIdentifier(resourceId);

        if (links != null) {
            links.forEach((key, link) -> builder.putInTemporaryStorage(key, link.getHref()));
        }

        return builder.build();
    }

    @JsonIgnore
    private String getUniqueIdentifier(AspspConfiguration aspspConfiguration) {
        if (aspspConfiguration.shouldReturnLowercaseAccountId()) {
            return iban.toLowerCase(Locale.ENGLISH);
        } else {
            return iban.toUpperCase(Locale.ENGLISH);
        }
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
