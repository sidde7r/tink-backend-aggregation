package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {

    private long unscaledValue = 0L;
    private int scale = 0;
    private CurrencyEntity coin = null;

    // they use different names on different places in the api, but they do EXACTLY the same thing
    @JsonProperty("importeConSigno")
    public void setAmountWithSign(int amountWithSign) {
        this.unscaledValue = amountWithSign;
    }

    @JsonProperty("importe")
    public void setAmount(int amountWithSign) {
        this.unscaledValue = amountWithSign;
    }

    @JsonProperty("numeroDecimales")
    public void setDecimalsNumber(int decimalsNumber) {
        this.scale = decimalsNumber;
    }

    @JsonProperty("decimales")
    public void setDecimals(int decimalsNumber) {
        this.scale = decimalsNumber;
    }

    @JsonProperty("moneda")
    public void setCoin(CurrencyEntity coin) {
        this.coin = coin;
    }

    public Amount toTinkAmount() {
        return new Amount(
                coin.getShortName(), BigDecimal.valueOf(unscaledValue, scale).doubleValue());
    }

    @JsonProperty("numeroDecimalesImporte")
    public void setDecimalsNumberAmount(String decimalsNumberAmount) {
        this.scale = Integer.parseInt(decimalsNumberAmount);
    }

    @JsonProperty("nombreMoneda")
    public void setCurrencyName(String currencyName) {
        Objects.requireNonNull(currencyName);
        if (coin == null) {
            coin = new CurrencyEntity();
        }
        coin.setShortName(currencyName);
    }
}
