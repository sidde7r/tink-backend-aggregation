package se.tink.backend.system.cli.extraction;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.io.Files;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.sourceforge.argparse4j.inf.Namespace;
import org.elasticsearch.common.primitives.Ints;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.utils.EsSearchUtils;
import se.tink.backend.core.KVPair;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class DetermineUserLocationCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(DetermineUserLocationCommand.class);

    public DetermineUserLocationCommand() {
        super("determine-user-location",
                "Finds city strings in user transactins and prints list with users and their city.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        final TransactionDao transactionRepository = serviceContext.getDao(TransactionDao.class);
        PostalCodeAreaRepository postalcodeAreaRepository = serviceContext
                .getRepository(PostalCodeAreaRepository.class);

        // build matcher

        final Map<String, List<String>> cities = Maps.newHashMap();

        for (String city : postalcodeAreaRepository.findAllCities()) {
            if (city.length() < 6) {
                continue;
            }
            List<String> modifiedCities = EsSearchUtils.addModifiedCities(city.toLowerCase(), 6);

            cities.put(city.toLowerCase(), modifiedCities);
        }

        File file = new File("data/admin/user-location.txt");
        final BufferedWriter writer = Files.newWriter(file, Charsets.UTF_8);

        int count = 0;

        final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        List<String> users = userRepository.findAllUserIds();

        log.info("Determine location for " + users.size() + " users");

        ExecutorService executor = Executors.newFixedThreadPool(20);

        for (final String userId : users) {

            final Splitter splitter = Splitter.on(" ").trimResults().omitEmptyStrings();
            count++;

            final int usersCount = count;
            executor.execute(() -> {
                try {

                    List<String> userCities = Lists.newArrayList();

                    List<Transaction> transList = transactionRepository.findAllByUserId(userId);
                    transactionLoop:
                    for (Transaction t : transList) {

                        Iterable<String> words = splitter.split(t.getOriginalDescription().toLowerCase());

                        for (Entry<String, List<String>> city : cities.entrySet()) {

                            for (String word : words) {
                                if (word.equals(city.getKey())) {
                                    userCities.add(city.getKey());
                                    continue transactionLoop;
                                }
                            }

                            for (String modifiedCity : city.getValue()) {
                                for (String word : words) {
                                    if (word.equals(modifiedCity)) {
                                        userCities.add(city.getKey());
                                        continue transactionLoop;
                                    }
                                }
                            }
                        }
                    }

                    ImmutableListMultimap<String, String> userCitiesMap = Multimaps.index(userCities,
                            s -> s);

                    List<KVPair<String, Integer>> topTwoCities = new ArrayList<KVPair<String, Integer>>();

                    for (String key : userCitiesMap.keySet()) {
                        Integer count1 = userCitiesMap.get(key).size();
                        if (topTwoCities.size() > 1) {
                            if (topTwoCities.get(1).getValue() < count1) {
                                topTwoCities.remove(1);
                                topTwoCities.add(1, new KVPair<String, Integer>(key, count1));
                            }
                        } else {
                            topTwoCities.add(new KVPair<String, Integer>(key, count1));
                        }
                        Collections.sort(topTwoCities, (o1, o2) -> -Ints.compare(o1.getValue(), o2.getValue()));
                    }

                    if (topTwoCities.size() < 2) {
                        return;
                    }

                    if (topTwoCities.get(0).getValue() < 3 * topTwoCities.get(1).getValue()) {
                        log.info("Not enough thresold: " + userId + "\t" + topTwoCities.get(0).getKey() + "\t"
                                + topTwoCities.get(0).getValue() + "\t" + topTwoCities.get(1).getKey() + "\t"
                                + topTwoCities.get(1).getValue());
                        return;
                    }

                    synchronized (writer) {
                        writer.write(userId);
                        writer.append("\t" + topTwoCities.get(0).getKey());
                        writer.append("\n");
                    }

                    log.info("\thave done " + usersCount + " users");
                } catch (Exception e) {

                }
            });
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Could not determin user location", e);
        }
        log.info("DONE!");
        writer.close();
    }
}
