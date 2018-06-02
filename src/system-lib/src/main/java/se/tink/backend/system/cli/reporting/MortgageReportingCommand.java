package se.tink.backend.system.cli.reporting;

import com.google.common.base.Strings;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Date;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.application.mortgage.CompileAndSendReportCommand;
import se.tink.backend.common.application.mortgage.SwitchMortgageProviderReportingController;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.DateUtils;

public class MortgageReportingCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(MortgageReportingCommand.class);

    public MortgageReportingCommand() {
        super("mortgage-reporting", "");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        SwitchMortgageProviderReportingController controller = injector
                .getInstance(SwitchMortgageProviderReportingController.class);
        controller.setDryRun(Boolean.getBoolean("dryRun"));
        controller.setPrint(true);
        controller.compileAndSendReport(getCommand());
    }

    private CompileAndSendReportCommand getCommand() {
        String month = System.getProperty("month");
        String fromDateString = System.getProperty("fromDate");
        String toDateString = System.getProperty("toDate");

        if (!Strings.isNullOrEmpty(month)) {
            if (!Strings.isNullOrEmpty(fromDateString)) {
                log.info("Ignoring `fromDate`");
            }

            if (!Strings.isNullOrEmpty(toDateString)) {
                log.info("Ignoring `toDate`");
            }

            return CompileAndSendReportCommand.forMonth(month);
        } else if (!Strings.isNullOrEmpty(fromDateString) || !Strings.isNullOrEmpty(toDateString)) {
            if (Strings.isNullOrEmpty(fromDateString)) {
                log.error("Missing `fromDate`. Aborting.");
                return null;
            }

            if (Strings.isNullOrEmpty(toDateString)) {
                log.error("Missing `toDate`. Aborting.");
                return null;
            }

            Date fromDate = DateUtils.setInclusiveStartTime(DateUtils.parseDate(fromDateString));
            Date toDate = DateUtils.setInclusiveEndTime(DateUtils.parseDate(toDateString));

            return CompileAndSendReportCommand.forPeriod(fromDate, toDate);
        } else {
            return CompileAndSendReportCommand.forLastCompleteMonth();
        }
    }
}
