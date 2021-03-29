package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@JsonObject
public class AccountsItem {

    @JsonProperty("resourceId")
    private String resourceId;

    @JsonProperty("_links")
    private Links links;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("status")
    private String status;

    @JsonAlias("name")
    private String ownerName;

    public String getResourceId() {
        return resourceId;
    }

    public String getIban() {
        return iban;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toCheckingAccount(final BalanceResponse balanceResponse) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(getBalanceModule(balanceResponse))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(iban)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(resourceId)
                .addParties(getParties())
                .putInTemporaryStorage(StorageKey.RESOURCE_ID, getResourceId())
                .build();
    }

    private List<Party> getParties() {
        return RabobankConstants.SPLITTERS
                .splitAsStream(ownerName.trim())
                .map(name -> new Party(name, Party.Role.HOLDER))
                .collect(Collectors.toList());
    }

    private BalanceModule getBalanceModule(BalanceResponse balanceResponse) {

        final List<BalancesItem> balances = balanceResponse.getBalances();

        // Booked balance is required, so we cannot continue with no way to determine it.
        if (balances.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot determine booked balance from empty list of balances.");
        }
        logUnknownTypes(balances);

        final BalancesItem balance =
                balances.stream()
                        .filter(
                                b ->
                                        b.getBalanceType()
                                                .equals(RabobankConstants.BALANCE_TYPE_EXPECTED))
                        .findFirst()
                        .get();

        return BalanceModule.builder()
                .withBalance(
                        ExactCurrencyAmount.of(
                                balance.getBalanceAmount().getAmount(),
                                balance.getBalanceAmount().getCurrency()))
                .build();
    }

    private void logUnknownTypes(List<BalancesItem> balances) {
        List<String> unknownTypes =
                balances.stream()
                        .map(BalancesItem::getBalanceType)
                        .filter(s -> s.equals(RabobankConstants.BALANCE_TYPE_EXPECTED))
                        .collect(Collectors.toList());
        if (!unknownTypes.isEmpty()) {
            log.warn(
                    "Found balance entities with unknown balanceType :"
                            + String.join(", ", unknownTypes));
        }
    }
}
