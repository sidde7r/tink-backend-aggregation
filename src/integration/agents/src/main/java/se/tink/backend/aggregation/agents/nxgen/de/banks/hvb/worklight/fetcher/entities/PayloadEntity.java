package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.codehaus.jackson.annotate.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PayloadEntity {
    @JsonProperty("H")
    private String H;

    private String a;
    private String b;
    private String c;
    private String z;

    @JsonProperty("H")
    public void setH(String h) {
        this.H = h;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getA() {
        return a;
    }

    public void setB(String b) {
        this.b = b;
    }

    public String getB() {
        return b;
    }

    public void setC(String c) {
        this.c = c;
    }

    public String getC() {
        return c;
    }

    public void setZ(String z) {
        this.z = z;
    }

    public String getZ() {
        return z;
    }
}
