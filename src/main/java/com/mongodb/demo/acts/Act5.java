package com.mongodb.demo.acts;

import static com.mongodb.demo.Mql.heading;
import static com.mongodb.demo.Mql.note;
import static com.mongodb.demo.Mql.pretty;
import static com.mongodb.demo.Mql.row;

import com.mongodb.client.MongoClients;
import com.mongodb.demo.Persistence;
import com.mongodb.demo.model.MovieSummary;
import com.mongodb.demo.model.WatchlistEntry;
import jakarta.persistence.OptimisticLockException;
import org.bson.BsonDocument;

/**
 * ACT 5 -- The escape hatch, and the production details.
 *
 * <p>The extension does not aim for full HQL coverage, and some of MongoDB's best features have no
 * HQL spelling at all. When you need them you stay inside the same {@code Session}: hand MongoDB a
 * pipeline and get entities back.
 */
public final class Act5 {

    private Act5() {}

    /**
     * A pipeline HQL cannot express: it filters and sorts on {@code tomatoes.*}, which the
     * {@link MovieSummary} entity does not map at all. {@code $search}, {@code $vectorSearch} and
     * {@code $geoNear} go in exactly this slot.
     *
     * <p>The trailing {@code $project} is required: the extension reads the result field names off
     * it, and they must match the fields of the entity being hydrated.
     */
    private static final String PIPELINE =
            """
            {
              "aggregate": "movies",
              "pipeline": [
                { "$match": {
                    "tomatoes.viewer.rating": { "$gte": 4.2 },
                    "tomatoes.viewer.numReviews": { "$gte": 5000 },
                    "year": { "$gte": 1990 }
                } },
                { "$sort": { "tomatoes.viewer.numReviews": -1 } },
                { "$limit": 5 },
                { "$project": {
                    "_id": 1, "title": 1, "year": 1, "runtime": 1, "rated": 1
                } }
              ]
            }
            """;

    public static void run() {
        try (var sessionFactory = Persistence.sampleMflix()) {

            heading("5a. Native MongoDB pipeline, hydrated straight into entities");
            note("Sorting by tomatoes.viewer.numReviews -- a field the entity does not even map:");
            System.out.println(pretty(PIPELINE));
            var crowdPleasers = sessionFactory.fromTransaction(
                    session -> session.createNativeQuery(PIPELINE, MovieSummary.class).getResultList());
            crowdPleasers.forEach(m -> row(m));
            note("Same Session, same transaction, real managed entities. This is where Atlas");
            note("Search, Vector Search and geospatial queries live until HQL grows a spelling.");

            heading("5b. $unset -- the cleanup Act 3 could not express in HQL");
            try (var mongoClient = MongoClients.create(Persistence.connectionString())) {
                var result = mongoClient
                        .getDatabase(Persistence.DATABASE)
                        .getCollection("movies", BsonDocument.class)
                        .updateMany(
                                BsonDocument.parse("{ \"staffPick\": { \"$exists\": true } }"),
                                BsonDocument.parse("{ \"$unset\": { \"staffPick\": \"\" } }"));
                row("removed the staffPick field from " + result.getModifiedCount() + " documents");
            }
        }

        try (var sessionFactory = Persistence.watchlist()) {

            heading("5c. JPA @Index and @UniqueConstraint, created as MongoDB indexes");
            note("Bootstrapping this SessionFactory issued create + createIndexes (above).");
            note("Reading the indexes back with the driver:");
            try (var mongoClient = MongoClients.create(Persistence.connectionString())) {
                mongoClient
                        .getDatabase(Persistence.DATABASE)
                        .getCollection("hibernate_watchlist", BsonDocument.class)
                        .listIndexes(BsonDocument.class)
                        .forEach(index -> row(index.toJson()));
            }

            heading("5d. Optimistic locking with @Version");
            var seeded = sessionFactory.fromTransaction(session -> {
                session.createMutationQuery("delete from WatchlistEntry").executeUpdate();
                var entry = new WatchlistEntry("demo@mongodb.com", "Titanic", 1);
                session.persist(entry);
                return entry.getId();
            });
            row("persisted " + sessionFactory.fromTransaction(session -> session.find(WatchlistEntry.class, seeded)));

            note("Two sessions load the same entry at version 0 and both try to write it.");
            var first = sessionFactory.fromSession(session -> session.find(WatchlistEntry.class, seeded));
            var second = sessionFactory.fromSession(session -> session.find(WatchlistEntry.class, seeded));

            sessionFactory.inTransaction(session -> session.merge(first.setPriority(2)));
            row("session 1 committed priority=2");

            try {
                sessionFactory.inTransaction(session -> session.merge(second.setPriority(99)));
                note("UNEXPECTED: the stale write was accepted.");
            } catch (OptimisticLockException e) {
                row("session 2 was rejected: " + e.getClass().getSimpleName());
                note("The update carried {_id: ..., version: 0} as its filter, matched nothing,");
                note("and Hibernate turned the zero matched count into a lost-update error.");
            }

            row("final state: " + sessionFactory.fromTransaction(session -> session.find(WatchlistEntry.class, seeded)));
        }
    }
}
