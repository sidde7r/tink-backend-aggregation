package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class PaymentProviderTest {

    // Dummy provider that doesn't add extend the payment provider class with any logic
    public class DomainNameProvider extends PaymentProvider {

        @Override
        protected Pattern getNamePattern() {
            return null;
        }

        @Override
        protected ImmutableList<Pattern> getDescriptionPatterns() {
            return ImmutableList.of();
        }
    }

    @Test
    public void testUrlDescriptionPatterns() throws Exception {

        // Payment provider is abstract so use dummy provider
        DomainNameProvider provider = new DomainNameProvider();

        // Non matched descriptions
        assertThat(provider.getDescription("sdf 123")).isNull();
        assertThat(provider.getDescription("a.Come")).isNull();
        assertThat(provider.getDescription("www .erik . nl")).isNull();
        assertThat(provider.getDescription("erik dot nl")).isNull();

        // Simple domain patterns
        assertThat(provider.getDescription("www.pay.nl")).isEqualTo("www.pay.nl");
        assertThat(provider.getDescription("pay.nl")).isEqualTo("pay.nl");
        assertThat(provider.getDescription("Some text www.pay.nl")).isEqualTo("www.pay.nl");
        assertThat(provider.getDescription("Some text pay.nl")).isEqualTo("pay.nl");
        assertThat(provider.getDescription("www.bestsellers.eu")).isEqualTo("www.bestsellers.eu");
        assertThat(provider.getDescription("www.sunet.se")).isEqualTo("www.sunet.se");

        // Check that we always pick the longest domain name if we have several matches. This is done since
        // we have cases where we have both "google.com" and "goog le.com"
        assertThat(provider.getDescription("ORD02267 005000228 5876370 Canvasscherm.nl Canvassc herm.nl"))
                .isEqualTo("Canvasscherm.nl");

        assertThat(provider.getDescription("ORD02267 short.nl Canvassc looooooong.nl")).isEqualTo("looooooong.nl");

        assertThat(provider.getDescription("ORD0226 looooooong.nl Canvassc short.nl")).isEqualTo("looooooong.nl");

        assertThat(provider.getDescription("OR67 www.short.nl Canva www.looooooong.nl")).isEqualTo("www.looooooong.nl");

        assertThat(provider.getDescription("ORD www.looooooong.nl Cansc www.short.nl")).isEqualTo("www.looooooong.nl");

        // Domain patterns that contain digits
        assertThat(provider.getDescription("www.sunet12.se")).isEqualTo("www.sunet12.se");
        assertThat(provider.getDescription("www.sun11et.se")).isEqualTo("www.sun11et.se");
        assertThat(provider.getDescription("94405542 Automatten4you.nl 12121")).isEqualTo("Automatten4you.nl");

        // Do not support dot inside the domain name since it is most likly missing a space before start
        assertThat(provider.getDescription("12122 Starthosting.nl000381Starthosting.nl")).isEqualTo("Starthosting.nl");

        // Should not match numbers in the beginning since it is most likely a sequence or order number
        assertThat(provider.getDescription("Foo bar 000043Weekendjeweg.nl")).isNull();

        // Remove any http:// or https:// in the beginning
        assertThat(provider.getDescription("http://www.eu-test.com, Foo ")).isEqualTo("www.eu-test.com");
        assertThat(provider.getDescription("https://www.eu-test.com, Bar ")).isEqualTo("www.eu-test.com");
        assertThat(provider.getDescription("https://www.eu-test.com, Bar ")).isEqualTo("www.eu-test.com");

    }

    // Dummy provider that is using the order pattern
    public class OrderPatternProvider extends PaymentProvider {

        @Override
        protected Pattern getNamePattern() {
            return null;
        }

        @Override
        protected ImmutableList<Pattern> getDescriptionPatterns() {
            return ImmutableList.of(ORDER_PATTERN);
        }
    }

    @Test
    public void testDescriptionPatterns() throws Exception {
        OrderPatternProvider provider = new OrderPatternProvider();

        // Non matched descriptions
        assertThat(provider.getDescription("123 description 345")).isNull();
        assertThat(provider.getDescription("123 123 345")).isNull();

        // Matched descriptions
        assertThat(provider.getDescription("121212 Order @ bar1")).isEqualTo("bar1");
        assertThat(provider.getDescription("121212 Order at bar2")).isEqualTo("bar2");

        assertThat(provider.getDescription("121212 Order 1212 @ bar1")).isEqualTo("bar1");
        assertThat(provider.getDescription("121212 Order 1212 @bar1")).isEqualTo("bar1");
        assertThat(provider.getDescription("121212 Order 1212 at bar2")).isEqualTo("bar2");

        assertThat(provider.getDescription("Order ORD00048 Vogue")).isEqualTo("Vogue");

        assertThat(provider.getDescription("Bestelling #100011111 @ GoToBags")).isEqualTo("GoToBags");
        assertThat(provider.getDescription("Bestelnummer: ET100111111 Etrias")).isEqualTo("Etrias");
        assertThat(provider.getDescription("Order #11111 Mijn")).isEqualTo("Mijn");
        assertThat(provider.getDescription("Order(s) #1111111 DJ Student")).isEqualTo("DJ Student");
        assertThat(provider.getDescription("Webshop Order De pspdokt")).isEqualTo("De pspdokt");
        assertThat(provider.getDescription("Webshop Order : Deass pspdokt")).isEqualTo("Deass pspdokt");
        assertThat(provider.getDescription("61014907 8308036 Bestelling #100078669 @ all4runn all4running"))
                .isEqualTo("all4runn all4running");

        assertThat(provider.getDescription("122111 Order 111111 LifeCity Baarn B.V"))
                .isEqualTo("LifeCity Baarn B.V");
    }
}
