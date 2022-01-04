package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class NickelAddress {
    private String address;
    private String address2;
    private String address3;
    private String address4;
    private String at;
    private String city;
    private String country;
    private String linearized;
    private String region;
    private String zipCode;
}
