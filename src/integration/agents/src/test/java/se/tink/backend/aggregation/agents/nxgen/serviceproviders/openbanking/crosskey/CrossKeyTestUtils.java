package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyMarketConfiguration;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class CrossKeyTestUtils {
    public static final String FINANCIAL_ID = "financialId";
    public static final String BASE_API_URL = "baseApiURL";
    public static final String BASE_AUTH_URL = "baseAuthURL";
    public static final Date PAGING_TO =
            new GregorianCalendar(2020, Calendar.DECEMBER, 31).getTime();
    public static final Date PAGING_FROM = new GregorianCalendar(2020, Calendar.JUNE, 30).getTime();

    public static final String RESOURCES_FILE_DIR =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/crosskey/resources/";

    public static CrosskeyMarketConfiguration getCrossKeyMarketConfiguration() {
        return new CrosskeyMarketConfiguration(
                CrossKeyTestUtils.FINANCIAL_ID,
                CrossKeyTestUtils.BASE_API_URL,
                CrossKeyTestUtils.BASE_AUTH_URL);
    }

    public static <T> T loadResourceFileContent(String fileName, Class<T> returnClass) {
        return SerializationUtils.deserializeFromString(
                new File(RESOURCES_FILE_DIR + fileName), returnClass);
    }
}
