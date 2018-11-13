package io.openshift.booster;

import java.util.Objects;

import io.openshift.booster.database.CrudApplication;
import io.openshift.booster.http.HttpApplication;
import io.vertx.core.Future;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import rx.Observable;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class MainApplication extends AbstractVerticle {

  @Override
  public void start(final Future future) {
    // Create Router
    Router router = createRouter();

    Observable.from(getRouterConsumers())
      .filter(Objects::nonNull)
      .flatMapCompletable(r -> {
        r.accept(router);
        return r.start();
      })
      .toCompletable()
      .doOnCompleted(() -> future.complete())
      .doOnError(throwable -> future.fail(throwable))
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

  private RouterConsumer[] getRouterConsumers() {
    return new RouterConsumer[]{
      // TODO: Add new RouteConsumers here
      new CrudApplication(vertx),
      new HttpApplication(vertx),
      // This is to ease the code generation
      null
    };
  }
}
