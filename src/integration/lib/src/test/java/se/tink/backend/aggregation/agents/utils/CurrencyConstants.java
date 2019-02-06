package se.tink.backend.aggregation.agents.utils;

public enum CurrencyConstants {
    SE("SEK", "kr"),
    FI("EUR", "€"),
    AT("EUR", "€"),
    BE("EUR", "€"),
    ES("EUR", "€"),
    NL("EUR", "€"),
    DE("EUR", "€"),
    FR("EUR", "€"),
    DK("DKK", "kr"),
    NO("NOK", "kr"),
    UK("GBP", "£");

    private final String code;
    private final String symbol;

    CurrencyConstants(String code, String symbol) {
        this.code = code;
        this.symbol = symbol;
    }

    public String getCode() {
        return this.code;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public String getCountryCode(){
        return this.name();
    }

}
