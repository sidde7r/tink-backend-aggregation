package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.module;

import com.google.inject.AbstractModule;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.verdict.PaymentVerdict;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.verdict.PaymentVerdicter;

public class VerdictModule extends AbstractModule {

    public VerdictModule() {}

    @Override
    public void configure() {
        bind(PaymentVerdict.class).to(PaymentVerdicter.class);
    }
}
