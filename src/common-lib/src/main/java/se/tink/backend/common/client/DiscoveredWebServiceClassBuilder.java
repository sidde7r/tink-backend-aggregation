package se.tink.backend.common.client;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceInstance;
import se.tink.backend.common.client.retry.RetryableWebResource;
import se.tink.backend.common.client.retry.RetryableWebResource.Candidate;
import se.tink.backend.utils.RotatedListView;
import se.tink.libraries.discovery.ServiceDiscoveryHelper;
import se.tink.libraries.discovery.ServiceDiscoveryHelper.InstanceDetails;
import se.tink.libraries.http.client.ServiceClassBuilder;
import se.tink.libraries.http.client.WebResourceFactory;

public class DiscoveredWebServiceClassBuilder implements ServiceClassBuilder {

    private static final InstanceToURLFunction urlGenerator = new InstanceToURLFunction();

    private Client client;
    private ServiceDiscoveryHelper serviceDiscoveryHelper;
    private String serviceName;

    public DiscoveredWebServiceClassBuilder(CuratorFramework coordinationClient, Client client, String serviceName) {
        this.serviceDiscoveryHelper = new ServiceDiscoveryHelper(coordinationClient, serviceName);
        this.serviceName = serviceName;
        this.client = client;
    }

    private <T> T build(Class<T> serviceClass, List<Candidate> candidates) {
        ServiceInstance<InstanceDetails> instance = serviceDiscoveryHelper.queryForInstance();

        String url = "http://" + instance.getAddress() + ":" + instance.getPort() + "/";

        // Can't be reused sinceRetryableWebResource.decorate adds a filter to it.
        WebResource jerseyResource = client.resource(url);

        // Decorate the jerseyResource witth a filter that retries

        RetryableWebResource.decorate(jerseyResource, candidates, serviceName);

        // Create a proxy class that acts as `serviceClass`, but transparently delegates HTTP calls to jerseyResource.

        return WebResourceFactory.newResource(serviceClass, jerseyResource);
    }

    @Override
    public <T> T build(Class<T> serviceClass) {

        // Build a shuffled list of candidates.

        ArrayList<Candidate> candidates = Lists.newArrayList(getCandidates());
        Collections.shuffle(candidates);

        // Build.

        return build(serviceClass, candidates);

    }

    private Iterable<Candidate> getCandidates() {
        return Iterables.concat(Iterables.transform(
                serviceDiscoveryHelper.queryForInstances(), urlGenerator));
    }

    @Override
    public <T> T build(Class<T> serviceClass, Object hashSource) {

        // Using a sorted list here to always use the same fallback in case the first candidate doesn't work.
        ImmutableList<Candidate> sortedCandidates = ImmutableSortedSet.copyOf(getCandidates()).asList();

        Preconditions.checkState(sortedCandidates.size() > 0, "There are no services announced for '%s'.",
                serviceClass);

        // Using consistent hashing here to have as few requests as possible being directed to a new candidate if
        // when/if candidates come and go.
        int bucket = Hashing.consistentHash(hashSource.hashCode(), sortedCandidates.size());

        // First item in `candidates` list below will be the same element as sortedCandidates.get(bucket).
        List<Candidate> candidates = RotatedListView.of(sortedCandidates, -bucket);

        // Build

        return build(serviceClass, candidates);

    }

}
