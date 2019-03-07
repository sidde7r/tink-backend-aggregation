package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class AnswerEntityTransactionsResponse {
    @JsonProperty("codigoMoneda")
    private String currencyCode;

    @JsonProperty("codigoInternoCentro")
    private String internalCodeCenter;

    @JsonProperty("codigoExtMoneda")
    private String extCurrencyCode;

    @JsonProperty("DatosTarjeta")
    private CardDataEntityAccountTransactionsResponse cardData;

    @JsonProperty("masDatos")
    private String moreData;

    @JsonProperty("titular")
    private String headline;

    @JsonProperty("ListaApuntesCliente")
    private List<CustomerNotesListEntity> listCustomerNotes;

    @JsonProperty("numeroRegistros")
    private String registersNumber;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setListCustomerNotes(List<CustomerNotesListEntity> listCustomerNotes) {
        this.listCustomerNotes = listCustomerNotes;
    }

    public List<CustomerNotesListEntity> getListCustomerNotes() {
        return listCustomerNotes;
    }
}
