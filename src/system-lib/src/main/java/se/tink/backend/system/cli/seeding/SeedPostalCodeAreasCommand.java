package se.tink.backend.system.cli.seeding;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;
import se.tink.backend.core.PostalCodeArea;
import se.tink.backend.system.cli.ServiceContextCommand;

public class SeedPostalCodeAreasCommand extends ServiceContextCommand<ServiceConfiguration> {

    public SeedPostalCodeAreasCommand() {
        super("seed-postal-code-areas",
                "Seeds database from file or writes to file from database. Flag reverse=true writes from DB to file.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        boolean reverse = Boolean.getBoolean("reverse");

        if(reverse) {
            fromDbToFile(serviceContext);
        } else {
            fromFileToDb(serviceContext);
        }
    }

    private void fromFileToDb(ServiceContext serviceContext) {
        DatabaseSeeder seeder = DatabaseSeeder.getInstance(serviceContext);
        seeder.seedGeography();
    }

    private void fromDbToFile(ServiceContext serviceContext) throws Exception {

        PostalCodeAreaRepository postalCodeAreaRepository = serviceContext.getRepository(PostalCodeAreaRepository.class);
        
        List<PostalCodeArea> areas = postalCodeAreaRepository.findAll();

        File output = new File("data/seeding/postal-codes-se-coords.txt");
        if (output.exists()) {
            output.delete();
        }

        StringBuilder sb = new StringBuilder();
        for(PostalCodeArea area : areas) {
            if (!"Sweden".equals(area.getCountry())) {
                continue;
            }

            sb.append(area.getPostalCode()).append('\t');
            sb.append(area.getCity().toUpperCase()).append('\t');
            sb.append(area.getPopulation()).append('\t');

            if (area.getLatitude() != null && area.getLongitude() != null) {
                sb.append(area.getLatitude()).append('\t');
                sb.append(area.getLongitude());
            } else {
                sb.append('\t');
            }
            sb.append('\n');
        }

        Files.write(sb.toString(), output, Charsets.UTF_16);
    }
}
