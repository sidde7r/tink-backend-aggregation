package se.tink.backend.system.product.mortgage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.Test;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudRealEstateEngagementContent;
import se.tink.backend.core.User;
import se.tink.backend.core.property.PropertyType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FraudDetailsPropertyTypeFinderTest {

    private static final DateTime NOW = new DateTime();

    @Test
    public void whenAddressWithoutLgh_returnsHouse() {
        // One fraud details, but address is null
        FraudDetailsRepository mockedFraudDetailsRepository = mockFraudDetailsRepositoryWithAddress("Östgötagatan");

        FraudDetailsPropertyTypeFinder propertyTypeFinder = new FraudDetailsPropertyTypeFinder(mockedFraudDetailsRepository);

        assertThat(propertyTypeFinder.findPropertyType(new User()).orElse(null))
                .isEqualTo(PropertyType.HOUSE);
    }

    @Test
    public void whenAddressContainsLgh_returnsApartment() {
        // One fraud details, but address is null
        FraudDetailsRepository mockedFraudDetailsRepository =
                mockFraudDetailsRepositoryWithAddress("Östgötagatan 4 lgh 3101");

        FraudDetailsPropertyTypeFinder propertyTypeFinder = new FraudDetailsPropertyTypeFinder(mockedFraudDetailsRepository);

        assertThat(propertyTypeFinder.findPropertyType(new User()).orElse(null))
                .isEqualTo(PropertyType.APARTMENT);
    }

    @Test
    public void whenMultipleAddressEntries_returnsMostRecent() {
        FraudDetailsRepository mockedFraudDetailsRepository =
                mockFraudDetailsRepositoryWithAddresses(ImmutableMap.of(
                        NOW.minusMonths(2), "Östgötagatan 4 lgh 3101",
                        NOW.minusMonths(1), "Barkarbyslingan", // <-- Most recent entry (HOUSE)
                        NOW.minusMonths(3), "Östgötagatan 4 lgh 2123"));

        FraudDetailsPropertyTypeFinder propertyTypeFinder = new FraudDetailsPropertyTypeFinder(mockedFraudDetailsRepository);

        assertThat(propertyTypeFinder.findPropertyType(new User()).orElse(null))
                .isEqualTo(PropertyType.HOUSE);
    }

    @Test
    public void whenNoMatchingProperty_returnsApartment() {
        FraudDetailsRepository mockedFraudDetailsRepository = mockFraudDetailsRepositoryWithAddressesAndEngagements(
                ImmutableMap.of(NOW.minusMonths(1), "Östgötagatan"), // Will statically be located in Stockholm, see below
                "Göteborg"); // Non-matching property

        FraudDetailsPropertyTypeFinder propertyTypeFinder = new FraudDetailsPropertyTypeFinder(mockedFraudDetailsRepository);

        assertThat(propertyTypeFinder.findPropertyType(new User()).orElse(null))
                .isEqualTo(PropertyType.APARTMENT);
    }

    @Test
    public void whenNoAddress_returnsAbsent() {
        // No fraud details
        FraudDetailsRepository mockedFraudDetailsRepository = mockFraudDetailsRepositoryWithDetails(
                Collections.<FraudDetails>emptyList());

        FraudDetailsPropertyTypeFinder propertyTypeFinder = new FraudDetailsPropertyTypeFinder(mockedFraudDetailsRepository);

        assertThat(propertyTypeFinder.findPropertyType(new User()).isPresent()).isFalse();
    }

    @Test
    public void whenAddressIsEmpty_returnsAbsent() {
        // One fraud details, but address is null
        FraudDetailsRepository mockedFraudDetailsRepository = mockFraudDetailsRepositoryWithAddress("");

        FraudDetailsPropertyTypeFinder propertyTypeFinder = new FraudDetailsPropertyTypeFinder(mockedFraudDetailsRepository);

        assertThat(propertyTypeFinder.findPropertyType(new User()).isPresent()).isFalse();
    }

    private FraudDetailsRepository mockFraudDetailsRepositoryWithAddress(String address) {
        return mockFraudDetailsRepositoryWithAddresses(ImmutableMap.of(NOW.minusMonths(1), address));
    }

    private FraudDetailsRepository mockFraudDetailsRepositoryWithAddresses(Map<DateTime, String> fraudAddressRecords) {
        return mockFraudDetailsRepositoryWithAddressesAndEngagements(fraudAddressRecords, "Stockholm", "Göteborg");
    }

    private FraudDetailsRepository mockFraudDetailsRepositoryWithAddressesAndEngagements(
            Map<DateTime, String> fraudAddressRecords,
            String... engagementMunicipalities) {
        ImmutableList.Builder<FraudDetails> fraudDetailsBuilder = ImmutableList.builder();

        for (Map.Entry<DateTime, String> addressEntry : fraudAddressRecords.entrySet()) {
            FraudAddressContent fraudAddressContent = new FraudAddressContent();
            fraudAddressContent.setAddress(addressEntry.getValue());
            fraudAddressContent.setCommunity("Stockholm");

            FraudDetails fraudDetails = new FraudDetails();
            fraudDetails.setDate(addressEntry.getKey().toDate());
            fraudDetails.setContent(fraudAddressContent);
            fraudDetails.setType(FraudDetailsContentType.ADDRESS);

            fraudDetailsBuilder.add(fraudDetails);
        }

        for (String engagementMunicipality : engagementMunicipalities) {
            FraudRealEstateEngagementContent realEstateEngagement = new FraudRealEstateEngagementContent();
            realEstateEngagement.setMuncipality(engagementMunicipality);

            FraudDetails realEstateEngagementDetails = new FraudDetails();
            realEstateEngagementDetails.setContent(realEstateEngagement);
            realEstateEngagementDetails.setType(FraudDetailsContentType.REAL_ESTATE_ENGAGEMENT);
            fraudDetailsBuilder.add(realEstateEngagementDetails);
        }

        return mockFraudDetailsRepositoryWithDetails(fraudDetailsBuilder.build());
    }

    private FraudDetailsRepository mockFraudDetailsRepositoryWithDetails(List<FraudDetails> fraudDetails) {
        FraudDetailsRepository mock = mock(FraudDetailsRepository.class);
        when(mock.findAllByUserId(any(String.class))).thenReturn(fraudDetails);
        return mock;
    }

}
