package se.tink.backend.system.cli.seeding;

import org.junit.Assert;
import org.junit.Test;

public class SeedCoordinatesForAllUsersCommandTest {


    @Test
    public void testCleanAddress() {
        SeedCoordinatesForAllUsersCommand sut = new SeedCoordinatesForAllUsersCommand();

        Assert.assertEquals("Sågargatan 6 C", sut.cleanAddress("Sågargatan 6 C lgh 1101"));
        Assert.assertEquals("Onsjögatan 8 F", sut.cleanAddress("Onsjögatan 8 F"));
        Assert.assertEquals("Ämbetsgatan 1", sut.cleanAddress("Ämbetsgatan 1 lgh 1201"));
        Assert.assertEquals("Dag Hammarskjölds Väg 4 F", sut.cleanAddress("Dag Hammarskjölds Väg 4 F lgh 1107"));
        Assert.assertEquals("Backsippsgatan 21 A", sut.cleanAddress("Backsippsgatan 21 A lgh 1202"));
        Assert.assertEquals("Kungsgatan 4", sut.cleanAddress("Kungsgatan 4 lgh 1205"));
        Assert.assertEquals("Kengisgatan 34 D", sut.cleanAddress("Kengisgatan 34 D lgh 1201"));
        Assert.assertEquals("Vindåkravägen 7", sut.cleanAddress("Vindåkravägen 7"));
        Assert.assertEquals("Farstavägen 91", sut.cleanAddress("Farstavägen 91 lgh 1903"));
        Assert.assertEquals("Gråsdalsvägen 1", sut.cleanAddress("Gråsdalsvägen 1"));
        Assert.assertEquals("something-random-with-lgh-mid-sentence 1", sut.cleanAddress("something-random-with-lgh-mid-sentence 1 lgh 1431"));
    }

}
