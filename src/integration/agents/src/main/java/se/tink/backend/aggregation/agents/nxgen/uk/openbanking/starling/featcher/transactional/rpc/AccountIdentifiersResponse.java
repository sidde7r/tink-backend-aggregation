package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class AccountIdentifiersResponse {

    private String accountIdentifier;
    private String bankIdentifier;
    private String iban;
    private String bic;

    public String getAccountIdentifier() {
        return accountIdentifier;
    }

    public String getBankIdentifier() {
        return bankIdentifier;
    }

    public String getIban() {
        return iban;
    }

    public String getBic() {
        return bic;
    }

    public AccountIdentifier getIbanIdentifier() {
        return AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban);
    }

    // Required to reflect UK.OBIE.SortCodeAccountNumber format
    public AccountIdentifier getSortCodeAccountNumber() {
        return AccountIdentifier.create(
                AccountIdentifier.Type.SORT_CODE, bankIdentifier + accountIdentifier);
    }
}
