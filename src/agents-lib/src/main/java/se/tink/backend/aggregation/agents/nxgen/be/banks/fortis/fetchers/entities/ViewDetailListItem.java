package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class ViewDetailListItem {
    private String accountType;
    private Object viewDetailId;
    private String accountSequenceNumber;
    private String accountNumber;
    private Account account;
    private static final AggregationLogger LOGGER = new AggregationLogger(ViewDetailListItem.class);

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

    private AccountTypes getTinkAccountType() {
        String type = getAccount().getAccountType(); // TODO: verify
        Optional<AccountTypes> accountType = FortisConstants.ACCOUNT_TYPE_MAPPER.translate(type);
        return accountType.orElse(AccountTypes.OTHER);
    }

    private String getIban() {
        return getAccount().getIban();
    }

    private Amount getTinkAmount() {
        return new Amount(getAccount().getBalance().getCurrency(),
                Double.parseDouble(getAccount().getBalance().getAmount()));
    }

    private String getAccountName() {
        return getAccount().getAccountName();
    }

    public boolean isValid() {
        try {
            toTinkAccount();
            return true;
        } catch (Exception e) {
            LOGGER.errorExtraLong("error validating transaction!", FortisConstants.LOGTAG.TRANSACTION_VALIDATION_ERR,
                    e);
            return false;
        }
    }

    private AccountIdentifier getIbanIdentifier() {
        return AccountIdentifier.create(AccountIdentifier.Type.IBAN, getIban(), AccountIdentifier.Type.IBAN.toString());
    }

    private HolderName getHoldername() {
        return new HolderName(getAccount().getAlias());
    }

    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.builder(getTinkAccountType(), getIban(), getTinkAmount())
                .setAccountNumber(getIban())
                .setHolderName(getHoldername())
                .setName(getAccountName())
                .addIdentifier(getIbanIdentifier())
                .putInTemporaryStorage(FortisConstants.STORAGE.ACCOUNT_PRODUCT_ID, account.getProductId())
                .build();
    }

}
