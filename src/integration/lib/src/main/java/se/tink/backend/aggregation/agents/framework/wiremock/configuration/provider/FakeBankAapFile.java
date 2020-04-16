package se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface FakeBankAapFile {}
