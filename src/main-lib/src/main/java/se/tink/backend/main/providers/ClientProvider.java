package se.tink.backend.main.providers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.File;
import java.util.List;
import java.util.Map;
import se.tink.backend.core.Client;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ClientProvider implements Provider<Map<String, Client>> {

    private static final LogUtils log = new LogUtils(ClientProvider.class);
    static final String CLIENTS_SE_FILE_PATH = "data/seeding/clients-se.json";
    static final String CLIENTS_ABNAMRO_FILE_PATH = "data/seeding/clients-abnamro.json";

    private final Map<String, Client> clientsByClientId;

    @Inject
    public ClientProvider(Cluster cluster) {
        clientsByClientId = createClientKeyMap(createClientListBasedOnCluster(cluster));
    }

    @Override
    public Map<String, Client> get() {
        return clientsByClientId;
    }

    private List<Client> createClientListBasedOnCluster(Cluster cluster) {
        final File file;

        if (cluster == Cluster.ABNAMRO) {
            file = new File(CLIENTS_ABNAMRO_FILE_PATH);
        } else {
            file = new File(CLIENTS_SE_FILE_PATH);
        }

        List<Client> clients = SerializationUtils.deserializeFromString(file, new TypeReference<List<Client>>() {});
        clients.forEach(client -> client.validate());

        return clients;
    }

    private Map<String, Client> createClientKeyMap(List<Client> clientList) {
        return Maps.uniqueIndex(clientList, Client::getId);
    }
}