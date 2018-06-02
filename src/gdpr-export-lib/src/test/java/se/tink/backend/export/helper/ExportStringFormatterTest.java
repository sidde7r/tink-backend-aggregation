package se.tink.backend.export.helper;

import java.util.Objects;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ExportStringFormatterTest {

    private final static String emptyString = "";
    private final static String a = "a";
    private final static String A = "A";
    private final static String unformattedString = "some unformatTED STRing_with spaces_and_underscores";
    private final static String formattedString = "Some Unformatted String With Space And Underscores";

    @Test
    public void setUp() {

    }

    @Test
    public void testStringFormat() {
        Assertions.assertThat(Objects.equals(emptyString, ExportStringFormatter.format(emptyString)));
        Assertions.assertThat(Objects.equals(A, ExportStringFormatter.format(a)));
        Assertions.assertThat(Objects.equals(formattedString, ExportStringFormatter.format(unformattedString)));
    }

}
