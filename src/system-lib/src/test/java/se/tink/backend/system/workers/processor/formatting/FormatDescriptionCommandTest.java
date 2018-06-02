package se.tink.backend.system.workers.processor.formatting;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.core.User;
import se.tink.backend.util.GuiceRunner;
import se.tink.backend.util.TestUtil;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricRegistry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(GuiceRunner.class)
public class FormatDescriptionCommandTest {
    public static final Cluster CLUSTER = Cluster.TINK;

    private List<FormatDescriptionCommand> commands;
    private Map<String, TransactionProcessorContext> contexts;
    private List<User> users;
    private Provider provider;
    @Inject
    private TestUtil testUtil;

    @Inject
    private MetricRegistry metricRegistry;
    
    @Before
    public void setup() {
        provider = testUtil.getProvidersByName()
                .get("swedbank-bankid");
        users = testUtil.getTestUsers("extrapolateTest");
        contexts = users.stream().map(user -> new TransactionProcessorContext(
                user,
                testUtil.getProvidersByName(),
                Lists.newArrayList()
        )).collect(Collectors.toMap(tc -> tc.getUser().getId(), tc -> tc));

        commands = contexts.entrySet()
                .stream()
                .map(context -> new FormatDescriptionCommand(
                                context.getValue(),
                                MarketDescriptionFormatterFactory.byCluster(CLUSTER),
                                MarketDescriptionExtractorFactory.byCluster(CLUSTER),
                                metricRegistry,
                                provider
                        )
                ).collect(Collectors.toList());

        commands.forEach(c -> c.initialize());
    }
    
    @Test
    public void extrapolationWithEffectTest() {
        commands.forEach(command -> {
            Transaction t1 = testUtil.getNewTransaction("test1", -99, "MC DONALDS FÄLT");
            command.execute(t1);

            Transaction t2 = testUtil.getNewTransaction("test1", -99, "MC DONALDS FÄLTÖVERSTEN");
            command.execute(t2);

            String description = "Mc Donalds Fältöversten Stockholm";

            Assert.assertEquals(description, t1.getDescription());
            Assert.assertEquals(description, t2.getDescription());
        });
    }

    @Test
    public void extrapolationWithoutEffectTest() {
        commands.forEach(command -> {
            Transaction t1 = testUtil.getNewTransaction("test1", -99, "MC DONALDS");
            command.execute(t1);

            Transaction t2 = testUtil.getNewTransaction("test1", -99, "HEMKÖP");
            command.execute(t2);

            Assert.assertEquals("Mc Donalds", t1.getDescription());
            Assert.assertEquals("Hemköp", t2.getDescription());
        });
    }

    @Test
    public void extrapolateWithSpecialCharacterCleaning() {
        commands.forEach(command -> {
            Transaction t1 = testUtil.getNewTransaction("test1", -99, "Forex Ringv{ge");
            command.execute(t1);

            Transaction t2 = testUtil.getNewTransaction("test1", -99, "Forex Ringväge");
            command.execute(t2);

            String description = "Forex Ringvägen";

            Assert.assertEquals("Broken charset string isn't extrapolated", description, t1.getDescription());
            Assert.assertEquals("Short string is extrapolated incorrectly", description, t2.getDescription());
        });
    }
    
    @Test
    public void emptyDescriptionTest() {
        users.forEach(user -> {
            Transaction t1 = testUtil.getNewTransaction("test1", -99, "");
            commands.get(0).execute(t1);

            Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());
            Assert.assertEquals(catalog.getString("(missing description)"), t1.getDescription());
        });

    }

    @Test
    public void testAbnExtraction() {
        users.forEach(user -> {
            // Setup

            final Cluster cluster = Cluster.ABNAMRO;
            TransactionProcessorContext context = contexts.get(user.getId());
            final FormatDescriptionCommand command = new FormatDescriptionCommand(
                    context,
                    MarketDescriptionFormatterFactory.byCluster(cluster),
                    MarketDescriptionExtractorFactory.byCluster(cluster),
                    metricRegistry,
                    provider
            );
            User extrapolateTest = testUtil.getTestUsers("extrapolateTest").get(0);
            context = new TransactionProcessorContext(
                    extrapolateTest,
                    testUtil.getProvidersByName(),
                    Lists.newArrayList()
            );

            command.initialize();

            // Actual test

            Transaction transaction = testUtil.getNewTransaction("test1", -99, "SUBWAY EARLS,PAS660");
            command.execute(transaction);

            Assert.assertEquals("Subway Earls", transaction.getDescription());
        });
    }

}
