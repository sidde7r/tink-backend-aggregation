package se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket;

public interface FakeBankSocket {

    String getHttpHost();

    String getHttpsHost();
}
