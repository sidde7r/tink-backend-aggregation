package se.tink.libraries.jollyday;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;
import de.jollyday.config.Configuration;
import de.jollyday.configuration.ConfigurationProviderManager;
import de.jollyday.impl.XMLManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Set;
import org.joda.time.ReadableInterval;
import se.tink.libraries.jollyday.holidays.HolidayConfigurationManager;

@SuppressWarnings("all")
public class TinkHolidayManager {

    private static ConfigurationProviderManager configurationProviderManager =
            new ConfigurationProviderManager();
    private final HolidayManager manager = new XMLManager();

    public TinkHolidayManager(CountryCode countryCode) {

        try {
            Method setPropertiesMethod =
                    HolidayManager.class.getDeclaredMethod("setProperties", Properties.class);
            setPropertiesMethod.setAccessible(true);
            setPropertiesMethod.invoke(
                    this.manager, configurationProviderManager.getConfigurationProperties(null));

            Configuration configuration =
                    HolidayConfigurationManager.getInstance().getConfiguration(countryCode);
            Field configField = XMLManager.class.getDeclaredField("configuration");
            configField.setAccessible(true);
            configField.set(this.manager, configuration);

            Method validateConfigurationHierarchy =
                    XMLManager.class.getDeclaredMethod(
                            "validateConfigurationHierarchy", Configuration.class);

            validateConfigurationHierarchy.setAccessible(true);

            validateConfigurationHierarchy.invoke(this.manager, configuration);

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Set<Holiday> getHolidays(int year, String... args) {
        return this.manager.getHolidays(year, args);
    }

    public Set<Holiday> getHolidays(ReadableInterval interval, String... args) {
        return this.manager.getHolidays(interval, args);
    }
}
