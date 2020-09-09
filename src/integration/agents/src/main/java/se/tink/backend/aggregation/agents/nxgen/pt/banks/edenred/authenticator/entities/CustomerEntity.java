package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.authenticator.entities;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEntity {

    private long id;
    private long regVersion;
    private String name;
    private Date birthDate;
    private String cellPhoneNumber;
    private long workPostalCode;
    private long residencePostalCode;
    private String gender;
    private String emailStatus;
    private String workPlace;
    private String residencePlace;
    private String registerStatus;
    private double latCoordinateWork;
    private double lngCoordinateWork;
    private double latCoordinateResidence;
    private double lngCoordinateResidence;
    private String passwordStatus;
    private String email;
}
