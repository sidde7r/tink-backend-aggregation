package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator;

import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

class ErrorDiscover {

    static boolean isChannelNotPermitted(HttpResponseException ex) {
        return ex.getResponse().getStatus() == 403
                && ex.getResponse()
                        .getBody(String.class)
                        .contains("\"error\":\"channel_not_permitted\"");
    }
}
