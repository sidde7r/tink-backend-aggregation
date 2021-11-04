package se.tink.backend.aggregation.agents.banks.sbab.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.AccountHolderType;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.HolderIdentity;
import se.tink.backend.agents.rpc.HolderRole;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
@Getter
@Setter
@Slf4j
public class AccountEntity implements GeneralAccountEntity {
    @JsonProperty("accountType")
    private String productName;

    @JsonProperty("description")
    private String productType;

    @JsonProperty("name")
    private String accountName;

    @JsonProperty("number")
    private String accountNumber;

    @JsonProperty("balance")
    private String balance;

    @JsonProperty("availableForWithdrawal")
    private String availableBalance;

    private List<AccountHolderEntity> accountHolders;

    private String mandateType;

    private TransfersEntity transfers;

    public Optional<Account> toTinkAccount() {

        Account account = new Account();
        account.setType(AccountTypes.SAVINGS);
        account.putFlag(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        account.setAccountNumber(accountNumber);
        account.setBankId(accountNumber);
        account.setBalance(Double.parseDouble(balance));
        account.setExactBalance(ExactCurrencyAmount.of(availableBalance, "SEK"));
        account.setName(accountName);
        account.putIdentifier(new SwedishIdentifier(accountNumber));

        if (!Strings.isNullOrEmpty(balance) && !balance.trim().isEmpty()) {
            String cleanBalance = balance.replaceAll("[^\\d.,]", "");
            account.setBalance(StringUtils.parseAmount(cleanBalance));
        } else {
            log.error("An account cannot have a null balance");
            return Optional.empty();
        }

        String name = !Strings.isNullOrEmpty(accountName) ? accountName : accountNumber;
        account.setName(name == null ? "" : name.replace("\n", "").replace("\r", ""));

        // Due to this agent being legacy we have to work with the rpc Account model directly. Using
        // the same logic as we do in core Account model when we map to the rpc Account.
        AccountHolder accountHolder = getTinkAccountHolder();
        account.setAccountHolder(accountHolder);
        account.setHolderName(getFirstHolder(accountHolder.getIdentities()).orElse(null));
        account.setSourceInfo(createAccountSourceInfo());
        account.setCapabilities(getAccountCapabilities(productType));

        return Optional.of(account);
    }

    public TransfersEntity getTransfers() {
        return transfers;
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SwedishIdentifier(accountNumber);
    }

    @Override
    public String generalGetBank() {
        return generalGetAccountIdentifier().isValid()
                ? generalGetAccountIdentifier().to(SwedishIdentifier.class).getBankName()
                : null;
    }

    @Override
    public String generalGetName() {
        return accountName;
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
                        .map(holder -> holder.toHolderIdentity(getHolderRole()))
                        .collect(Collectors.toList()));
        return accountHolder;
    }

    private Optional<String> getFirstHolder(List<HolderIdentity> holderIdentities) {
        return holderIdentities.stream()
                .filter(holderIdentity -> HolderRole.HOLDER.equals(holderIdentity.getRole()))
                .findFirst()
                .map(HolderIdentity::getName);
    }

    private HolderRole getHolderRole() {
        if ("OWNER".equalsIgnoreCase(mandateType) || "MEDKONTOHAVARE".equals(mandateType)) {
            return HolderRole.HOLDER;
        }

        log.warn("Unknown holder role type {}", mandateType);
        return HolderRole.OTHER;
    }

    public static AccountCapabilities getAccountCapabilities(String type) {
        return SBABConstants.ACCOUNT_CAPABILITIES_MAPPER
                .translate(type)
                .orElse(
                        new AccountCapabilities(
                                AccountCapabilities.Answer.UNKNOWN,
                                AccountCapabilities.Answer.UNKNOWN,
                                AccountCapabilities.Answer.UNKNOWN,
                                AccountCapabilities.Answer.UNKNOWN));
    }
}
