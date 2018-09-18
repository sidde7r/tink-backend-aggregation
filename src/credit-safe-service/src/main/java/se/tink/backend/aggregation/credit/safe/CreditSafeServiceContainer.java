package se.tink.backend.aggregation.credit.safe;

import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import se.tink.backend.common.AbstractServiceContainer;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;

public class CreditSafeServiceContainer extends AbstractServiceContainer {

    public static void main(String[] args) throws Exception {
        new CreditSafeServiceContainer().run(args);
    }

    @Override
    public String getName() {
        return "CREDIT_SAFE";
    }

    @Override
    public void initialize(Bootstrap<ServiceConfiguration> bootstrap) {
        DropwizardObjectMapperConfigurator.doNotFailOnUnknownProperties(bootstrap);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void build(ServiceConfiguration configuration, Environment environment) throws Exception {

    }
}
