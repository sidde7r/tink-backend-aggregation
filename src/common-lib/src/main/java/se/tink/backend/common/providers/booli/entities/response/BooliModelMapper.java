package se.tink.backend.common.providers.booli.entities.response;

import java.util.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableListMultimap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import se.tink.backend.common.application.PropertyUtils;
import se.tink.backend.common.providers.booli.entities.request.BooliEstimateRequest;
import se.tink.backend.common.providers.booli.entities.request.ResidenceType;
import se.tink.backend.core.BooliEstimate;
import se.tink.backend.core.BooliSoldProperty;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.libraries.date.DateUtils;

public class BooliModelMapper {
    private static final Pattern APARTMENT_NUMBER_IN_ADDRESS_PATTERN = Pattern
            .compile("lgh\\s?(?<apartmentNumber>\\d{4})$", Pattern.CASE_INSENSITIVE); // lgh 1234, where 1234 is group apartmentNumber
    private static final Pattern FLOOR_NUMBER_IN_ADDRESS_PATTERN = Pattern
            .compile("(?<floorNumber>\\d+)\\s?tr$", Pattern.CASE_INSENSITIVE); // 1 tr, where 1 is group floorNumber

    public static BooliSoldProperty referenceToSoldProperty(Reference reference) {
        BooliSoldProperty booliSoldProperty = new BooliSoldProperty();

        booliSoldProperty.setStreetAddress(reference.getStreetAddress());
        booliSoldProperty.setRooms(reference.getRooms());
        booliSoldProperty.setResidenceType(reference.getObjectType());
        booliSoldProperty.setBooliId(reference.getSoldId());
        booliSoldProperty.setFloor(reference.getFloor());
        booliSoldProperty.setBooliUrl(reference.getUrl());
        booliSoldProperty.setIndexAdjustedFloatPrice(reference.getIndexAdjustedSoldPrice());
        booliSoldProperty.setLatitude(reference.getLatitude());
        booliSoldProperty.setLongitude(reference.getLongitude());
        booliSoldProperty.setLivingArea(reference.getLivingArea());
        booliSoldProperty.setOperatingCost(reference.getOperatingCost());
        booliSoldProperty.setPlotArea(reference.getPlotArea());
        booliSoldProperty.setRent(reference.getRent());
        booliSoldProperty.setSoldDate(DateUtils.parseDate(reference.getSoldDate()));
        booliSoldProperty.setSoldPrice(reference.getSoldPrice());
        booliSoldProperty.setSoldSqmPrice(reference.getSoldSqmPrice());

        return booliSoldProperty;
    }

    public static BooliEstimate booliEstimateResponseToBooliEstimate(String propertyId, BooliEstimateResponse response) {
        BooliEstimate estimate = new BooliEstimate();

        estimate.setPropertyId(propertyId);

        estimate.setAccuracy(response.getAccuracy());
        estimate.setPrice(response.getPrice());
        estimate.setPriceRangeHigh(response.getPriceRangeHigh());
        estimate.setPriceRangeLow(response.getPriceRangeLow());
        estimate.setSqmPrice(response.getSqmPrice());
        estimate.setSqmPriceRangeHigh(response.getSqmPriceRangeHigh());
        estimate.setSqmPriceRangeLow(response.getSqmPriceRangeLow());
        estimate.setNumberOfReferences(response.getReferences().size());
        estimate.setAdditionalAndLivingArea(response.getResidence().getAdditionalAndLivingArea());
        estimate.setAdditionalArea(response.getResidence().getAdditionalArea());
        estimate.setApartmentNumber(response.getResidence().getApartmentNumber());
        estimate.setBalcony(response.getResidence().getBalcony());
        estimate.setBathroomCondition(response.getResidence().getBathroomCondition());
        estimate.setBuildingHasElevator(response.getResidence().getBuildingHasElevator());
        estimate.setCanParkCar(response.getResidence().getCanParkCar());
        estimate.setCeilingHeight(response.getResidence().getCeilingHeight());
        estimate.setConstructionEra(response.getResidence().getConstructionEra());
        estimate.setConstructionYear(response.getResidence().getConstructionYear());
        estimate.setFireplace(response.getResidence().getFireplace());
        estimate.setFloor(response.getResidence().getFloor());
        estimate.setHasBasement(response.getResidence().getHasBasement());
        estimate.setKitchenCondition(response.getResidence().getKitchenCondition());
        estimate.setKnowledge(response.getResidence().getKnowledge());
        estimate.setLastGroundDrainage(response.getResidence().getLastGroundDrainage());
        estimate.setLastRoofRenovation(response.getResidence().getLastRoofRenovation());
        estimate.setLatitude(response.getResidence().getLatitude());
        estimate.setLongitude(response.getResidence().getLongitude());
        estimate.setListPrice(response.getResidence().getListPrice());
        estimate.setLivingArea(response.getResidence().getLivingArea());
        estimate.setOperatingCost(response.getResidence().getOperatingCost());
        estimate.setOperatingCostPerSqm(response.getResidence().getOperatingCostPerSqm());
        estimate.setPatio(response.getResidence().getPatio());
        estimate.setPlotArea(response.getResidence().getPlotArea());
        estimate.setRent(response.getResidence().getRent());
        estimate.setRentPerSqm(response.getResidence().getRentPerSqm());
        estimate.setResidenceType(response.getResidence().getObjectType());
        estimate.setRooms(response.getResidence().getRooms());
        estimate.setStreetAddress(response.getResidence().getStreetAddress());
        estimate.setKnnPrediction(response.getMeta().getKnnPrediction());
        estimate.setBiddingAveragePrediction(response.getMeta().getBiddingAveragePrediction());
        estimate.setBiddingAverageWeight(response.getMeta().getBiddingAverageWeight());
        estimate.setDifferenceAverage(response.getMeta().getDifferenceAverage());
        estimate.setDifferenceCv(response.getMeta().getDifferenceCv());
        estimate.setKnnWeight(response.getMeta().getKnnWeight());
        estimate.setPredictionDate(DateUtils.parseDate(response.getMeta().getPredictionDate()));
        estimate.setPredictor(response.getMeta().getPredictor());
        estimate.setPreviousSalePrediction(response.getMeta().getPreviousSalePrediction());
        estimate.setPreviousSaleWeight(response.getMeta().getPreviousSaleWeight());
        estimate.setPriceCv(response.getMeta().getPriceCv());
        estimate.setRecommendation(response.getMeta().getRecommendation());

        return estimate;
    }

    public static BooliEstimateRequest applicationToBooliEstimateRequest(GenericApplication application)
            throws Exception {
        ImmutableListMultimap<String, GenericApplicationFieldGroup> groups = ApplicationUtils
                .getGroupsByName(application);
        GenericApplicationFieldGroup residence = ApplicationUtils
                .getFirst(groups, GenericApplicationFieldGroupNames.RESIDENCE).get();
        GenericApplicationFieldGroup property = ApplicationUtils
                .getFirst(groups, GenericApplicationFieldGroupNames.PROPERTY).get();

        ResidenceType residenceType = ResidenceType.fromApplicationOptionValue(
                residence.getField(ApplicationFieldName.VALUATION_RESIDENCE_TYPE));

        Optional<Double> latitude = property.tryGetFieldAsDouble(ApplicationFieldName.LATITUDE);
        Optional<Double> longitude = property.tryGetFieldAsDouble(ApplicationFieldName.LONGITUDE);

        Preconditions.checkState(latitude.isPresent(),
                "Latitude of property cannot be absent. Ensure that we got a coordinate from geocoding.");
        Preconditions.checkState(longitude.isPresent(),
                "Longitude of property cannot be absent. Ensure that we got a coordinate from geocoding.");

        BooliEstimateRequest request = new BooliEstimateRequest(latitude.get(), longitude.get(), residenceType,
                residence.getFieldAsInteger(ApplicationFieldName.LIVING_AREA).doubleValue());

        // Common fields

        request.setEstimateToDate(DateTime.now());
        String streetAddress = property.getField(ApplicationFieldName.STREET_ADDRESS);
        request.setStreetAddress(PropertyUtils.cleanStreetAddress(streetAddress));
        request.setOperatingCost(residence.getFieldAsDouble(ApplicationFieldName.VALUATION_MONTHLY_OPERATING_COST));
        request.setRent(residence.getFieldAsDouble(ApplicationFieldName.VALUATION_MONTHLY_HOUSING_COMMUNITY_FEE));
        request.setRooms(residence.getFieldAsDouble(ApplicationFieldName.NUMBER_OF_ROOMS));
        request.setAdditionalArea(residence.getFieldAsDouble(ApplicationFieldName.ADDITIONAL_AREA));
        request.setConstructionYear(residence.getFieldAsInteger(ApplicationFieldName.CONSTRUCTION_YEAR));
        request.setPlotArea(residence.getFieldAsDouble(ApplicationFieldName.PLOT_AREA));

        // Residence type specific fields

        switch (residenceType) {
        case APARTMENT:
            String apartmentNumber = getApartmentNumber(streetAddress, residence);
            request.setApartmentNumber(apartmentNumber);
            request.setFloor(getFloorFromApartmentNumberOrStreetAddress(apartmentNumber, streetAddress));
            break;
        default:
            break;
        }

        return request;
    }

    /**
     * Using fallback and parse from streetaddress if no apartment number is available on residence
     */
    private static String getApartmentNumber(String streetAddress, GenericApplicationFieldGroup residence) {
        String apartmentNumber = getApartmentNumber(residence);

        if (Strings.isNullOrEmpty(apartmentNumber)) {
            // Fallback: Parse apartment number from street address
            apartmentNumber = getApartmentNumber(streetAddress);
        }

        return apartmentNumber;
    }

    private static String getApartmentNumber(GenericApplicationFieldGroup residence) {
        String apartmentNumber = residence.getField(ApplicationFieldName.APARTMENT_NUMBER);

        if (apartmentNumber != null && apartmentNumber.length() == 4) {
            return apartmentNumber;
        }

        return null;
    }

    protected static String getApartmentNumber(String streetAddress) {
        if (Strings.isNullOrEmpty(streetAddress)) {
            return null;
        }

        Matcher apartmentNumberMatcher = APARTMENT_NUMBER_IN_ADDRESS_PATTERN.matcher(streetAddress);

        if (!apartmentNumberMatcher.find()) {
            return null;
        }

        return apartmentNumberMatcher.group("apartmentNumber");
    }

    private static Integer getFloorFromApartmentNumberOrStreetAddress(String apartmentNumber, String streetAddress) {
        Integer floor = getFloorFromApartmentNumber(apartmentNumber);

        if (floor == null) {
            // Fallback: Try get number of stairs from the address
            floor = getFloorFromStreetAddress(streetAddress);
        }

        return floor;
    }

    protected static Integer getFloorFromApartmentNumber(String apartmentNumber) {
        if (Strings.isNullOrEmpty(apartmentNumber) || apartmentNumber.length() != 4) {
            return null;
        }

        // SKVs apartment number is 4 digits, where first two digits constitute the floor number + 10.
        return Integer.valueOf(apartmentNumber.substring(0, 2)) - 10;
    }

    protected static Integer getFloorFromStreetAddress(String streetAddress) {
        if (Strings.isNullOrEmpty(streetAddress)) {
            return null;
        }

        Matcher floorNumberMatcher = FLOOR_NUMBER_IN_ADDRESS_PATTERN.matcher(streetAddress);

        if (!floorNumberMatcher.find()) {
            return null;
        }

        return Integer.valueOf(floorNumberMatcher.group("floorNumber"));
    }
}
