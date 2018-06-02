package se.tink.backend.system.cli.seeding.search;

/**
 * This class implements the efficient hash function
 * developed by <i>Daniel J. Bernstein</i>.
 */
public class DjbHashFunction {

    public static int DJB_HASH(String value) {
        long hash = 5381;

        for (int i = 0; i < value.length(); i++) {
            hash = ((hash << 5) + hash) + value.charAt(i);
        }

        return (int) hash;
    }

    public static int DJB_HASH(byte[] value, int offset, int length) {
        long hash = 5381;

        final int end = offset + length;
        for (int i = offset; i < end; i++) {
            hash = ((hash << 5) + hash) + value[i];
        }

        return (int) hash;
    }

    public int hash(String routing) {
        return DJB_HASH(routing);
    }

    public int hash(String type, String id) {
        long hash = 5381;

        for (int i = 0; i < type.length(); i++) {
            hash = ((hash << 5) + hash) + type.charAt(i);
        }

        for (int i = 0; i < id.length(); i++) {
            hash = ((hash << 5) + hash) + id.charAt(i);
        }

        return (int) hash;
    }

    public int calculateShardIndex(String userId, int totalShards) {
        int shard = Math.abs(hash(userId) % totalShards);
        return shard;
    }
}