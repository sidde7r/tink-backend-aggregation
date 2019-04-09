package se.tink.libraries.discovery;

import com.google.common.base.Preconditions;
import io.dropwizard.lifecycle.Managed;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.uuid.UUIDUtils;

// TODO: Split this class into two classes; One that announces a service and another which allows
// querying for a service.
public class ServiceDiscoveryHelper implements Managed {
    public static class InstanceDetails {
        @JsonProperty private String workerId;

        public InstanceDetails() {}

        InstanceDetails(@JsonProperty String workerId) {
            this.workerId = workerId;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ServiceDiscoveryHelper.class);

    private static final ModifiedJsonInstanceSerializer<InstanceDetails> serializer =
            new ModifiedJsonInstanceSerializer<>(InstanceDetails.class);
    private AtomicReference<ServiceCache<InstanceDetails>> cache;
    private CuratorFramework coordinationClient;

    private ServiceDiscovery<InstanceDetails> discovery;
    private Optional<Integer> port;

    private Random random = new Random();

    private String serviceName;

    private ServiceInstance<InstanceDetails> currentInstance;

    private Optional<Integer> tlsPort;

    // Only needed when announcing a service
    private CoordinationConfiguration coordinationConfiguration;

    /**
     * Constructor when helper will only be used for discovery and not for registering a service.
     */
    public ServiceDiscoveryHelper(CuratorFramework coordinationClient, String serviceName) {
        this.coordinationClient = coordinationClient;
        this.serviceName = serviceName;

        discovery = getDiscovery();
        cache = new AtomicReference(getCache());

        try {
            discovery.start();
            cache.get().start();
            startServicesWatcher();
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate ServiceDiscoveryHelper.", e);
        }
    }

    /** Constructor when helper will be used for either discovery or announcing a service. */
    public ServiceDiscoveryHelper(
            CuratorFramework coordinationClient,
            CoordinationConfiguration coordinationConfiguration,
            String serviceName,
            Optional<Integer> port,
            Optional<Integer> tlsPort) {

        Preconditions.checkArgument(
                port.isPresent() || tlsPort.isPresent(),
                "Either HTTP or HTTPS port must be present.");

        this.coordinationClient = coordinationClient;
        this.serviceName = serviceName;
        this.port = port;
        this.tlsPort = tlsPort;

        if (coordinationConfiguration != null) {
            this.coordinationConfiguration = coordinationConfiguration;
        }

        discovery = getDiscovery();
        cache = new AtomicReference(getCache());

        try {
            discovery.start();
            cache.get().start();
            startServicesWatcher();
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate ServiceDiscoveryHelper.", e);
        }
    }

    /**
     * Constructor when helper will be used for either discovery or announcing a service.
     *
     * <p>Is only used by Integration tests for the Encryption container.
     */
    @Deprecated
    public ServiceDiscoveryHelper(
            CuratorFramework coordinationClient,
            String serviceName,
            Optional<Integer> port,
            Optional<Integer> tlsPort) {
        this(coordinationClient, null, serviceName, port, tlsPort);
    }

    /**
     * Deregister a previously registered service.
     *
     * <p>For outside accessors, call {@link #stop()} instead of this.
     */
    private void deregisterService(ServiceInstance<InstanceDetails> serviceInstance)
            throws Exception {
        log.info("Deregistering service container: {}", serviceName);
        discovery.unregisterService(serviceInstance);
    }

    private ServiceCache<InstanceDetails> getCache() {
        return discovery.serviceCacheBuilder().name(serviceName).build();
    }

    private ServiceDiscovery<InstanceDetails> getDiscovery() {
        return ServiceDiscoveryBuilder.builder(InstanceDetails.class)
                .serializer(serializer)
                .basePath("/services")
                .client(coordinationClient)
                .build();
    }

    private ServiceInstance<InstanceDetails> generateInstance() throws Exception {
        String workerId = UUIDUtils.generateUUID();

        ServiceInstanceBuilder<InstanceDetails> builder =
                ServiceInstance.<InstanceDetails>builder()
                        .name(serviceName)
                        .id(workerId)
                        .payload(new InstanceDetails(workerId));

        if (coordinationConfiguration != null) {
            // The configuration is on the format IP:PORT,IP:PORT
            // Trim up until the first colon to only get one IP
            String firstZookeeperIP = coordinationConfiguration.getHosts();
            firstZookeeperIP = firstZookeeperIP.substring(0, firstZookeeperIP.indexOf(":"));

            // Find the IP that we want to advertise
            String advertiseIP =
                    ExposedIpFinder.getIpForRoute(
                            ServiceInstanceBuilder.getAllLocalIPs(), firstZookeeperIP);

            log.debug(
                    String.format(
                            "Advertising IP %s based on ZooKeeper IP of %s",
                            advertiseIP, firstZookeeperIP));

            builder.address(advertiseIP);
        }

        if (tlsPort.isPresent()) {
            builder.sslPort(tlsPort.get());
        } else if (port.isPresent()) {
            builder.port(port.get());
        }

        return builder.build();
    }

    public ServiceInstance<InstanceDetails> queryForInstance() {
        List<ServiceInstance<InstanceDetails>> instances = queryForInstances();
        if (instances.isEmpty()) {
            throw new IllegalStateException(
                    String.format("No running %s instances found. Are all down?", serviceName));
        }

        return instances.get(random.nextInt(instances.size()));
    }

    public List<ServiceInstance<InstanceDetails>> queryForInstances() {
        try {
            return cache.get().getInstances();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Register a previously registered service.
     *
     * <p>For outside accessors, call {@link #start()} instead of this.
     */
    private void registerService(ServiceInstance<InstanceDetails> serviceInstance)
            throws Exception {
        log.info("Registering service instance: {}", serviceInstance);
        discovery.registerService(serviceInstance);
    }

    /**
     * Need this simply to fulfill the Managed interface.
     *
     * @see com.yammer.dropwizard.lifecycle.Managed#start()
     */
    @Override
    public void start() throws Exception {
        currentInstance = generateInstance();

        registerService(currentInstance);
    }

    /**
     * Need this simply to fulfill the Managed interface.
     *
     * @see com.yammer.dropwizard.lifecycle.Managed#stop()
     */
    @Override
    public void stop() throws Exception {
        deregisterService(currentInstance);
    }

    void restartServiceCache() throws Exception {
        log.info("restartServiceCache for service {}", serviceName);

        // Initialize new cache
        ServiceCache<InstanceDetails> newCache = getCache();
        newCache.start();

        // Replace cache
        ServiceCache<InstanceDetails> oldCache = cache.getAndSet(newCache);

        // Close previous cache
        oldCache.close();
    }

    private void startServicesWatcher() throws Exception {
        PathChildrenCache watcherWatcher =
                new PathChildrenCache(coordinationClient, "/services", false);
        watcherWatcher.getListenable().addListener(new ServicesWatcher(this, serviceName));
        watcherWatcher.start();
    }
}

/** Helper class to support new version of Jackson in Curator. */
class ModifiedJsonInstanceSerializer<T> implements InstanceSerializer<T> {
    private final ObjectMapper mMapper;
    private final Class<T> mPayloadClass;

    /** @param payloadClass used to validate payloads when deserializing */
    ModifiedJsonInstanceSerializer(final Class<T> payloadClass) {
        this(payloadClass, new ObjectMapper());
    }

    private ModifiedJsonInstanceSerializer(
            final Class<T> pPayloadClass, final ObjectMapper pMapper) {
        mPayloadClass = pPayloadClass;
        mMapper = pMapper;
        mMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ServiceInstance<T> deserialize(final byte[] pBytes) throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(pBytes);
        final JsonNode rootNode = mMapper.readTree(bais);
        final ServiceInstanceBuilder<T> builder = ServiceInstance.builder();
        {
            final String address = getTextField(rootNode, "address");
            if (address != null) {
                builder.address(address);
            }
        }
        {
            final String id = getTextField(rootNode, "id");
            if (id != null) {
                builder.id(id);
            }
        }
        {
            final String name = getTextField(rootNode, "name");
            if (name != null) {
                builder.name(name);
            }
        }
        {
            final Integer port = getIntegerField(rootNode, "port");
            if (port != null) {
                builder.port(port);
            }
        }
        {
            final Integer sslPort = getIntegerField(rootNode, "sslPort");
            if (sslPort != null) {
                builder.sslPort(sslPort);
            }
        }
        {
            final Long registrationTimeUTC = getLongField(rootNode, "registrationTimeUTC");
            if (registrationTimeUTC != null) {
                builder.registrationTimeUTC(registrationTimeUTC);
            }
        }
        {
            final T payload = getObject(rootNode, "payload", mPayloadClass);
            if (payload != null) {
                builder.payload(payload);
            }
        }
        {
            final ServiceType serviceType = getObject(rootNode, "serviceType", ServiceType.class);
            if (serviceType != null) {
                builder.serviceType(serviceType);
            }
        }
        {
            final UriSpec uriSpec = getObject(rootNode, "uriSpec", UriSpec.class);
            if (uriSpec != null) {
                builder.uriSpec(uriSpec);
            }
        }
        return builder.build();
    }

    private Integer getIntegerField(final JsonNode pNode, final String pFieldName) {
        Preconditions.checkNotNull(pNode);
        Preconditions.checkNotNull(pFieldName);
        return (pNode.get(pFieldName) != null && pNode.get(pFieldName).isNumber())
                ? pNode.get(pFieldName).getIntValue()
                : null;
    }

    private Long getLongField(final JsonNode pNode, final String pFieldName) {
        Preconditions.checkNotNull(pNode);
        Preconditions.checkNotNull(pFieldName);
        return (pNode.get(pFieldName) != null && pNode.get(pFieldName).isLong())
                ? pNode.get(pFieldName).getLongValue()
                : null;
    }

    private <O> O getObject(
            final JsonNode pNode, final String pFieldName, final Class<O> pObjectClass)
            throws IOException {
        Preconditions.checkNotNull(pNode);
        Preconditions.checkNotNull(pFieldName);
        Preconditions.checkNotNull(pObjectClass);
        if (pNode.get(pFieldName) != null && pNode.get(pFieldName).isObject()) {
            return mMapper.readValue(pNode.get(pFieldName), pObjectClass);
        } else {
            return null;
        }
    }

    private String getTextField(final JsonNode pNode, final String pFieldName) {
        Preconditions.checkNotNull(pNode);
        Preconditions.checkNotNull(pFieldName);
        return pNode.get(pFieldName) != null ? pNode.get(pFieldName).getTextValue() : null;
    }

    @Override
    public byte[] serialize(final ServiceInstance<T> pInstance) throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        mMapper.writeValue(out, pInstance);
        return out.toByteArray();
    }
}

/**
 * Curators ServiceCache does not properly handle the situation where all services disappear and
 * then come back again
 *
 * <p>The znode "/services/${serviceName}" is of the ZK type CONTAINER. Containers are automatically
 * garbage collected by ZK if the become empty after having contained something.
 *
 * <p>ServicesWatcher listens on changes to "/services", and if "/services/${serviceName}" is
 * removed we'll restart ServiceCache, which creates "/services/${serviceName}" again, and is able
 * to discover new service announcements.
 *
 * <p>This Curator bug is tracked here: https://issues.apache.org/jira/browse/CURATOR-388
 */
class ServicesWatcher implements PathChildrenCacheListener {
    private static final Logger log = LoggerFactory.getLogger(ServiceDiscoveryHelper.class);

    private String serviceName;
    private ServiceDiscoveryHelper serviceDiscoveryHelper;

    ServicesWatcher(ServiceDiscoveryHelper serviceDiscoveryHelper, String serviceName) {
        this.serviceName = serviceName;
        this.serviceDiscoveryHelper = serviceDiscoveryHelper;
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event)
            throws Exception {
        if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
            log.info("ServicesWatcher CHILD_REMOVED: " + event.toString());

            if (event.getData().getPath().equals("/services/" + serviceName)) {
                serviceDiscoveryHelper.restartServiceCache();
            }
        }
    }
}
