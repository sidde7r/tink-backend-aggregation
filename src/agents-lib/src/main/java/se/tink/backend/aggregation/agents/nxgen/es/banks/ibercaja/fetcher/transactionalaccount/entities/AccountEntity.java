package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.Portfolio;
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
    @JsonProperty("IBAN")
    private String iban;

    public TransactionalAccount toTinkAccount() {

        return TransactionalAccount.builder(IberCajaConstants.ACCOUNT_TYPE_MAPPER.translate(type).get(), iban,
                new Amount(IberCajaConstants.currency, balance))
                .setAccountNumber(number)
                .addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .setBankIdentifier(number)
                .setBalance(new Amount(IberCajaConstants.currency, balance))
                .setName(alias)
                .build();
    }

    public InvestmentAccount toTinkInvestmentAccount() {

        Portfolio portfolio = new Portfolio();
        portfolio.setType(Portfolio.Type.DEPOT);

        return InvestmentAccount.builder(iban)
                .setAccountNumber(number)
                .addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .setBankIdentifier(number)
                .setName(alias)
                .setBalance(new Amount(IberCajaConstants.currency, balance))
                .setPortfolios(Collections.singletonList(portfolio))
                .build();
    }

    public CreditCardAccount toTinkCreditCardAccount() {

        return CreditCardAccount.builder(number, new Amount(IberCajaConstants.currency, balance),
                new Amount(IberCajaConstants.currency, limit))
                .setAccountNumber(number)
                .setBankIdentifier(number)
                .setBalance(new Amount(IberCajaConstants.currency, balance))
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
