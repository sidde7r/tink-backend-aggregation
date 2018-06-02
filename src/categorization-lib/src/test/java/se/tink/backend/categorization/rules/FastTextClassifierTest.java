package se.tink.backend.categorization.rules;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.Uninterruptibles;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.ClusterCategories;

public class FastTextClassifierTest {

    private FastTextClassifier fasttext;
    private File tempModel;

    @Before
    public void setup() throws IOException {
        Collection<Category> categories = new ArrayList<Category>();
        categories.add(new Category("Restaurant", "", "food.restaurants", 1, CategoryTypes.EXPENSES));
        categories.add(new Category("Insurance fees", "", "home.incurences-fees", 2, CategoryTypes.EXPENSES));
        categories.add(new Category("Mobile phone", "", "home.communications", 3, CategoryTypes.EXPENSES));
        categories.add(new Category("Car", "", "transport.car", 4, CategoryTypes.EXPENSES));

        tempModel = File.createTempFile("fasttextTempModel", ".bin");
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(tempModel))) {
            InputStream stream = this.getClass().getResourceAsStream("/categorization-lib/minimal_model.bin");
            Preconditions.checkState(stream != null, "Test resource could not be found.");
            try {
                ByteStreams.copy(stream, out);
            } finally {
                stream.close();
            }
        }

        File executable = File.createTempFile("fasttext", "_executable");
        executable.deleteOnExit();
        InputStream stream = this.getClass().getResourceAsStream("/fasttext");
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(executable));
        ByteStreams.copy(stream, out);
        stream.close();
        out.close();
        executable.setExecutable(true);

        CategoryConfiguration categoryConfiguration = new SECategories();
        ClusterCategories clusterCategories = new ClusterCategories(categories);
        CategorizationConfiguration categorizationConfiguration = new CategorizationConfiguration();
        categorizationConfiguration.setMinimumPercentage(0.0);
        categorizationConfiguration.setTopTwoPercentageDelta(0.0);
        categorizationConfiguration.setUsePatchedFastText(true);
        fasttext = new FastTextClassifier(executable, tempModel, categoryConfiguration, categorizationConfiguration, 3,
                clusterCategories, ImmutableList.of(Function.identity()));

        fasttext.start();
    }

    @After
    public void tearDown() {
        fasttext.stop();
        tempModel.delete();
    }

    @Test
    public void testPrediction() {
        Map<String, Double> predictedCategorization = fasttext.predict("pizza").get();
        String highestPredictedCategory = Collections
                .max(predictedCategorization.entrySet(), Map.Entry.comparingByValue()).getKey();
        Assert.assertEquals("expenses:food.restaurants", highestPredictedCategory);
    }

    @Test
    public void testNewlinePrediction() {
        Map<String, Double> predictedCategorization = fasttext.predict("\npizza\n\n").get();
        String highestPredictedCategory = Collections
                .max(predictedCategorization.entrySet(), Map.Entry.comparingByValue()).getKey();
        Assert.assertEquals("expenses:food.restaurants", highestPredictedCategory);
    }

    @Test
    public void testTextEncodingPrediction() {
        Map<String, Double> predictedCategorization = fasttext.predict("försäkring").get();
        String highestPredictedCategory = Collections
                .max(predictedCategorization.entrySet(), Map.Entry.comparingByValue()).getKey();
        Assert.assertEquals("expenses:home.incurences-fees", highestPredictedCategory);
    }

    @Test
    public void testThreadedPrediction() {
        final int NUMBER_OF_THREADS = 2000;
        final String[] predict = { "telefon", "pizza", "bil" };
        final String[] answers = {
                "expenses:home.communications",
                "expenses:food.restaurants",
                "expenses:transport.car"
        };
        final CountDownLatch synchronizationLatch = new CountDownLatch(1);
        final CountDownLatch finishedRunning = new CountDownLatch(NUMBER_OF_THREADS);
        final LongAdder didNotError = new LongAdder();
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            final int current = i;
            Runnable runner = () -> {
                Uninterruptibles.awaitUninterruptibly(synchronizationLatch, 1, TimeUnit.MINUTES);
                if (synchronizationLatch.getCount() > 0) {
                    return;
                }

                Map<String, Double> predictedCategorization = fasttext.predict(predict[current % 3]).get();
                String highestPredictedCategory = Collections
                        .max(predictedCategorization.entrySet(), Map.Entry.comparingByValue()).getKey();
                if (answers[current % 3].equals(highestPredictedCategory)) {
                    didNotError.add(1);
                }
                finishedRunning.countDown();
            };
            new Thread(runner, "Thread_" + i).start();
        }

        synchronizationLatch.countDown();

        // To make sure we won't accidentally wait forever due to some problem.
        Uninterruptibles.awaitUninterruptibly(finishedRunning, 1, TimeUnit.MINUTES);

        Assert.assertEquals(0, finishedRunning.getCount());
        Assert.assertEquals(NUMBER_OF_THREADS, didNotError.intValue());

    }

}
