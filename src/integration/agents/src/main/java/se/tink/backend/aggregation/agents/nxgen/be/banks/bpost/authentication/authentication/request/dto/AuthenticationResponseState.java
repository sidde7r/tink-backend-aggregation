package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto;

public interface AuthenticationResponseState {

    boolean isError();

    String getState();
}
