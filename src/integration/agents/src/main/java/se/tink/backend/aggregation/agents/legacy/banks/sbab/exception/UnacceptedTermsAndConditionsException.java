package se.tink.backend.aggregation.agents.banks.sbab.exception;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class UnacceptedTermsAndConditionsException extends RuntimeException {
    private String url;
    private MultivaluedMapImpl input;

    public UnacceptedTermsAndConditionsException(String url, MultivaluedMapImpl input) {
        super(
                "User has not accepted the Terms and Conditions needed for using the SBABs web site.");
        this.url = url;
        this.input = input;
    }

    public String getUrl() {
        return url;
    }

    public MultivaluedMapImpl getInput() {
        return input;
    }
}
