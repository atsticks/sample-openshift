package gh.atsticks.samples.k8s.person;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;


/**
 * REST Service to expose the data to display in the UI grid.
 *
 * @author Roberto Cortez
 */
public class PersonServer extends AbstractVerticle {

    private static final String APPLICATION_JSON = "application/json";

    private HttpServer server;


    @Override
    public void start()throws Exception {
        super.start();
        server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        Router restAPI = Router.router(vertx);
        restAPI.get().handler(this::list);
        restAPI.get("/:id").handler(this::get);
        restAPI.post().handler(this::persist);
        restAPI.delete("/:id").handler(this::delete);
        router.mountSubRouter("/resources/persons", restAPI);
        router.route("/*").handler(StaticHandler.create("webapp"));

        server.requestHandler(router::accept).listen(8080);
    }

    private void delete(RoutingContext rc) {
        vertx.eventBus().send(PersonRepository.DELETE, rc.get("id"), h -> {
            rc.response()
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .end();
        });
    }

    private void persist(RoutingContext rc) {
        rc.request().bodyHandler(buff -> {
            vertx.eventBus().send(PersonRepository.STORE, buff.toString(),
                    h -> {
                        rc.response()
                                .setStatusCode(HttpResponseStatus.CREATED.code())
                                .end();
                    });
        });
    }

    private void get(RoutingContext rc) {
        vertx.eventBus().send(PersonRepository.GET, rc.get("id"), h -> {
            rc.response()
                    .putHeader(HttpHeaderNames.CONTENT_TYPE, APPLICATION_JSON)
                    .end(String.valueOf(h.result().body()));
        });
    }

    private void list(RoutingContext rc) {
        PaginatedListWrapper listWrapper = getListWrapper(rc);
        vertx.eventBus().send(PersonRepository.LIST,
                Json.encode(listWrapper), h -> {
                    rc.response()
                            .putHeader(HttpHeaderNames.CONTENT_TYPE, APPLICATION_JSON)
                            .end(String.valueOf(h.result().body()));
                });
    }

    @Override
    public void stop() throws Exception {
        this.server.close();
        super.stop();
    }

    private PaginatedListWrapper getListWrapper(RoutingContext rc) {
        PaginatedListWrapper wrapper = new PaginatedListWrapper();
        String page = rc.request().getParam("page");
        wrapper.setCurrentPage(page==null?1:Integer.parseInt(page));
        wrapper.setSortDirections(rc.request().getParam("sortDirections"));
        wrapper.setSortFields(rc.request().getParam("sortFields"));
        return wrapper;
    }

}
