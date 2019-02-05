package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.init;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapSerializer;

public class BankIdInitRequestBody {
    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String dob;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    @JsonProperty("mobile_number")
    private String mobileNumber;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String operation;

    public BankIdInitRequestBody(String dob, String mobileNumber, String operation) {
        this.dob = Preconditions.checkNotNull(dob);
        this.mobileNumber = Preconditions.checkNotNull(mobileNumber);
        this.operation = Preconditions.checkNotNull(operation);
    }

    public String getDob() {
        return dob;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
