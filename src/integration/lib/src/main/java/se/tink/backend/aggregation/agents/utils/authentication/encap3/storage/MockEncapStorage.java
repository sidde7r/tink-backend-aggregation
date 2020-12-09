package se.tink.backend.aggregation.agents.utils.authentication.encap3.storage;

import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class MockEncapStorage extends BaseEncapStorage {

    private int randomBaseCounter = 0;
    private int uuidCounter = 0;

    public MockEncapStorage(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    @Override
    protected String generateRandomBase64Encoded(int randomLength) {
        switch (randomBaseCounter++) {
            case 0:
                return "xcFN0EEksrNP60IIBdPN5+bk1DM5RdRmyiRh7YwTmXY=";
            case 1:
                return "+ADbU59Kk5zj2NEvbGhXqUG9l85NqQUagwNPAVEW7gk=";
            case 2:
                return "K7jW11vELEK9NK6jnjnuzdv6zK5nl11PXeYzdj6bESk=";
            case 3:
                return "fScARq1z8IR4HgS1nQj0loVlyUjzsb/IVkd9upWIPyc=";
            default:
                throw new IllegalStateException("Encap Storage to many generated values");
        }
    }

    @Override
    protected String generateRandomUUID() {
        switch (uuidCounter++) {
            case 0:
                return "c5b048ec-9b74-4189-96fc-0691708b5c74";
            case 1:
                return "79baddfb-42d3-4802-b22c-b1ff9bb31fc6";
            default:
                throw new IllegalStateException("Encap Storage to many generated values");
        }
    }
}
