package se.tink.agent.runtime.operation;

import se.tink.agent.sdk.operation.Provider;

public class ProviderImpl implements Provider {
    private final String market;
    private final String name;
    private final String currency;
    private final String payload;

    public ProviderImpl(String market, String name, String currency, String payload) {
        this.market = market;
        this.name = name;
        this.currency = currency;
        this.payload = payload;
    }

    @Override
    public String getMarket() {
        return this.market;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getCurrency() {
        return this.currency;
    }

    @Override
    public String getPayload() {
        return this.payload;
    }
}
