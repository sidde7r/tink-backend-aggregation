package se.tink.backend.main.controllers;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.common.repository.mysql.main.PropertyRepository;
import se.tink.backend.core.User;
import se.tink.backend.core.property.Property;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PropertyServiceControllerTest {
    @Test
    public void deleteValuation() throws Exception {
        PropertyRepository mockedPropertyRepository = mock(PropertyRepository.class);

        PropertyServiceController propertyServiceController = new PropertyServiceController(mockedPropertyRepository);

        Property property = new Property();
        property.setId("property-id");
        property.setMostRecentValuation(12345678);
        property.setBooliEstimateId("booli-estimate-id");

        User user = new User();
        user.setId("user-id");

        // Setup mock behavior
        when(mockedPropertyRepository.findByUserIdAndId(user.getId(), property.getId()))
                .thenReturn(property);

        when(mockedPropertyRepository.save(property)).thenReturn(property);

        // Delete
        Property updatedProperty = propertyServiceController.deleteValuation(user, property.getId());

        // Ensure returned property has no ref to booli estimate or valuation
        assertThat(updatedProperty.getMostRecentValuation()).isNull();
        assertThat(updatedProperty.getBooliEstimateId()).isNull();

        // Ensure db stored property has no ref to booli estimate or valuation
        ArgumentCaptor<Property> propertyArgumentCaptor = ArgumentCaptor.forClass(Property.class);
        verify(mockedPropertyRepository, times(1)).save(propertyArgumentCaptor.capture());
        assertThat(propertyArgumentCaptor.getValue().getBooliEstimateId()).isNull();
        assertThat(propertyArgumentCaptor.getValue().getMostRecentValuation()).isNull();
    }
}
