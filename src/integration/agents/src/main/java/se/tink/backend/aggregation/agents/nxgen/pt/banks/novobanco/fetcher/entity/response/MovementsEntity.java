package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MovementsEntity {
    @JsonProperty("Saldo")
    private double balance;

    @JsonProperty("Tipo")
    private int type;

    @JsonProperty("Descricao")
    private String description;

    @JsonProperty("Montante")
    private double sum;

    @JsonProperty("DataValor")
    private String dateValue;

    @JsonProperty("DataOperacao")
    private String dateOfOperation;

    @JsonProperty("Numero")
    private int number;

    @JsonProperty("NumPedido")
    private String inOrder;

    @JsonProperty("Categoria")
    private String category;

    @JsonProperty("IdCategoria")
    private int idCategory;

    @JsonProperty("OfIdN1")
    private int ofIdn1;

    @JsonProperty("OfIcon")
    private int ofIcon;

    public double getBalance() {
        return balance;
    }

    public int getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public double getSum() {
        return sum;
    }

    public String getDateValue() {
        return dateValue;
    }

    public String getDateOfOperation() {
        return dateOfOperation;
    }

    public int getNumber() {
        return number;
    }

    public String getInOrder() {
        return inOrder;
    }

    public String getCategory() {
        return category;
    }

    public int getIdCategory() {
        return idCategory;
    }

    public int getOfIdn1() {
        return ofIdn1;
    }

    public int getOfIcon() {
        return ofIcon;
    }
}
