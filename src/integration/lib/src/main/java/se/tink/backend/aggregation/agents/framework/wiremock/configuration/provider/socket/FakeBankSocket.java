package se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket;

import java.util.function.Supplier;

// TODO change to Supplier<InetSocketAddress> ? With annotation?
public interface FakeBankSocket extends Supplier<String> {

    @Override
    String get();
}
