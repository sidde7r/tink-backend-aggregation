package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.core.Amount;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class AccountEntity {

    @JsonProperty("Numero")
    private String number;
    @JsonProperty("Tipo")
    private int type;
    @JsonProperty("TipoTarjeta")
    private int typeCard;
    @JsonProperty("EmisorTarjeta")
    private int cardIssuer;
    @JsonProperty("Alias")
    private String alias;
    @JsonProperty("Saldo")
    private double balance;
    @JsonProperty("Limite")
    private double limit;
    @JsonProperty("Dispuesto")
    private double disposed;
    @JsonProperty("IBAN")
    private String iban;

    public TransactionalAccount toTinkAccount() {

        return TransactionalAccount.builder(IberCajaConstants.ACCOUNT_TYPE_MAPPER.translate(type).get(), iban,
                new Amount(IberCajaConstants.currency, balance))
                .setAccountNumber(iban)
                .addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .setBankIdentifier(number)
                .setBalance(new Amount(IberCajaConstants.currency, balance))
                .setName(alias)
                .build();
    }

    public InvestmentAccount toTinkInvestmentAccount() {

        Portfolio portfolio = toTinkPortfolio();

        return InvestmentAccount.builder(iban)
                .setAccountNumber(number)
                .addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .setBankIdentifier(number)
                .setName(alias)
                .setPortfolios(Collections.singletonList(portfolio))
                .setCashBalance(new Amount(IberCajaConstants.currency, balance))
                .build();
    }

    private Portfolio toTinkPortfolio() {

        // Ibercaja seem to use the same model for all types of accounts, can not find more portfolio data
        // for our ambassador credentials. Hopefully logging for accounts with active investments will yield
        // more info.

        Portfolio portfolio = new Portfolio();

        portfolio.setUniqueIdentifier(iban);
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setCashValue(balance);   // this is a guessing game, will probably need to be revised.
        portfolio.setTotalValue(disposed); // this is a guessing game, will probably need to be revised.

        return portfolio;
    }

    public CreditCardAccount toTinkCreditCardAccount() {

        return CreditCardAccount.builder(number, new Amount(IberCajaConstants.currency, disposed),
                new Amount(IberCajaConstants.currency, limit))
                .setAccountNumber(number)
                .setBankIdentifier(number)
                .setName(alias)
                .build();
    }

    public int getType() {
        return type;
    }

    public int getTypeCard() {
        return typeCard;
    }
}
