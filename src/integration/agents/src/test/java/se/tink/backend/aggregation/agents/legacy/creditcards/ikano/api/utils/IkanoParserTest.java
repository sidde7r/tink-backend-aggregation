package se.tink.backend.aggregation.agents.creditcards.ikano.api.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import se.tink.libraries.social.security.time.SwedishTimeRule;

public class IkanoParserTest {
    private static final String DATE = "2016-03-17";

    @Rule public SwedishTimeRule timeRule = new SwedishTimeRule();

    @Test
    public void stringToDoubleTest() {
        Double transferOut = IkanoParser.stringToDouble("2050");
        Double transferIn = IkanoParser.stringToDouble("-2050");

        Assertions.assertThat(transferOut).isEqualTo(2050.00);
        Assertions.assertThat(transferIn).isEqualTo(-2050.00);
    }

    @Test
    public void stringToDateTest() throws ParseException {
        Date date = IkanoParser.stringToDate(DATE);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateAsString = formatter.format(date);

        Assertions.assertThat(dateAsString).isEqualTo(DATE);
    }
}
