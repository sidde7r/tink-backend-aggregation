package se.tink.backend.abnamro.workers.activity.generators;

import java.util.Date;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Activity;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AbnAmroMaintenanceActivityGenerator extends ActivityGenerator {

    private ActivityGeneratorContext context;

    public AbnAmroMaintenanceActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(AbnAmroMaintenanceActivityGenerator.class, 100, 100, deepLinkBuilderFactory);
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        this.context = context;

        if (context.getServiceContext().getConfiguration().getAbnAmro().isDownForMaintenance()) {
            context.addActivity(generateMaintenanceActivity());
        }
    }

    private Activity generateMaintenanceActivity() {

        String key = String.format("%s.%s", Activity.Types.MAINTENANCE_INFORMATION_ABNAMRO,
                ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date()));

        String identifier = getIdentifier(key);

        return createActivity(
                context.getUser().getId(),
                DateUtils.getToday(),
                Activity.Types.MAINTENANCE_INFORMATION_ABNAMRO,
                "Tijdelijk geen updates in de Grip-app",
                "We zijn bezig met onderhoud aan Grip. Overzichten in deze app worden tijdelijk niet bijgewerkt.",
                null,
                key,
                identifier);
    }

    private String getIdentifier(String key) {
        return StringUtils.hashAsStringSHA1(key);
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }
}
