package se.tink.backend.common.workers.activity.generators;

import java.util.List;
import org.joda.time.DateTime;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Activity;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.utils.StringUtils;

/**
 * Generate the Year in Numbers activity.
 * <p>
 * Limitations: It is only enabled for year 2016.
 */
public class YearInNumbersActivityGenerator extends ActivityGenerator {

    private DateTime startDate = DateTime.parse("2016-12-30");
    private DateTime endDate = DateTime.parse("2017-01-31");

    public YearInNumbersActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(YearInNumbersActivityGenerator.class, 70, deepLinkBuilderFactory);

        minIosVersion = "2.5.7";
        minAndroidVersion = "9999.9.9"; // Disabled for android
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {

        final List<String> userFlags = context.getUser().getFlags();
        final DateTime now = DateTime.now();

        if (userFlags.contains(FeatureFlags.TINK_EMPLOYEE) || userFlags.contains(FeatureFlags.IOS_BETA) || (
                now.isAfter(startDate) && now.isBefore(endDate))) {
            context.addActivity(generateActivity(context.getUser().getId()));
        }
    }

    private Activity generateActivity(String userId) {

        String year = "2016";

        String key = String.format("%s.%s", Activity.Types.YEAR_IN_NUMBERS, year);

        String identifier = getIdentifier(key);

        return createActivity(
                userId,
                DateTime.parse("2017-01-12").toDate(),
                Activity.Types.YEAR_IN_NUMBERS,
                "Year in numbers" + year,
                null,
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
