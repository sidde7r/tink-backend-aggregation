package se.tink.backend.aggregation.agents.utils.berlingroup.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LinksEntity {

    // Custom deserialization makes it possible for this class to support two ways of supplying
    // "urls" in berlingroup standard:
    // without object       "_links": {"scaRedirect": "awesomeUrl"}
    // and with href object "_links": {"scaRedirect": {"href":"awesomeUrl"}}
    // The approach differs from bank to bank, but Agent implementation shouldn't really have to
    // worry, or care about which one is used by bank

    @JsonDeserialize(using = LinkDeserializer.class)
    private String startAuthorisationWithPsuAuthentication;

    @JsonDeserialize(using = LinkDeserializer.class)
    private String startAuthorisationWithEncryptedPsuAuthentication;

    @JsonDeserialize(using = LinkDeserializer.class)
    private String startAuthorisation;

    @JsonDeserialize(using = LinkDeserializer.class)
    private String scaOAuth;

    @JsonDeserialize(using = LinkDeserializer.class)
    private String scaRedirect;

    @JsonDeserialize(using = LinkDeserializer.class)
    private String scaStatus;

    @JsonDeserialize(using = LinkDeserializer.class)
    private String self;

    @JsonDeserialize(using = LinkDeserializer.class)
    private String status;

    @JsonDeserialize(using = LinkDeserializer.class)
    private String authoriseTransaction;

    @JsonDeserialize(using = LinkDeserializer.class)
    private String selectAuthenticationMethod;
}
