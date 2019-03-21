package se.tink.backend.nasa.boot;

import spark.Spark;

class AggregationControllerApiFactory {
    private static final String UPDATE_SERVICE = "/aggregation/controller/v1/system/update/%s";

    private static String createUpdateServiceEndpoint(String path) {
        return String.format(UPDATE_SERVICE, path);
    }

    static void createUpdateServiceEndpoints() {
        Spark.get(
                createUpdateServiceEndpoint("ping"),
                (request, response) -> "pong");
    }
}
