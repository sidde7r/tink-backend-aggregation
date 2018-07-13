package se.tink.backend.aggregation.agents.banks.sbab.model.request;

import java.util.Optional;
import com.google.common.base.Strings;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.aggregation.log.AggregationLogger;

public class MortgageApplicationMapper {

    private static final AggregationLogger log = new AggregationLogger(MortgageApplicationMapper.class);

    public static Optional<EmploymentStatus> getEmploymentStatus(String value) {
        if (Strings.isNullOrEmpty(value)) {
            log.error("Invalid employment status: null or empty.");
            return Optional.empty();
        }
        
        switch (value) {
        case ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT:
            return Optional.of(EmploymentStatus.TILLSVIDARE);
        case ApplicationFieldOptionValues.TEMPORARY_EMPLOYMENT:
        case ApplicationFieldOptionValues.OTHER_OCCUPATION:
            return Optional.of(EmploymentStatus.VISSTIDS);
        case ApplicationFieldOptionValues.UNEMPLOYED:
            return Optional.of(EmploymentStatus.ARBETSLOS);
        case ApplicationFieldOptionValues.STUDENT_RESEARCHER:
            return Optional.of(EmploymentStatus.STUDERANDE);
        case ApplicationFieldOptionValues.SENIOR:
            return Optional.of(EmploymentStatus.PENSIONAR);
        case ApplicationFieldOptionValues.SELF_EMPLOYED:
            return Optional.of(EmploymentStatus.EGEN_FORETAGARE);
        default:
            log.error(String.format("Invalid employment status: %s.", value));
            return Optional.empty();
        }
    }

    public static Optional<String> getCivilStatus(String value) {
        if (Strings.isNullOrEmpty(value)) {
            log.error("Invalid civil status: null or empty.");
            return Optional.empty();
        }
        
        switch (value) {
        case ApplicationFieldOptionValues.SINGLE:
            return Optional.of(CivilStatus.ENSAMSTAENDE.toString());
        case ApplicationFieldOptionValues.MARRIED:
            return Optional.of(CivilStatus.GIFT.toString());
        case ApplicationFieldOptionValues.COHABITANT:
            return Optional.of(CivilStatus.SAMBO.toString());
        default:
            log.error(String.format("Invalid civil status: %s.", value));
            return Optional.empty();
        }
    }

    public static Optional<String> getFormOfHousing(String value) {
        if (Strings.isNullOrEmpty(value)) {
            log.error("Invalid form of housing: null or empty.");
            return Optional.empty();
        }
        
        switch (value) {
        case ApplicationFieldOptionValues.APARTMENT:
            return Optional.of(FormOfHousing.BRF.toString());
        case ApplicationFieldOptionValues.HOUSE:
            return Optional.of(FormOfHousing.VILLA.toString());
        case ApplicationFieldOptionValues.TENANCY:
            return Optional.of(FormOfHousing.HYRESRATT.toString());
        default:            
            log.error(String.format("Invalid form of housing: %s.", value));
            return Optional.empty();
        }
    }

    public static Optional<PropertyType> getPropertyType(String type) {
        if (Strings.isNullOrEmpty(type)) {
            log.error("Invalid property type: null or empty.");
            return Optional.empty();
        }
        
        switch (type) {
        case ApplicationFieldOptionValues.HOUSE:
            return Optional.of(PropertyType.VILLA);
        case ApplicationFieldOptionValues.VACATION_HOUSE:
            return Optional.of(PropertyType.FRITIDSHUS);
        default:
            log.error(String.format("Invalid property type: %s.", type));
            return Optional.empty();
        }
    }
}
