package se.tink.backend.system.cli.fraud;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.workers.processor.creditsafe.CreditSafeDataRefresher;

public class CreditSafeDataRefresherCommand extends ServiceContextCommand<ServiceConfiguration> {
    public CreditSafeDataRefresherCommand() {
        super("credit-safe-refresh", "Refrehes CreditSafe data and cleans up monitoring if set");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        Integer changedDays = Integer.getInteger("changedDays", null);
        String mode = System.getProperty("mode", "all");
        
        CreditSafeDataRefresher refresher = injector.getInstance(CreditSafeDataRefresher.class);

        if (mode.equals("all")) {
            refresher.refreshCredentialsForIdControlUsers(changedDays);
            refresher.cleanUpMonitoredConsumers();
        }
        else if (mode.equals("refresh")) {
            refresher.refreshCredentialsForIdControlUsers(changedDays);            
        }
        else if (mode.equals("cleanupConsumers")) {
            refresher.cleanUpMonitoredConsumers();            
        }
    }
}
