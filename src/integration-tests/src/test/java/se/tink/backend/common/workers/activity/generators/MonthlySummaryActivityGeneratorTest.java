package se.tink.backend.common.workers.activity.generators;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.common.ActivityGeneratorWorkerTestBase;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.generators.models.MonthlySummaryActivityData;
import se.tink.backend.core.Activity;

/**
 * TODO this is a unit test
 */
public class MonthlySummaryActivityGeneratorTest extends ActivityGeneratorWorkerTestBase {

    @Test
    public void testMonthlySummaryActivityGenerator_numberOfActivities() {

        getActivityGeneratorContext().forEach(context -> {

            MonthlySummaryActivityGenerator generator = new MonthlySummaryActivityGenerator(new DeepLinkBuilderFactory(""));

            generator.generateActivity(context);

            List<Activity> activities = context.getActivities();

            int expNumberOfActivities = 1;
            int actualNumberOfActivities = 0;

            if (activities != null) {

                actualNumberOfActivities = activities.size();
            }

            Assert.assertEquals(expNumberOfActivities, actualNumberOfActivities);
        });
    }

    @Test
    public void testMonthlySummaryActivityGenerator_Income() {
        getActivityGeneratorContext().forEach(context -> {

            MonthlySummaryActivityGenerator generator = new MonthlySummaryActivityGenerator(new DeepLinkBuilderFactory(""));

            generator.generateActivity(context);

            List<Activity> activities = context.getActivities();

            double expIncome = 50000;
            double actualIncome = 0;

            if (activities != null) {

                if (activities.size() == 1) {

                    MonthlySummaryActivityData content = (MonthlySummaryActivityData) activities.get(0).getContent();

                    if (content != null) {

                        actualIncome = content.getIncome();
                    }
                }
            }

            Assert.assertEquals(expIncome, actualIncome, 0);
        });
    }

    @Test
    public void testFirstLetterIsUpperCase() throws Exception {
        getActivityGeneratorContext().forEach(context -> {

        MonthlySummaryActivityGenerator generator = new MonthlySummaryActivityGenerator(new DeepLinkBuilderFactory(""));

        generator.generateActivity(context);

        List<Activity> activities = context.getActivities();

        Assert.assertNotNull(activities);
        Assert.assertNotEquals(0, activities.size());

        for (Activity activity : activities) {
            String message = activity.getMessage();
            Assert.assertNotNull(message);
            Assert.assertNotEquals(0, message.length());
            String letter = message.substring(0, 1);
            Assert.assertEquals("Message is not stating from upper case letter", letter.toUpperCase(), letter);
        }
        });
    }
}
