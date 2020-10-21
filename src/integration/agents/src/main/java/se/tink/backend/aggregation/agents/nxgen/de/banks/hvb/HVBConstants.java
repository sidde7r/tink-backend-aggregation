package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

public final class HVBConstants {
    private HVBConstants() {
        throw new AssertionError();
    }

    public static final String APP_ID = "de.unicredit.apptan";
    public static final String APP_VERSION = "4.2.7";
    public static final String OS_NAME = "ios";
    public static final String OS_VERSION = "13.5.1";
    public static final String PLATFORM = "iPhone";

    static final String PREDEFINED_RSA_KEY_PAIR =
            "{\"privKey\":\"30820156020100300d06092a864886f70d0101010"
                    + "500048201403082013c020100024100c29ca7b0fa6c9a99ea4a5ac2043eceef7ec4912ad5d0821e54f62f3b1"
                    + "fd816c9489bdf4f050634bd5bbf954179a86dc4ebc8ba15ebdef634136119024462615702030100010241008"
                    + "70d7fb1e99784925b6cce1ee623502813a905b4dc3c9e8fbe163a20a5b0a7de413c3273138a4c90abd2a5e40"
                    + "4a8aacb1f4a4b3a763c5a7e53c231aad1e73001022100eac6813daf38b96e81b4b8973a5a83de05087a9b934"
                    + "0329fc22bea3664af0501022100d434a1c0c95246e01e6cedeb58f20dd0b080e01ef870eb257615c40b9281a"
                    + "e57022100ccb8399c4b3d805c7f17d25a7454d765f327989a2e85ad5f2796d98d82b0270102210094bd0f638"
                    + "1b919d49d7edc84c9a35feb96cbbe4bf6f7aeda631a7e7f5e0167a1022011f9ec48afe9d6bb275c0edf70cbe"
                    + "3634c05c3329e4bdffff66a1bf25378ee26\",\"alg\":\"RSA\",\"pubKey\":\"305c300d06092a864886f"
                    + "70d0101010500034b003048024100c29ca7b0fa6c9a99ea4a5ac2043eceef7ec4912ad5d0821e54f62f3b1fd"
                    + "816c9489bdf4f050634bd5bbf954179a86dc4ebc8ba15ebdef63413611902446261570203010001\"}\n";
}
