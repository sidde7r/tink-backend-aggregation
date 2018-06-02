package se.tink.backend.common.client;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.apache.curator.x.discovery.ServiceInstance;
import se.tink.backend.common.client.retry.RetryableWebResource;
import se.tink.backend.common.client.retry.RetryableWebResource.Candidate;
import se.tink.libraries.discovery.ServiceDiscoveryHelper.InstanceDetails;

public class InstanceToURLFunction implements Function<ServiceInstance<InstanceDetails>, Iterable<Candidate>> {

    @Override
    public Iterable<Candidate> apply(ServiceInstance<InstanceDetails> instance) {
        Builder<Candidate> builder = ImmutableList.builder();
        if (instance.getPort() != null) {
            builder.add(new RetryableWebResource.Candidate("http", instance.getAddress(), instance.getPort()));
        }
        // No "else if" here, since a service might be exposing _both_ http and https.
        if (instance.getSslPort() != null) {
            builder.add(new RetryableWebResource.Candidate("https", instance.getAddress(), instance.getSslPort()));
        }
        return builder.build();
    }

}
