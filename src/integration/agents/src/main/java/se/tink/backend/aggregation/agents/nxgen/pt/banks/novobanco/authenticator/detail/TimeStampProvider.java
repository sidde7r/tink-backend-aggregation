package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.detail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeStampProvider {
  private static final SimpleDateFormat SIMPLE_DATE_FORMAT =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

  static {
    SIMPLE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public static String getTimestamp() {
    return SIMPLE_DATE_FORMAT.format(new Date());
  }
}
