package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard.entities.CardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard.entities.RepoEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.PaginationEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.UserEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardTransactionsResponse {
    @JsonProperty("lista")
    private CardTransactionsList cardTransactionsList;

    @JsonProperty("finLista")
    private String finLista;

    @JsonProperty("_links")
    private PaginationEntity pagination;

    @JsonProperty("numPersonaCliente")
    private UserEntity userInfo;

    @JsonProperty("repo")
    private RepoEntity repo;

    @JsonProperty("descProducto")
    private String productDescription;

    @JsonProperty("nombreTitular")
    private String accountHolder;

    public static CardTransactionsResponse empty() {
        return new CardTransactionsResponse();
    }

    public List<CardTransactionEntity> getCardTransactions() {
        return cardTransactionsList.cardTransactions;
    }

    public String getFinLista() {
        return finLista;
    }

    public PaginationEntity getPagination() {
        return pagination;
    }

    public UserEntity getUserInfo() {
        return userInfo;
    }

    public RepoEntity getRepo() {
        return repo;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    private static class CardTransactionsList {
        @JsonProperty("movimientos")
        private List<CardTransactionEntity> cardTransactions;
    }
}
