package se.tink.backend.system.cli.fraud;

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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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

public class ImportCreditsafeCompanyDataCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final CSVFormat FORMAT = CSVFormat.newFormat(',').withRecordSeparator('\n').withQuote('"').withNullString(null);

    private static final LogUtils log = new LogUtils(ImportCreditsafeCompanyDataCommand.class);

    public ImportCreditsafeCompanyDataCommand() {
        super("import-company-data", "Updates companies and replaces company engagements in db.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        CompanyRepository companyRepository = serviceContext.getRepository(CompanyRepository.class);
        CompanyEngagementRepository companyEngagementRepository = serviceContext
                .getRepository(CompanyEngagementRepository.class);

        final long companiesCountBefore = companyRepository.count();
        log.info("Count before updating companies: " + companiesCountBefore);
        
        // Add companies.

        int countCompanies = 0;
        String line;
        try (
                InputStream fis = new FileInputStream("data/seeding/companies.txt");
                InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr)) {
            
            while ((line = br.readLine()) != null) {

                CSVParser parser = CSVParser.parse(line, FORMAT);
                CSVRecord record = parser.getRecords().get(0);

                if (record.get(0).equals("ORGNUMBER")) {
                    continue;
                }

                countCompanies++;

                try {
                    Company company = new Company();

                    company.setOrgNumber(getString(record.get(0)));
                    company.setName(getString(record.get(1)));
                    company.setAddress(getString(record.get(3)));
                    company.setZipcode(getString(record.get(4)));
                    company.setPhone(getString(record.get(5)));
                    company.setEmail(getString(record.get(7)));
                    company.setUrl(getString(record.get(8)));
                    company.setCommunity(getString(record.get(9)));
                    company.setCounty(getString(record.get(10)));
                    company.setRegistered(getDate(ThreadSafeDateFormat.FORMATTER_DAILY, record.get(13)));
                    company.setFskattRegistered(getBoolean(record.get(14)));
                    company.setStatus(getString(record.get(19)));
                    company.setStatusUpdated(getDate(ThreadSafeDateFormat.FORMATTER_SECONDS,
                            record.get(21)));
                    company.setSnicode(getString(record.get(22)));
                    company.setTown(getString(record.get(24)));

                    companyRepository.save(company);

                    if (countCompanies % 10000 == 0) {
                        log.info("Added companies: " + countCompanies);
                    }

                } catch (Exception e) {
                    log.error("Could not add: " + line, e);
                }
            }
        }
        catch (Exception e) {
            log.error("Could not read data/seeding/companies.txt", e);
        }
        
        final long companiesCountAfter = companyRepository.count();

        log.info("Count parsed companies:          " + countCompanies);
        log.info("Count before updating companies: " + companiesCountBefore);
        log.info("Count after updating companies:  " + companiesCountAfter);

        // Add company engagements.

        final long companyEngagementsCountBefore = companyEngagementRepository.count();

        log.info("Count before updating company engagements: " + companyEngagementsCountBefore);
        
        companyEngagementRepository.truncate();

        int countEngagements = 0;

        try (
                InputStream fis = new FileInputStream("data/seeding/companies_engagements.txt");
                InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr)) {
            while ((line = br.readLine()) != null) {

                CSVParser parser = CSVParser.parse(line, FORMAT);
                CSVRecord record = parser.getRecords().get(0);

                if (record.get(0).equals("ORG_NUMMER")) {
                    continue;
                }
                
                countEngagements++;

                try {
                    CompanyEngagement engagement = parseEngagement(record);
                    companyEngagementRepository.save(engagement);

                    if (countEngagements % 10000 == 0) {
                        log.info("Added engagements: " + countEngagements);
                    }

                } catch (Exception e) {
                    log.error("Could not add: " + line, e);
                }
            }
        }
        catch (Exception e) {
            log.error("Could not read data/seeding/companies_engagements.txt", e);
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

                CSVParser parser = CSVParser.parse(line, FORMAT);
                CSVRecord record = parser.getRecords().get(0);

                if (record.get(0).equals("ORG_NUMMER")) {
                    continue;
                }

                count++;

                try {
                    CompanyEngagement engagement = parseEngagement(record);
                    calendar.setTime(engagement.getToDate());

                    if (calendar.get(Calendar.YEAR) >= 2015) {
                        companyEngagementRepository.save(engagement);
                        countEngagementsEnded++;
                    }

                    if (count % 10000 == 0) {
                        log.info("Added ended engagements: " + count);
                    }

                } catch (Exception e) {
                    log.error("Could not add: " + line, e);
                }
            }
        }
        catch (Exception e) {
            log.error("Could not read data/seeding/companies_engagements_ended.txt", e);
        }
        
        final long companyEngagementsEndedCountAfter = companyEngagementRepository.count();

        log.info("Count before updating company engagements: " + companyEngagementsCountBefore);
        log.info("Count after updating company engagements: " + companyEngagementsEndedCountAfter);

        log.info("-------------------- RESULTS --------------------");
        log.info("Parsed " + countCompanies + " companies.");
        log.info("Parsed " + countEngagements + " engagements.");
        log.info("Parsed " + countEngagementsEnded + " ended engagements.");
        log.info("-------------------------------------------------");
    }

    private CompanyEngagement parseEngagement(Iterable<String> data) throws ParseException {
        CompanyEngagement engagement = new CompanyEngagement();
        engagement.setOrgNumber(getString(Iterables.get(data, 0)));
        engagement.setPersonNumber(getString(Iterables.get(data, 2)));
        engagement.setRoleName(getString(Iterables.get(data, 9)));
        engagement.setFromDate(getDate(ThreadSafeDateFormat.FORMATTER_DAILY, Iterables.get(data, 10).trim()));
        engagement.setToDate(getDate(ThreadSafeDateFormat.FORMATTER_DAILY, Iterables.get(data, 11).trim()));
        return engagement;
    }

    private boolean getBoolean(String data) {
        String value = StringUtils.trimToNull(data);
        if (value == null) {
            return false;
        }

        if (value.equals("1")) {
            return true;
        }

        return false;
    }

    private Date getDate(ThreadSafeDateFormat formatter, String data) throws ParseException {
        String value = StringUtils.trimToNull(data);
        if (value == null) {
            return null;
        }

        return formatter.parse(value);
    }

    private String getString(String data) {
        String value = StringUtils.trimToNull(data);
        if (value == null) {
            return null;
        }
        return se.tink.backend.utils.StringUtils.formatHuman(value);
    }
}
