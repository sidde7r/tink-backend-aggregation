package se.tink.backend.common.workers.activity.generators;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.utils.StringUtils;

public class SummerInNumbersActivityGenerator extends ActivityGenerator {

    private DateTime startDate = DateTime.parse("2017-07-12");
    private DateTime endDate = DateTime.parse("2017-08-31");

    private final static Date summerStart = DateTime.parse("2016-06-01").toDate();
    private final static Date summerEnd = DateTime.parse("2016-09-01").toDate();

    public SummerInNumbersActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(SummerInNumbersActivityGenerator.class, 70, deepLinkBuilderFactory);

        minIosVersion = "2.5.16";
        minAndroidVersion = "9999.9.9"; // Disabled for android
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {

        final List<String> userFlags = context.getUser().getFlags();
        final DateTime now = DateTime.now();

        if ((userFlags.contains(FeatureFlags.TINK_EMPLOYEE) || userFlags.contains(FeatureFlags.IOS_BETA)) || (now.isAfter(startDate) && now.isBefore(endDate) && userTransactedLastSummer(context.getTransactions()))) {
            context.addActivity(generateActivity(context.getUser().getId()));
        }
    }

    private Activity generateActivity(String userId) {

        String year = "2016";

        String key = String.format("%s.%s", Activity.Types.SUMMER_IN_NUMBERS, year);

        String identifier = getIdentifier(key);

        return createActivity(
                userId,
                DateTime.parse("2017-08-17").toDate(),
                Activity.Types.SUMMER_IN_NUMBERS,
                "Summer in numbers " + year,
                null,
                null,
                key,
                identifier);
    }

    private String getIdentifier(String key) {
        return StringUtils.hashAsStringSHA1(key);
    }

    private Boolean userTransactedLastSummer(List<Transaction> transactions) {
        List<Transaction> summerTransactions = transactions.stream()
                .filter(t -> (t.getDate().after(summerStart) && t.getDate().before(summerEnd)))
                .collect(Collectors.toList());

        return summerTransactions.size() >= 1;
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }
}
