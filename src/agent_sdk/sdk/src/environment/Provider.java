package se.tink.agent.sdk.environment;

public interface Provider {
    String getMarket();

    String getName();

    String getCurrency();

    String getPayload();
}
