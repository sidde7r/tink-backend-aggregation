package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountInfoEntity {
    private BigDecimal balance;
    private String balanceFormat;
    private String bankName;
    private String currency;
    private Boolean enabled;
    private String hasDetail;

    @JsonProperty("id")
    private String accountId;

    private String imageId;
    private String lastDigits;
    private String linkedAccount;
    private String type;
    private String typeName;
    private String use;
    private String viewOrder;
    private String bacle;
    private String cardDetailVersion;
    private String cardLockedFlag;
    private String creditCardStatus;
    private String operationMode;
    private String pinFlag;
    private String pinFlagType;
    private String newInVista;
    private String availableCredit;
    private String availableCreditUnformat;
    private String issueDate;
    private String minPayment;
    private String minPaymentFormat;
    private String nonInterestPayment;
    private String nonInterestPaymentFormat;
    private String balanceError;

    public Boolean getEnabled() {
        return enabled;
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return CitiBanaMexConstants.ACCOUNT_TYPE_MAPPER.isOf(
                type, TransactionalAccountType.CHECKING);
    }

    @JsonIgnore
    private Optional<TransactionalAccountType> getAccountType() {
        return CitiBanaMexConstants.ACCOUNT_TYPE_MAPPER.translate(type);
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(String accountName, String holderName) {
        final Optional<TransactionalAccountType> accountType = getAccountType();
        Preconditions.checkState(accountType.isPresent());
        Preconditions.checkNotNull(accountId);

        final IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(accountId)
                        .withAccountNumber(accountId)
                        .withAccountName(accountName)
                        .addIdentifier(
                                AccountIdentifier.create(AccountIdentifierType.TINK, lastDigits))
                        .build();

        return TransactionalAccount.nxBuilder()
                .withType(accountType.get())
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(new ExactCurrencyAmount(balance, currency)))
                .withId(idModule)
                .setApiIdentifier(accountId)
                .addHolderName(holderName)
                .build();
    }
}
