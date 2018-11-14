package io.openshift.booster;

import io.reactivex.Completable;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;

import java.util.function.Consumer;



public abstract class RouterConsumer implements Consumer<Router> {

  protected final Vertx vertx;

  protected RouterConsumer(Vertx vertx) {
    this.vertx = vertx;
  }

  public Completable start() {
    return Completable.complete();
  }

}
