package se.tink.backend.core;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.protostuff.Tag;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;

@Entity
@Table(name = "currencies")
public class Currency {
    private static final int LARGE_AMOUNT_THRESHOLD_FACTOR = 20;

    @Id
    @Tag(1)
    private String code;
    @Tag(2)
    private double factor;
    @Tag(3)
    private boolean prefixed;
    @Tag(4)
    private String symbol;

    public Currency() {

    }

    public Currency(String code, String symbol, boolean prefixed, double factor) {
        this.code = code;
        this.symbol = symbol;
        this.prefixed = prefixed;
        this.factor = factor;
    }

    @ApiModelProperty(name = "code", value="The ISO 4217 code of the currency.", example="SEK", required = true)
    public String getCode() {
        return code;
    }

    @ApiModelProperty(name = "factor", value="An approximate currency conversion factor to inversely scale triggers to the EUR currency.", example="10", required = true)
    public double getFactor() {
        return factor;
    }

    @JsonIgnore
    public double getLargeAmountThreshold() {
        return factor * LARGE_AMOUNT_THRESHOLD_FACTOR;
    }

    @ApiModelProperty(name = "symbol", value="The symbol of the currency.", example="kr", required = true)
    public String getSymbol() {
        return symbol;
    }

    @ApiModelProperty(name = "prefixed", value="Indicates that the currency symbol should prefix the amount.", required = true)
    public boolean isPrefixed() {
        return prefixed;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public void setPrefixed(boolean prefixed) {
        this.prefixed = prefixed;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
