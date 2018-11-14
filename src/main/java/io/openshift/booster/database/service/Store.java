package io.openshift.booster.database.service;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;


/**
 * A CRUD to SQL interface
 */
public interface Store {

  Single<JsonObject> create(JsonObject item);

  Flowable<JsonObject> readAll();

  Single<JsonObject> read(long id);

  Completable update(long id, JsonObject item);

  Completable delete(long id);
}
