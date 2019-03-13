package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.entities;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountsEntity {
  private String iban;

  private String currencyCode;

  private String bic;

  private String accountType;

  private Number currentBalance;

  private String productDescription;

  public TransactionalAccount toTinkAccount(String owner) {
    return CheckingAccount.builder()
        .setUniqueIdentifier(iban)
        .setAccountNumber(bic)
        .setBalance(new Amount(currencyCode, currentBalance))
        .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.BE, bic))
        .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
        .addHolderName(getName(owner))
        .setAlias(getName(owner))
        .setApiIdentifier(bic)
        .putInTemporaryStorage(DeutscheBankConstants.StorageKeys.ACCOUNT_ID, iban)
        .build();
  }

  private String getName(String owner) {
    return Strings.isNullOrEmpty(owner) ? bic : owner;
  }
}
