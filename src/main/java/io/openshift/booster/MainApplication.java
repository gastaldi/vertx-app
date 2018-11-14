package io.openshift.booster;

import io.openshift.booster.database.CrudApplication;
import io.openshift.booster.http.HttpApplication;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;


/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class MainApplication extends AbstractVerticle {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(MainApplication.class.getName());
  }

  @Override
  public void start(final Future<Void> future) {
    // Create Router
    Router router = createRouter();

    Completable routes = initRoutes(router);
    Completable http = initHttpServer(router);

    //noinspection ResultOfMethodCallIgnored
    routes
      .andThen(http)
      .subscribe(
        future::complete,
        future::fail
      );
  }

  private Completable initRoutes(Router router) {
    return Flowable.fromArray(getRouterConsumers())
      .doOnNext(consumer -> consumer.accept(router))
      .flatMapCompletable(RouterConsumer::start);
  }

  private Completable initHttpServer(Router router) {
    return vertx.createHttpServer()
      .requestHandler(router::accept)
      .rxListen(config().getInteger("http.port", 8080))
      .doOnSuccess(server -> System.out.println("Server started on port " + server.actualPort()))
      .toCompletable();
  }

  private Router createRouter() {
    // Create a router object.
    Router router = Router.router(vertx);
    // enable parsing of request bodies
    router.route().handler(BodyHandler.create());

    // health check
    router.get("/health").handler(rc -> rc.response().end("OK"));

    // web interface
    router.get().last().handler(StaticHandler.create());
    return router;
  }

  private RouterConsumer[] getRouterConsumers() {
    return new RouterConsumer[]{
      // TODO: Add new RouteConsumers here
      new CrudApplication(vertx),
      new HttpApplication(vertx),
    };
  }
}
