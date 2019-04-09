package se.tink.libraries.social.security.time;

import java.util.TimeZone;
import org.joda.time.DateTimeZone;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class SwedishTimeRule extends TestWatcher {

    private DateTimeZone jodaDefault = DateTimeZone.getDefault();
    private TimeZone jvmDefault = TimeZone.getDefault();
    private static final String TZ = "Europe/Stockholm";

    @Override
    protected void starting(Description description) {
        TimeZone.setDefault(TimeZone.getTimeZone(TZ));
        DateTimeZone.setDefault(DateTimeZone.forID(TZ));
    }

    @Override
    protected void finished(Description description) {
        TimeZone.setDefault(jvmDefault);
        DateTimeZone.setDefault(jodaDefault);
    }
}
