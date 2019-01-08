package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.BalanceDataEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.MetaEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;

@JsonObject
public class CreditBalanceResponse {
  @JsonProperty("Data")
  private BalanceDataEntity data;

  @JsonProperty("Links")
  private LinksEntity links;

  @JsonProperty("Meta")
  private MetaEntity meta;

  public Amount getBalance(String accountId) { // TODO: verify if there can be more than 1
    BalanceEntity balance =
        data.getBalance()
            .stream()
            .filter(balanceEntity -> balanceEntity.getAccountId().equalsIgnoreCase(accountId))
            .findFirst()
            .get();
    return new Amount(
        balance.getBalanceEntity().getCurrency(),
        Double.parseDouble(balance.getBalanceEntity().getAmount()));
  }

  public Amount getAvailableCredit(String accountId) { // TODO: verify if there can be more than 1
    BalanceEntity balance =
        data.getBalance()
            .stream()
            .filter(balanceEntity -> balanceEntity.getAccountId().equalsIgnoreCase(accountId))
            .findFirst()
            .get();
    return new Amount(
        balance.getBalanceEntity().getCurrency(),
        Double.parseDouble(balance.getBalanceEntity().getAvailableLimit()));
  }
}
