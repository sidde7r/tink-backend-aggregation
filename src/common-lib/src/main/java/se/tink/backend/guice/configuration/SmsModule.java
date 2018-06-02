package se.tink.backend.guice.configuration;

import com.google.inject.AbstractModule;
import se.tink.backend.common.config.SmsConfiguration;
import se.tink.backend.sms.gateways.cmtelecom.CmTelecomGateway;
import se.tink.backend.sms.gateways.SmsGateway;
import se.tink.backend.sms.gateways.dummy.DummyGateway;

public class SmsModule extends AbstractModule {
    private final SmsConfiguration configuration;

    public SmsModule(SmsConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        if (configuration.getOtp().isEnabled()) {
            if (configuration.getOtp().isDummyMode()) {
                bind(SmsGateway.class).to(DummyGateway.class);
            } else {
                bind(SmsGateway.class).to(CmTelecomGateway.class);
            }
        }
    }
}
