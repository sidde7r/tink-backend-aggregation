package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;

@JsonObject
public class AmountEntity {
    private int value;
    private int precision;
    private String currency;

    public int getValue() {
        return value;
    }

    public int getPrecision() {
        return precision;
    }

    public String getCurrency() {
        return currency;
    }

    public Amount getTinkBalance(){
        String value = Integer.toString(getValue());

        if(value.length() == 1){
            return new Amount(getCurrency(), Double.parseDouble(value));
        }

        value = new StringBuffer(value).insert(value.length()-getPrecision(), ".").toString();
        double doubleValue = Double.parseDouble(value);
        return new Amount(getCurrency(), doubleValue);

    }
}
