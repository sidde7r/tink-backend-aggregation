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

        Spark.post(
                createUpdateServiceEndpoint("credentials/supplementalInformation"),
                (request, response) -> "OK!");

        Spark.post(
                createUpdateServiceEndpoint("accounts/update"),
                (request, response) -> "OK!");

        Spark.put(
                createUpdateServiceEndpoint("accounts/*/update"),
                (request, response) -> "OK!");

        Spark.post(
                createUpdateServiceEndpoint("accounts/transfer-destinations/update"),
                (request, response) -> "OK!");

        Spark.post(
                createUpdateServiceEndpoint("accounts/process"),
                (request, response) -> "OK!");

        Spark.post(
                createUpdateServiceEndpoint("accounts/opt-out"),
                (request, response) -> "OK!");

        Spark.post(
                createUpdateServiceEndpoint("credentials/update"),
                (request, response) -> "OK!");

        Spark.post(
                createUpdateServiceEndpoint("credentials/operation/update"),
                (request, response) -> "OK!");

        Spark.post(
                createUpdateServiceEndpoint("transfer/process"),
                (request, response) -> "OK!");

        Spark.post(
                createUpdateServiceEndpoint("document"),
                (request, response) -> "OK!");

        Spark.post(
                createUpdateServiceEndpoint("product/information"),
                (request, response) -> "OK!");

        Spark.post(
                createUpdateServiceEndpoint("application"),
                (request, response) -> "OK!");

        Spark.post(
                createUpdateServiceEndpoint("fraud/update"),
                (request, response) -> "OK!");
    }

    static void createProcessServiceEndpoints() {
        Spark.post(
                createUpdateServiceEndpoint("statisticsandactivities/generate"),
                (request, response) -> "OK!");

        Spark.post(
                createUpdateServiceEndpoint("transactions/update"),
                (request, response) -> "OK!");
    }

    static void createCredentialsServiceEndpoints() {
        Spark.put(
                createUpdateServiceEndpoint("sensitive"),
                (request, response) -> "OK!");
    }
}
