package se.tink.agent.sdk.operation;

public interface Provider {
    String getMarket();

    String getName();

    String getCurrency();

    String getPayload();
}
