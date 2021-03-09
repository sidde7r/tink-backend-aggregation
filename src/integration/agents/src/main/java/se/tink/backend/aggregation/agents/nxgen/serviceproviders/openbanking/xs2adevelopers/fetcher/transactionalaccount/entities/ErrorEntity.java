package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@NoArgsConstructor
public class ErrorEntity {
    private static final String ERROR = "ERROR";
    public static final ErrorEntity CONSENT_INVALID =
            new ErrorEntity(
                    ERROR,
                    "CONSENT_INVALID",
                    "Access right to transactions older than 90 days has expired");
    public static final ErrorEntity CONSENT_TIME_OUT_EXPIRED =
            new ErrorEntity(ERROR, "CONSENT_TIME_OUT_EXPIRED", "You can not access transactions.");

    public static final ErrorEntity PARAMETER_NOT_CONSISTENT =
            new ErrorEntity(ERROR, "PARAMETER_NOT_CONSISTENT", "requested page does not exist");

    public static final ErrorEntity SERVICE_UNAVAILABLE =
            new ErrorEntity(ERROR, "SERVICE_UNAVAILABLE", "The service is currently unavailable");

    private String category;
    private String code;
    @EqualsAndHashCode.Exclude private String text;
}
