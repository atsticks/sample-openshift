package gh.atsticks.samples.k8s.person;

import com.fasterxml.jackson.core.JsonGenerator;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;


public class PersonApp extends AbstractVerticle {

    private HttpServer server;

    @Override
    public void start() throws Exception {
        super.start();
//        Json.mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
//        Json.prettyMapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
        vertx.deployVerticle(PersonRepository.class.getName());
        vertx.deployVerticle(PersonServer.class.getName());
    }

    @Override
    public void stop() throws Exception {
        this.server.close();
        super.stop();
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new PersonApp());
    }
}