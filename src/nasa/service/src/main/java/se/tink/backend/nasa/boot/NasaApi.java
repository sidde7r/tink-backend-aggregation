package se.tink.backend.nasa.boot;

import spark.Request;
import spark.Response;

public interface NasaApi {
    Object ping(Request request, Response response);
    Object initiate(Request request, Response response);
}
