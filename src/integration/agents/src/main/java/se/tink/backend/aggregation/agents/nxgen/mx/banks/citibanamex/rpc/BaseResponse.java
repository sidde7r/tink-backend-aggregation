package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BaseResponse {
    private String faultcode;
    protected String faultstring = "";
    private int resultCode;
    private int opstatus;
    protected String errmsg = "";
    private int httpStatusCode;
}
