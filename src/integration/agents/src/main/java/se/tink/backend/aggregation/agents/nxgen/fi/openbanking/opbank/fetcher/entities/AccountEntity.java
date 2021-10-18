package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Slf4j
public class AccountEntity {
    private String accountId;
    private String productName;
    private String identifierSchema;
    private String identifier;
    private String servicerSchema;
    private String servicer;
    private String owner;
    private BigDecimal netBalance;
    private String grossBalance;
    private String coverReservationAmount;
    private String currency;

    public String getAccountId() {
        return accountId;
    }

    public String getProductName() {
        return productName;
    }

    public String getIdentifierSchema() {
        return identifierSchema;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getServicerSchema() {
        return servicerSchema;
    }

    public String getServicer() {
        return servicer;
    }

    public String getOwner() {
        return owner;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public String getGrossBalance() {
        return grossBalance;
    }

    public String getCoverReservationAmount() {
        return coverReservationAmount;
    }

    public String getCurrency() {
        return currency;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {
        String bic = getServicer();
        String iban = getIdentifier();
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        OpBankConstants.ACCOUNT_TYPE_MAPPER,
                        productName,
                        TransactionalAccountType.OTHER)
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountId)
                                .withAccountNumber(identifier)
                                .withAccountName(productName)
                                .addIdentifier(new IbanIdentifier(bic, iban))
                                .addIdentifier(new BbanIdentifier(iban.substring(4)))
                                .build())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, getAccountId())
                .setBankIdentifier(accountId)
                .setApiIdentifier(accountId)
                .addParties(getParties())
                .addAccountFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .build();
    }

    public ExactCurrencyAmount getAvailableBalance() {
        return new ExactCurrencyAmount(getNetBalance(), getCurrency());
    }

    private List<Party> getParties() {
        if (Strings.isNullOrEmpty(getOwner())) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new Party(getOwner(), Role.HOLDER));
    }
}
