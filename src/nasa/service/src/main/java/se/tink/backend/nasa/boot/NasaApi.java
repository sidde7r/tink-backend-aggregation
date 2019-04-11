package se.tink.backend.nasa.boot;

import org.apache.http.HttpResponse;
import spark.Request;
import spark.Response;

public interface NasaApi {
    String ping(Request request, Response response);

    HttpResponse initiate(Request request, Response response) throws Exception;
}
