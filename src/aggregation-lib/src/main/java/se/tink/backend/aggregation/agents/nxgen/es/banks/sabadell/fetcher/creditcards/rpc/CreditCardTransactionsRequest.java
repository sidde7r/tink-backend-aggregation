package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.PaginatorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardTransactionsRequest {
    private String dateFrom;
    private String dateTo;
    private CreditCardEntity card;
    private PaginatorEntity paginator;

    @JsonIgnore
    public static CreditCardTransactionsRequest build(CreditCardEntity cardEntity, int totalItems, int page) {
        PaginatorEntity cardPaginator = new PaginatorEntity();
        cardPaginator.setItemsPerPage(20);
        cardPaginator.setOrder("desc");
        cardPaginator.setTotalItems(totalItems);
        cardPaginator.setPage(page);

        CreditCardTransactionsRequest cardTransactionsRequest = new CreditCardTransactionsRequest();
        cardTransactionsRequest.setDateFrom("");
        cardTransactionsRequest.setDateTo("");
        cardTransactionsRequest.setCard(cardEntity);
        cardTransactionsRequest.setPaginator(cardPaginator);

        return cardTransactionsRequest;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public CreditCardEntity getCard() {
        return card;
    }

    public void setCard(CreditCardEntity card) {
        this.card = card;
    }

    public PaginatorEntity getPaginator() {
        return paginator;
    }

    public void setPaginator(
            PaginatorEntity paginator) {
        this.paginator = paginator;
    }
}
