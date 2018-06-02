package se.tink.backend.system.cli.fraud;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.CompanyEngagementRepository;
import se.tink.backend.common.repository.mysql.main.CompanyRepository;
import se.tink.backend.core.Company;
import se.tink.backend.core.CompanyEngagement;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class FraudDataMigrationCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(FraudDataMigrationCommand.class);
    private Splitter splitter = Splitter.on(",\"").trimResults().omitEmptyStrings();

    public FraudDataMigrationCommand() {
        super("fraud-data-migration", "Generic command to update fraud data items in db.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        CompanyRepository companyRepository = serviceContext.getRepository(CompanyRepository.class);
        CompanyEngagementRepository companyEngagementRepository = serviceContext
                .getRepository(CompanyEngagementRepository.class);

        // Add companies.

        int countCompanies = 0;
        String line;
        try (
                InputStream fis = new FileInputStream("data/seeding/companies.txt");
                InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr)) {
            while ((line = br.readLine()) != null) {

                Iterable<String> data = splitter.split(line);

                if (Iterables.get(data, 0).startsWith("#")) {
                    continue;
                }

                countCompanies++;

                try {
                    Company company = new Company();

                    company.setOrgNumber(getString(Iterables.get(data, 1)));
                    company.setName(getString(Iterables.get(data, 2)));
                    company.setAddress(getString(Iterables.get(data, 4)));
                    company.setZipcode(getString(Iterables.get(data, 5)));
                    company.setPhone(getString(Iterables.get(data, 6)));
                    company.setEmail(getString(Iterables.get(data, 8)));
                    company.setUrl(getString(Iterables.get(data, 9)));
                    company.setCommunity(getString(Iterables.get(data, 10)));
                    company.setCounty(getString(Iterables.get(data, 11)));
                    company.setRegistered(getDate(ThreadSafeDateFormat.FORMATTER_DAILY, Iterables.get(data, 14)));
                    company.setFskattRegistered(getBoolean(Iterables.get(data, 15)));
                    company.setStatus(getString(Iterables.get(data, 21)));
                    company.setStatusUpdated(getDate(ThreadSafeDateFormat.FORMATTER_INTEGER_DATE, Iterables.get(data, 22)));
                    company.setSnicode(getString(Iterables.get(data, 23)));
                    company.setTown(getString(Iterables.get(data, 25)));

                    companyRepository.save(company);

                    if (countCompanies % 10000 == 0) {
                        log.info("Added companies: " + countCompanies);
                    }
                    
                } catch (Exception e) {
                    log.error("Could not add: " + data, e);
                }
            }
        }

        log.info("DONE adding " + countCompanies + " companies.");

        // Add company engagements.

        int countEngagements = 0;
        
        try (
                InputStream fis = new FileInputStream("data/seeding/companies_engagements.txt");
                InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr)) {
            while ((line = br.readLine()) != null) {

                Iterable<String> data = splitter.split(line);

                if (Iterables.get(data, 0).startsWith("#")) {
                    continue;
                }

                countEngagements++;

                try {
                    CompanyEngagement engagement = new CompanyEngagement();
                    engagement.setOrgNumber(getString(Iterables.get(data, 0)));
                    engagement.setPersonNumber(getString(Iterables.get(data, 2)));
                    engagement.setRoleName(getString(Iterables.get(data, 9)));
                    engagement.setFromDate(getDate(ThreadSafeDateFormat.FORMATTER_DAILY, Iterables.get(data, 10).trim()));
                    engagement.setToDate(getDate(ThreadSafeDateFormat.FORMATTER_DAILY, Iterables.get(data, 11).trim()));

                    companyEngagementRepository.save(engagement);

                    if (countEngagements % 10000 == 0) {
                        log.info("Added engagements: " + countEngagements);
                    }
                    
                } catch (Exception e) {
                    log.error("Could not add: " + data, e);
                }
            }
        }

        log.info("DONE adding " + countEngagements + " engagements.");
        
        // Add ended company engagements.
        // Only get ended engagements from 2015 and on.
        
        Calendar calendar = Calendar.getInstance();

        int countEngagementsEnded = 0;
        int count = 0;
        
        try (
                InputStream fis = new FileInputStream("data/seeding/companies_engagements_ended.txt");
                InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr)) {
            while ((line = br.readLine()) != null) {

                Iterable<String> data = splitter.split(line);

                if (Iterables.get(data, 0).startsWith("#")) {
                    continue;
                }
                
                count++;

                try {
                    CompanyEngagement engagement = new CompanyEngagement();
                    engagement.setOrgNumber(getString(Iterables.get(data, 0)));
                    engagement.setPersonNumber(getString(Iterables.get(data, 2)));
                    engagement.setRoleName(getString(Iterables.get(data, 9)));
                    engagement.setFromDate(getDate(ThreadSafeDateFormat.FORMATTER_DAILY, Iterables.get(data, 10).trim()));
                    engagement.setToDate(getDate(ThreadSafeDateFormat.FORMATTER_DAILY, Iterables.get(data, 11).trim()));

                    calendar.setTime(engagement.getToDate());
                    
                    if (calendar.get(Calendar.YEAR) >= 2015) {
                        companyEngagementRepository.save(engagement);
                        countEngagementsEnded++;
                    }

                    if (count % 10000 == 0) {
                        log.info("Added ended engagements: " + count);
                    }
                    
                } catch (Exception e) {
                    log.error("Could not add: " + data, e);
                }
            }
        }

        log.info("-------------------- RESULTS --------------------");
        log.info("Added " + countCompanies + " companies.");
        log.info("Added " + countEngagements + " engagements.");
        log.info("Added " + countEngagementsEnded + " ended engagements.");
        log.info("-------------------------------------------------");
    }

    private boolean getBoolean(String data) {
        String value = StringUtils.trimToNull(data.replace("\"", "").trim());
        if (value == null) {
            return false;
        }

        if (value.equals("1")) {
            return true;
        }

        return false;
    }

    private Date getDate(ThreadSafeDateFormat formatter, String data) throws ParseException {
        String value = StringUtils.trimToNull(data.replace("\"", ""));
        if (value == null) {
            return null;
        }

        return formatter.parse(value);
    }

    private String getString(String data) {
        String value = StringUtils.trimToNull(data.replace("\"", ""));
        if (value == null) {
            return null;
        }
        return se.tink.backend.utils.StringUtils.formatHuman(value);
    }
}
