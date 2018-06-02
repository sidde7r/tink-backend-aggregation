package se.tink.backend.common.providers.booli.entities.response;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class BooliModelMapperTest {
    @Test
    @Parameters({
        "Storgatan 1A LGH 1203, 1203",
        "Storgatan 1A Lgh 1203, 1203",
        "Storgatan 1A lgh 1203, 1203"
    })
    public void apartmentNumberFromStreetAddressCaseInsensitive(String streetAddress, String apartmentNumber) {
        assertThat(BooliModelMapper.getApartmentNumber(streetAddress)).isEqualTo(apartmentNumber);
    }

    @Test
    @Parameters({
        "Storgatan 1A",
        "Storgatan 1A 2tr",
        "Storgatan 1A lgh 1203 ABC"
    })
    public void noApartmentNumberInStreetAddress(String streetAddress) {
        assertThat(BooliModelMapper.getApartmentNumber(streetAddress)).isNull();
    }

    @Test
    @Parameters({
        "0901, -1", // Basement
        "1001, 0", // Entrance level
        "1301, 3" // "Fourth floor"
    })
    public void floorNumberFromApartmentNumber(String apartmentNumber, int floor) {
        assertThat(BooliModelMapper.getFloorFromApartmentNumber(apartmentNumber)).isEqualTo(floor);
    }

    @Test
    @Parameters({
        "11001",
        "130"
    })
    public void noFloorNumberFromIncorrectApartmentNumber(String apartmentNumber) {
        assertThat(BooliModelMapper.getFloorFromApartmentNumber(apartmentNumber)).isNull();
    }

    @Test
    @Parameters({
        "Storgatan 1A 2tr, 2",
        "Storgatan 1A 2 tr, 2"
    })
    public void floorNumberFromStreetAddress(String streetAddress, int floor) {
        assertThat(BooliModelMapper.getFloorFromStreetAddress(streetAddress)).isEqualTo(floor);
    }

    @Test
    @Parameters({
        "Storgatan 1A lgh 1203",
        "Storgatan 1A BV",
        "Storgatan 1A 2tr ABC"
    })
    public void noFloorNumberFromStreetAddress(String streetAddress) {
        assertThat(BooliModelMapper.getFloorFromStreetAddress(streetAddress)).isNull();
    }
}
