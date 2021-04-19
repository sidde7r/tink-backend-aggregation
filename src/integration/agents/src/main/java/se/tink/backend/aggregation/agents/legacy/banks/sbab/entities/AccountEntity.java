package se.tink.backend.aggregation.agents.banks.sbab.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.AccountHolderType;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.HolderIdentity;
import se.tink.backend.agents.rpc.HolderRole;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
@Getter
@Setter
public class AccountEntity implements GeneralAccountEntity {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @JsonProperty("produktnamn")
    private String productName;

    @JsonProperty("produkttyp")
    private String productType;

    @JsonProperty("kundvaltNamn")
    private String accountaName;

    @JsonProperty("kontonummer")
    private String accountNumber;

    @JsonProperty("saldo")
    private String balance;

    @JsonProperty("disponibeltBelopp")
    private String availableBalance;

    @JsonProperty("kontohavare")
    private List<AccountHolderEntity> accountHolders;

    public Optional<Account> toTinkAccount() {

        Account account = new Account();
        account.setType(AccountTypes.SAVINGS);
        account.putFlag(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        account.setAccountNumber(accountNumber);
        account.setBankId(accountNumber);
        account.setBalance(Double.parseDouble(balance));
        account.setExactBalance(ExactCurrencyAmount.of(availableBalance, "SEK"));
        account.setName(accountaName);
        account.putIdentifier(new SwedishIdentifier(accountNumber));

        if (!Strings.isNullOrEmpty(balance) && !balance.trim().isEmpty()) {
            String cleanBalance = balance.replaceAll("[^\\d.,]", "");
            account.setBalance(StringUtils.parseAmount(cleanBalance));
        } else {
            logger.error("An account cannot have a null balance");
            return Optional.empty();
        }

        String name = !Strings.isNullOrEmpty(accountaName) ? accountaName : accountNumber;
        account.setName(name == null ? "" : name.replace("\n", "").replace("\r", ""));

        // Due to this agent being legacy we have to work with the rpc Account model directly. Using
        // the same logic as we do in core Account model when we map to the rpc Account.
        AccountHolder accountHolder = getTinkAccountHolder();
        account.setAccountHolder(accountHolder);
        account.setHolderName(getFirstHolder(accountHolder.getIdentities()).orElse(null));

        account.setSourceInfo(createAccountSourceInfo());

        return Optional.of(account);
    }

    private AccountSourceInfo createAccountSourceInfo() {
        return AccountSourceInfo.builder()
                .bankAccountType(productType)
                .bankProductName(productName)
                .build();
    }

    private AccountHolder getTinkAccountHolder() {
        AccountHolder accountHolder = new AccountHolder();
        accountHolder.setType(AccountHolderType.PERSONAL);
        accountHolder.setIdentities(
                CollectionUtils.emptyIfNull(accountHolders).stream()
                        .map(AccountHolderEntity::toHolderIdentity)
                        .collect(Collectors.toList()));
        return accountHolder;
    }

    private Optional<String> getFirstHolder(List<HolderIdentity> holderIdentities) {
        return holderIdentities.stream()
                .filter(holderIdentity -> HolderRole.HOLDER.equals(holderIdentity.getRole()))
                .findFirst()
                .map(HolderIdentity::getName);
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SwedishIdentifier(accountNumber);
    }

    @Override
    public String generalGetBank() {
        if (generalGetAccountIdentifier().isValid()) {
            return generalGetAccountIdentifier().to(SwedishIdentifier.class).getBankName();
        }
        return null;
    }

    @Override
    public String generalGetName() {
        return accountaName;
    }
}
