package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class ViewDetailListItem {

    private static final AggregationLogger LOGGER = new AggregationLogger(ViewDetailListItem.class);

    private String accountType;
    private Object viewDetailId;
    private String accountSequenceNumber;
    private String accountNumber;
    private Account account;

    public String getAccountType() {
        return accountType;
    }

    public Object getViewDetailId() {
        return viewDetailId;
    }

    public String getAccountSequenceNumber() {
        return accountSequenceNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Account getAccount() {
        return account;
    }

    @JsonIgnore
    private AccountTypes getTinkAccountType() {
        final String type = getAccount().getAccountType(); // TODO: verify

        return FortisConstants.ACCOUNT_TYPE_MAPPER.translate(type).orElse(AccountTypes.OTHER);
    }

    @JsonIgnore
    private String getIban() {
        return getAccount().getIban();
    }

    @JsonIgnore
    private Amount getTinkAmount() {
        return new Amount(
                getAccount().getBalance().getCurrency(),
                Double.parseDouble(getAccount().getBalance().getAmount()));
    }

    @JsonIgnore
    private String getAccountName() {
        return getAccount().getAccountName();
    }

    @JsonIgnore
    public boolean isValid() {
        try {
            toTinkAccount();
            return true;
        } catch (Exception e) {
            LOGGER.errorExtraLong(
                    "error validating transaction!",
                    FortisConstants.LoggingTag.TRANSACTION_VALIDATION_ERR,
                    e);
            return false;
        }
    }

    @JsonIgnore
    private AccountIdentifier getIbanIdentifier() {
        return AccountIdentifier.create(
                AccountIdentifier.Type.IBAN, getIban(), AccountIdentifier.Type.IBAN.toString());
    }

    @JsonIgnore
    private HolderName getHoldername() {
        return new HolderName(getAccount().getAlias());
    }

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.builder(getTinkAccountType(), getIban(), getTinkAmount())
                .setAccountNumber(getIban())
                .setHolderName(getHoldername())
                .setName(getAccountName())
                .addIdentifier(getIbanIdentifier())
                .putInTemporaryStorage(
                        FortisConstants.Storage.ACCOUNT_PRODUCT_ID, account.getProductId())
                .build();
    }
}
