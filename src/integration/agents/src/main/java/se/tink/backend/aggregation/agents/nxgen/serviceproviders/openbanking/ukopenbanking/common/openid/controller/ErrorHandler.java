package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.controller;

import java.util.Map;

interface ErrorHandler {
    void handle(Map<String, String> callbackData);
}
