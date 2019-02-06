package se.tink.backend.aggregation.agents.utils.authentication.encap2.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.HashMap;

public class RequestBody extends MultivaluedMapImpl {
    public RequestBody(HashMap<String, String> cryptoRequestParams) {
        add("EMD", cryptoRequestParams.get("EMD"));
        add("EMK", cryptoRequestParams.get("EMK"));
        add("MAC", cryptoRequestParams.get("MAC"));
        add("MPV", cryptoRequestParams.get("MPV"));
        add("PKH", cryptoRequestParams.get("PKH"));
    }
}
