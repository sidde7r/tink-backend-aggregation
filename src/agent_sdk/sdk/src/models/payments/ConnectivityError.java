package se.tink.agent.sdk.models.payments;

public interface ConnectivityError {
    String getReason();

    String getDisplayMessage();

    boolean isRetryable();
}
