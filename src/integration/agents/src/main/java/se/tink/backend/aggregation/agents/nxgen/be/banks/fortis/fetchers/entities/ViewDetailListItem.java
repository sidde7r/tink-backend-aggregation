package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class ViewDetailListItem {

    private String accountType;
    private Object viewDetailId;
    private String accountSequenceNumber;
    private String accountNumber;
    private Account account;

    @JsonIgnore
    private TransactionalAccountType getTinkAccountType() {
        final String accountType = account.getAccountType();
        return FortisConstants.ACCOUNT_TYPE_MAPPER
                .translate(accountType)
                .orElse(TransactionalAccountType.OTHER);
    }

    @JsonIgnore
    private String getIban() {
        return account.getIban();
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance() {
        Balance balance = account.getBalance();
        return ExactCurrencyAmount.of(
                Double.parseDouble(balance.getAmount()), balance.getCurrency());
    }

    @JsonIgnore
    public boolean isValid() {
        try {
            toTinkAccount();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {
        String iban = getIban();
        return TransactionalAccount.nxBuilder()
                .withType(getTinkAccountType())
                .withInferredAccountFlags()
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(account.getAccountName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.IBAN,
                                                iban,
                                                AccountIdentifier.Type.IBAN.name()))
                                .build())
                .setApiIdentifier(getIban())
                .putInTemporaryStorage(
                        FortisConstants.Storage.ACCOUNT_PRODUCT_ID, account.getProductId())
                .build()
                .get();
    }
}
