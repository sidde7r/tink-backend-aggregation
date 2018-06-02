package se.tink.backend.common.providers.booli.entities;

import com.google.common.base.Splitter;
import java.util.Map;
import org.junit.Test;
import se.tink.backend.common.providers.booli.entities.request.BalconyType;
import se.tink.backend.common.providers.booli.entities.request.BooliEstimateRequest;
import se.tink.backend.common.providers.booli.entities.request.MaintenanceIntervalType;
import se.tink.backend.common.providers.booli.entities.request.ParkingType;
import se.tink.backend.common.providers.booli.entities.request.ResidenceType;
import se.tink.backend.common.providers.booli.entities.request.SewerType;
import se.tink.backend.common.providers.booli.entities.request.VistaType;
import se.tink.backend.common.providers.booli.entities.request.WaterSupplyType;
import static org.assertj.core.api.Assertions.assertThat;

public class BooliEstimateRequestTest {
    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_ifLatitude_isAbsent() {
        new BooliEstimateRequest(null, 18.1234, ResidenceType.APARTMENT, 59.2);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_ifLongitude_isAbsent() {
        new BooliEstimateRequest(59.1234, null, ResidenceType.APARTMENT, 59.2);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_ifResidenceType_isAbsent() {
        new BooliEstimateRequest(59.1234, 18.1234, null, 59.2);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_ifLivingArea_isAbsent() {
        new BooliEstimateRequest(59.1234, 18.1234, ResidenceType.APARTMENT, null);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_ifKitchenCondition_isSmallerThan_one() {
        BooliEstimateRequest request = createRequest();
        request.setKitchenCondition(0);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_ifKitchenCondition_isHigherThan_five() {
        BooliEstimateRequest request = createRequest();
        request.setKitchenCondition(6);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_ifBathroomCondition_isSmallerThan_one() {
        BooliEstimateRequest request = createRequest();
        request.setKitchenCondition(0);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_ifBathroomCondition_isHigherThan_five() {
        BooliEstimateRequest request = createRequest();
        request.setBathroomCondition(6);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_ifResidenceType_isNot_apartment_and_apartmentNumber_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.HOUSE);
        request.setApartmentNumber("1234");
        request.toQueryParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrow_ifResidenceType_isNot_apartment_and_floor_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.HOUSE);
        request.setFloor(2);
        request.toQueryParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrow_ifResidenceType_isNot_apartment_and_ceilingHeight_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.HOUSE);
        request.setCeilingHeight(2.40);
        request.toQueryParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrow_ifResidenceType_isNot_apartment_and_elevator_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.HOUSE);
        request.setElevator(false);
        request.toQueryParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrow_ifResidenceType_isNot_apartment_and_balcony_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.HOUSE);
        request.setBalcony(BalconyType.TRADITIONAL);
        request.toQueryParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrow_ifResidenceType_isNot_house_and_lastGroundDrainage_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.APARTMENT);
        request.setLastGroundDrainage(MaintenanceIntervalType.LONG_TIME_AGO);
        request.toQueryParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrow_ifResidenceType_isNot_house_and_lastRoofRenovation_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.APARTMENT);
        request.setLastRoofRenovation(MaintenanceIntervalType.LONG_TIME_AGO);
        request.toQueryParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrow_ifResidenceType_is_apartment_and_patio_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.APARTMENT);
        request.setPatio(VistaType.NONE);
        request.toQueryParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrow_ifResidenceType_is_apartment_and_basement_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.APARTMENT);
        request.setBasement(false);
        request.toQueryParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrow_ifResidenceType_is_apartment_and_parking_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.APARTMENT);
        request.setParking(ParkingType.STREET);
        request.toQueryParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrow_ifResidenceType_is_apartment_and_assessmentValue_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.APARTMENT);
        request.setAssessmentValue(2.0);
        request.toQueryParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrow_ifResidenceType_is_apartment_and_assessmentYear_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.APARTMENT);
        request.setAssessmentYear(1989.0);
        request.toQueryParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrow_ifResidenceType_is_apartment_and_assessmentPoints_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.APARTMENT);
        request.setAssessmentPoints(2.0);
        request.toQueryParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrow_ifResidenceType_is_apartment_and_waterSupplyType_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.APARTMENT);
        request.setWaterSupplyType(WaterSupplyType.MUNICIPAL);
        request.toQueryParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrow_ifResidenceType_is_apartment_and_sewerType_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.APARTMENT);
        request.setSewerType(SewerType.MUNICIPAL);
        request.toQueryParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrow_ifResidenceType_is_apartment_and_ownShore_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.APARTMENT);
        request.setOwnShore(false);
        request.toQueryParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrow_ifResidenceType_is_apartment_and_distanceToWater_isPresent() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.APARTMENT);
        request.setDistanceToWater(4.0);
        request.toQueryParameters();
    }

    @Test
    public void ensureValidationPasses_ifKitchenCondition_isBetween_one_and_five() {
        BooliEstimateRequest request = createRequest();
        request.setKitchenCondition(5);
        request.toQueryParameters();

        request.setKitchenCondition(1);
        request.toQueryParameters();

        request.setKitchenCondition(3);
        request.toQueryParameters();
    }

    @Test
    public void ensureValidationPasses_ifBathroomCondition_isBetween_one_and_five() {
        BooliEstimateRequest request = createRequest();
        request.setBathroomCondition(5);
        request.toQueryParameters();

        request.setBathroomCondition(1);
        request.toQueryParameters();

        request.setBathroomCondition(3);
        request.toQueryParameters();
    }

    @Test
    public void ensureURI_isFormattedCorrectly() {
        BooliEstimateRequest request = createRequest();
        request.setResidenceType(ResidenceType.HOUSE);
        request.setOwnShore(true);

        String uri = request.toQueryParameters();

        Splitter.MapSplitter querySplitter = Splitter.on("&").withKeyValueSeparator("=");
        Map<String, String> keyValues = querySplitter.split(uri);

        assertThat(keyValues.get("location.position.latitude")).isEqualTo("59.1234");
        assertThat(keyValues.get("location.position.longitude")).isEqualTo("18.1234");
        assertThat(keyValues.get("objectType")).isEqualTo("Villa");
        assertThat(keyValues.get("livingArea")).isEqualTo("59.2");
        assertThat(keyValues.get("ownShore")).isEqualTo("1");
    }

    private static BooliEstimateRequest createRequest() {
        return new BooliEstimateRequest(59.1234, 18.1234, ResidenceType.APARTMENT, 59.2);
    }
}
