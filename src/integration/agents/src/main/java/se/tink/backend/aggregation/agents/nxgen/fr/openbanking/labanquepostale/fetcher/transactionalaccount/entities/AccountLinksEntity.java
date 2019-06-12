package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities;


import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountLinksEntity {
  LinksEntity balances;
  LinksEntity transactions;

  public LinksEntity getBalances() {return balances;}
  public LinksEntity getTransactions() {return transactions;}
}
