package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    @JsonProperty("id")
    private String accountId;

    private String alias;
    private String date;
    private String currency;
    private BigDecimal accountingBalance;
    private String iban;
    private String association;
    private BigDecimal availableBalance;
    private int type;

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkTransactionalAccount(List<Party> holders) {
        if (Strings.isNullOrEmpty(iban)) {
            return Optional.empty();
        }
        final AccountIdentifier ibanIdentifier =
                AccountIdentifier.create(AccountIdentifierType.IBAN, translateAccountType());

        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(CajamarConstants.ACCOUNT_TYPE_MAPPER, translateAccountType())
                .withBalance(toTinkAmountBalance())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban.toUpperCase(Locale.ENGLISH))
                                .withAccountNumber(iban)
                                .withAccountName(translateAccountName(holders))
                                .addIdentifier(ibanIdentifier)
                                .build())
                .setApiIdentifier(accountId)
                .addParties(holders)
                .build();
    }

    @JsonIgnore
    public String getAccountId() {
        return accountId;
    }

    @JsonIgnore
    private BalanceModule toTinkAmountBalance() {
        return BalanceModule.of(ExactCurrencyAmount.of(availableBalance, currency));
    }

    @JsonIgnore
    private String translateAccountName(List<Party> holders) {
        return alias != null
                ? alias
                : "Account "
                        + holders.stream()
                                .filter(party -> party.getRole().equals(Role.HOLDER))
                                .findFirst()
                                .map(Party::getName)
                                .orElseThrow(IllegalArgumentException::new);
    }

    @JsonIgnore
    private String translateAccountType() {
        return String.valueOf(type);
    }
}
