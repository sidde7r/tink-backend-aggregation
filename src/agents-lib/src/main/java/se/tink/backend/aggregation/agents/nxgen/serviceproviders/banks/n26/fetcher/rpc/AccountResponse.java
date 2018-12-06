package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class AccountResponse {

    private String id;

    private double availableBalance;

    private double usableBalance;

    private double bankBalance;

    private String iban;

    private String bic;

    private String bankName;

    private boolean seized;

    private String currency;

    public String getId() {
        return id;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public double getUsableBalance() {
        return usableBalance;
    }

    public double getBankBalance() {
        return bankBalance;
    }

    public String getIban() {
        return iban;
    }

    public String getBic() {
        return bic;
    }

    public String getBankName() {
        return bankName;
    }

    public boolean isSeized() {
        return seized;
    }

    public AccountTypes getType(){
        return AccountTypes.CHECKING;
    }

    public Amount getTinkBalance(){
        return new Amount(currency, availableBalance);
    }

    public TransactionalAccount toTransactionalAccount(){

        return TransactionalAccount.builder(getType(), getId(), getTinkBalance())
                .setAccountNumber(getId())
                .setName(getBankName())
                .setAccountNumber(getIban())
                .addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .build();
    }
}
