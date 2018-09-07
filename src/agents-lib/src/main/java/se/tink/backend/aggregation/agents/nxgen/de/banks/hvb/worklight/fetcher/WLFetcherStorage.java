package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.fetcher;

public interface WLFetcherStorage {
    String getWlInstanceId();

    String getSharedAesKey();

    void setSharedAesKey(String s);

    String getSharedAesIv();

    void setSharedAesIv(String s);
}
