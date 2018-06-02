package se.tink.backend.common.health;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Strings;
import se.tink.backend.connector.api.AbnAmroConnectorService;

public class AbnAmroConnectorServiceHealthCheck extends HealthCheck {

    private AbnAmroConnectorService abnAmroConnectorService;

    public AbnAmroConnectorServiceHealthCheck(AbnAmroConnectorService abnAmroConnectorService) {
        this.abnAmroConnectorService = abnAmroConnectorService;
    }
    
    @Override
    protected Result check() throws Exception {
        if (Strings.isNullOrEmpty(abnAmroConnectorService.ping())) {
            return Result.unhealthy("No answer");
        } else {
            return Result.healthy();
        }
    }

}
