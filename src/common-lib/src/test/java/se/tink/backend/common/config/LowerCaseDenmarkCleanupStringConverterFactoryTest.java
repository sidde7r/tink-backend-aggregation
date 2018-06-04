package se.tink.backend.common.config;

import java.util.function.Function;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class LowerCaseDenmarkCleanupStringConverterFactoryTest {
    private static Function<String, String> preprocessor;

    @BeforeClass
    public static void initialize() {
        preprocessor = new LowerCaseDenmarkCleanupStringConverterFactory().build();
    }

    @Test
    public void kontaktlosDankort() {
        Assert.assertEquals("q8 service", preprocessor.apply("kontaktløs dankort q8 service - nota"));
    }

    @Test
    public void dankortNota() {
        Assert.assertEquals("rema", preprocessor.apply("dankort-nota rema"));
    }

    @Test
    public void mastercard() {
        Assert.assertEquals("bs jyske", preprocessor.apply("bs jyske mastercard"));
    }

    @Test
    public void visaDankort() {
        Assert.assertEquals("kontantgebyr notad", preprocessor.apply("visa/dankort kontantgebyr notad"));
    }

    @Test
    public void atm() {
        Assert.assertEquals("udl", preprocessor.apply("atm udbet visa udl"));
    }

    @Test
    public void dsb() {
        Assert.assertEquals("arhus aut", preprocessor.apply("dankort dsb århus aut nota"));
    }

    @Test
    public void mastercardAlternative() {
        Assert.assertEquals("debit haevning i udl", preprocessor.apply("1 mastercard debit hævning i udl"));
    }

    @Test
    public void dankortNotaZ() {
        Assert.assertEquals("burger king", preprocessor.apply("dankort burger king nota z"));
    }
}
