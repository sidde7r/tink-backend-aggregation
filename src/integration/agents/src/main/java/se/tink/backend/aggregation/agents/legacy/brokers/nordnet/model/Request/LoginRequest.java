package se.tink.backend.aggregation.agents.brokers.nordnet.model.Request;

import com.google.common.base.MoreObjects;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class LoginRequest extends MultivaluedMapImpl {

    public LoginRequest() {
        add("encryption", "1");
        add("fake_password", "");
        add("referer", "/now/mobile/2016.05.10-11.47.24/index.html");
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper builder = MoreObjects.toStringHelper(this);

        for (String key : this.keySet()) {
            builder.add(key, this.get(key));
        }

        return builder.toString();
    }
}
