package se.tink.libraries.discovered;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.apache.curator.x.discovery.ServiceInstance;
import se.tink.libraries.discovery.ServiceDiscoveryHelper.InstanceDetails;

public class InstanceToURLFunction
        implements Function<
                ServiceInstance<InstanceDetails>, Iterable<RetryableWebResource.Candidate>> {

    @Override
    public Iterable<RetryableWebResource.Candidate> apply(
            ServiceInstance<InstanceDetails> instance) {
        ImmutableList.Builder<RetryableWebResource.Candidate> builder = ImmutableList.builder();
        if (instance.getSslPort() != null) {
            builder.add(
                    new RetryableWebResource.Candidate(
                            "https", instance.getAddress(), instance.getSslPort()));
        } else if (instance.getPort() != null) {
            builder.add(
                    new RetryableWebResource.Candidate(
                            "http", instance.getAddress(), instance.getPort()));
        }
        return builder.build();
    }
}
