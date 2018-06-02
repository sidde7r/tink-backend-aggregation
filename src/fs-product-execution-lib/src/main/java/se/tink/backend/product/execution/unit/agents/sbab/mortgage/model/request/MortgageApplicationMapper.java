package se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.request;

import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.product.execution.log.ProductExecutionLogger;
import se.tink.libraries.application.ApplicationFieldOptionValues;

public class MortgageApplicationMapper {
    private static final ProductExecutionLogger log = new ProductExecutionLogger(MortgageApplicationMapper.class);

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
            log.error(ProductExecutionLogger
                    .newBuilder().withMessage(String.format("Invalid employment status: %s.", value)));
            return Optional.empty();
        }
    }

    public static Optional<String> getCivilStatus(String value) {
        if (Strings.isNullOrEmpty(value)) {
            log.error(ProductExecutionLogger
                    .newBuilder().withMessage("Invalid civil status: null or empty."));
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
            log.error(ProductExecutionLogger
                    .newBuilder().withMessage(String.format("Invalid civil status: %s.", value)));
            return Optional.empty();
        }
    }

    public static Optional<String> getFormOfHousing(String value) {
        if (Strings.isNullOrEmpty(value)) {
            log.error(ProductExecutionLogger
                    .newBuilder().withMessage("Invalid form of housing: null or empty."));
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
            log.error(ProductExecutionLogger
                    .newBuilder().withMessage(String.format("Invalid form of housing: %s.", value)));
            return Optional.empty();
        }
    }

    public static Optional<PropertyType> getPropertyType(String type) {
        if (Strings.isNullOrEmpty(type)) {
            log.error(ProductExecutionLogger
                    .newBuilder().withMessage("Invalid property type: null or empty."));
            return Optional.empty();
        }

        switch (type) {
        case ApplicationFieldOptionValues.HOUSE:
            return Optional.of(PropertyType.VILLA);
        case ApplicationFieldOptionValues.VACATION_HOUSE:
            return Optional.of(PropertyType.FRITIDSHUS);
        default:
            log.error(ProductExecutionLogger
                    .newBuilder().withMessage(String.format("Invalid property type: %s.", type)));
            return Optional.empty();
        }
    }
}

