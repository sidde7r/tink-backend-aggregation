package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerEntity {

    @JsonProperty("exception")
    private boolean exception;

    @JsonProperty("epostadress")
    private String email;

    @JsonProperty("meddelandesatt")
    private String typeOfMessage;

    @JsonProperty("mobilnummer")
    private String phoneNumber;

    @JsonProperty("ekundsStatus")
    private String customerStatus;

    @JsonProperty("csnNummer")
    private int csnNumber;

    @JsonProperty("event")
    private Object event;

    @JsonProperty("dtoexception")
    private Object dtoexception;

    @JsonProperty("csnException")
    private Object csnException;

    @JsonProperty("transactionId")
    private Object transactionId;
}
