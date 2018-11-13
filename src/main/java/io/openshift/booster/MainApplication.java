package io.openshift.booster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.openshift.booster.database.CrudApplication;
import io.openshift.booster.http.HttpApplication;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import rx.Completable;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class MainApplication extends AbstractVerticle {

  @Override
  public void start() {
    // Create Router
    Router router = createRouter();

    List<Completable> completables = new ArrayList<>();

    // Initialize all Router Consumers
    for (RouterConsumer consumer : getRouterConsumers()) {
      consumer.accept(router);
      completables.add(consumer.start());
    }

    Completable.concat(completables)
      .subscribe(() ->
                   vertx.createHttpServer()
                     .requestHandler(router::accept)
                     .rxListen(config().getInteger("http.port", 8080))
                     .subscribe(httpServer ->
                                  System.out.println("Server started on port " + httpServer.actualPort()))
      );
  }

  private Router createRouter() {
    // Create a router object.
    Router router = Router.router(vertx);
    // enable parsing of request bodies
    router.route().handler(BodyHandler.create());

    // health check
    router.get("/health").handler(rc -> rc.response().end("OK"));

    // web interface
    router.get().handler(StaticHandler.create());
    return router;
  }


  private Collection<RouterConsumer> getRouterConsumers() {
    return Arrays.asList(
      new CrudApplication(vertx),
      new HttpApplication(vertx)
    );
  }
}
