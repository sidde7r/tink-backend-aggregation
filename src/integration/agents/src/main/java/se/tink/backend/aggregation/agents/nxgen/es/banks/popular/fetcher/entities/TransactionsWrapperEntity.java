package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsWrapperEntity {
    @JsonProperty("hayMas")
    private String hasMore;
    @JsonProperty("fechasaldo")
    private long dateBalance;
    @JsonProperty("saldo")
    private int balance;
    @JsonProperty("signosaldo")
    private String signBalance;
    @JsonProperty("fechasaldoval")
    private long dateBalanceValue;
    @JsonProperty("saldoval")
    private int balanceValue;
    @JsonProperty("signosaldoval")
    private String signBalanceValue;
    private String codmonedacta;
    @JsonProperty("indicadorRecibos")
    private String receiptsIndicator;
    private int noccurspartemv;
    @JsonProperty("customEccas211SPARTEMV")
    private List<TransactionEntity> transactionList;

    public String getHasMore() {
        return hasMore;
    }

    public long getDateBalance() {
        return dateBalance;
    }

    public int getBalance() {
        return balance;
    }

    public String getSignBalance() {
        return signBalance;
    }

    public long getDateBalanceValue() {
        return dateBalanceValue;
    }

    public int getBalanceValue() {
        return balanceValue;
    }

    public String getSignBalanceValue() {
        return signBalanceValue;
    }

    public String getCodmonedacta() {
        return codmonedacta;
    }

    public String getReceiptsIndicator() {
        return receiptsIndicator;
    }

    public int getNoccurspartemv() {
        return noccurspartemv;
    }

    public List<TransactionEntity> getTransactionList() {
        return transactionList;
    }
}
