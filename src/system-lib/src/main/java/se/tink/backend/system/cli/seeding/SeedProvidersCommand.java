package se.tink.backend.system.cli.seeding;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.ProviderImageRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderImage;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class SeedProvidersCommand extends ServiceContextCommand<ServiceConfiguration> {
    public SeedProvidersCommand() {
        super("seed-providers", "Seed the database with the supported providers.");
    }

    protected static TreeMap<String, String> categoryMappings = new TreeMap<String, String>();
    protected static LogUtils log = new LogUtils(SeedProvidersCommand.class);
    protected static ObjectMapper mapper = new ObjectMapper();

    public static void seedProviders(ProviderRepository repository, boolean isDevelopment) throws Exception {
        repository.deleteAllInBatch();

        // Load the providers.

        log.info("Seeding providers");

        File directory = new File("data/seeding");
        File[] providerFiles = directory.listFiles((dir, fileName) -> fileName.matches("providers-[a-z]{2}.json"));

        if (providerFiles != null) {
            for (File providerFile : providerFiles) {
                seedProviders(repository, providerFile);
            }
        }

        if (isDevelopment) {
            File[] devSpecificProviders = directory.listFiles(
                    (dir, fileName) -> fileName.matches("providers-development.json"));

            if (devSpecificProviders == null || devSpecificProviders.length != 1) {
                log.warn("Tried to seed development specific providers but could not find file.");
                return;
            }

            seedProviders(repository, devSpecificProviders[0]);
        }
    }

    public static void seedProvidersImages(ProviderImageRepository repository) throws IOException {
        log.info("Seeding Provider Images...");

        repository.deleteAllInBatch();

        List<String> lines = Files.readLines(new File("data/seeding/provider-images.txt"), Charsets.UTF_8);

        List<ProviderImage> images = Lists.newArrayList();

        for (String line : lines) {
            String[] data = line.split("\t");
            ProviderImage image = new ProviderImage();
            image.setCode(data[0]);
            if ("NULL".equals(data[1])) {
                image.setUrl(null);
            } else {
                image.setUrl(data[1]);
            }

            images.add(image);
        }

        repository.save(images);
    }

    private static void seedProviders(ProviderRepository repository, File file)
            throws IOException {
        ProviderConfigModel providerConfig = mapper.readValue(file, ProviderConfigModel.class);

        String currency = providerConfig.getCurrency();
        String market = providerConfig.getMarket();
        List<Provider> providers = providerConfig.getProviders();

        for (Provider provider : providers) {
            if (market != null) {
                provider.setMarket(market);
            }

            if (currency != null) {
                provider.setCurrency(currency);
            }

            repository.save(provider);
        }

        if (market == null) {
            market = "DEVELOPMENT";
        }

        log.info("Seeded " + providers.size() + " providers for " + market);
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        try {
            seedProviders(serviceContext.getRepository(ProviderRepository.class), configuration.isDevelopmentMode());
            seedProvidersImages(serviceContext.getRepository(ProviderImageRepository.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
