package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AccountEntity {
    @Getter private PermissionsEntity permissions;
    private String accountId;
    private String displayAccountNumber;
    private String iban;
    private String bic;
    private String countryCode;
    private String productCode;
    private String productName;
    private String nickname;
    private String productType;
    @Getter private String category;
    private String accountStatus;
    private double bookedBalance;
    private double creditLimit;
    private double availableBalance;
    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date latestTransactionDate;

    private String statementFormat;
    private List<AccountOwnerEntity> roles;
    private InterestInfoEntity interestInfo;

    public TransactionalAccount toTinkAccount() {

        return TransactionalAccount.builder(
                        getAccountType(), iban, ExactCurrencyAmount.of(availableBalance, currency))
                .setHolderName(getHolderName())
                .setName(StringUtils.isNotBlank(nickname) ? nickname : productName)
                .setExactBalance(ExactCurrencyAmount.of(availableBalance, currency))
                .addIdentifier(new IbanIdentifier(iban))
                .setAccountNumber(displayAccountNumber)
                .setBankIdentifier(accountId)
                .build();
    }

    private AccountTypes getAccountType() {
        return NordeaFIConstants.ACCOUNT_TYPE_MAPPER
                .translate(category)
                .orElseGet(this::logUnknownAccountAndGetDefaultValue);
    }

    private AccountTypes logUnknownAccountAndGetDefaultValue() {
        log.info("{}: {}", NordeaFIConstants.LogTags.NORDEA_FI_ACCOUNT_TYPE, category);
        return AccountTypes.CHECKING;
    }

    private HolderName getHolderName() {
        return roles.stream()
                .filter(AccountOwnerEntity::isOwner)
                .map(AccountOwnerEntity::getHolderName)
                .findFirst()
                .orElse(null);
    }
}
