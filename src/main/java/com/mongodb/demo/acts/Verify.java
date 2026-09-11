package com.mongodb.demo.acts;

import static com.mongodb.demo.Mql.heading;
import static com.mongodb.demo.Mql.note;
import static com.mongodb.demo.Mql.row;

import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;
import com.mongodb.demo.Persistence;
import org.bson.BsonDocument;

/**
 * Pre-flight check. Run this before the demo, not during it.
 *
 * <p>It confirms the cluster is reachable and the sample data is present, and it reports the
 * documents whose field types do not match the entity mapping. {@code sample_mflix} contains a
 * handful of movies whose {@code year} or {@code imdb.rating} is a string rather than a number;
 * reading one of those into a typed entity fails, which is why the queries in this demo carry a
 * {@code m.year >= 1900} guard.
 */
public final class Verify {

    private Verify() {}

    public static void run() {
        try (var mongoClient = MongoClients.create(Persistence.connectionString())) {
            var database = mongoClient.getDatabase(Persistence.DATABASE);

            heading("Cluster and dataset");
            row("connected to database: " + Persistence.DATABASE);
            var movies = database.getCollection("movies", BsonDocument.class);
            var comments = database.getCollection("comments", BsonDocument.class);
            row("movies   = " + movies.countDocuments() + " documents");
            row("comments = " + comments.countDocuments() + " documents");

            heading("Documents that would break a typed read");
            reportOffType(movies, "year", "number");
            reportOffType(movies, "imdb.rating", "number");
            reportOffType(movies, "imdb.votes", "number");
            reportOffType(movies, "runtime", "number");
            note("Each query in the demo constrains year numerically, which excludes these.");

            heading("Replica set check");
            var helloResult = database.runCommand(BsonDocument.parse("{ \"hello\": 1 }"), BsonDocument.class);
            var setName = helloResult.containsKey("setName")
                    ? helloResult.getString("setName").getValue()
                    : null;
            if (setName == null) {
                note("WARNING: this looks like a standalone deployment. The extension needs a");
                note("replica set, because it needs transactions. Atlas is always a replica set.");
            } else {
                row("replica set: " + setName);
            }

            heading("Leftovers from a previous run");
            row("movies carrying staffPick: " + movies.countDocuments(Filters.exists("staffPick")));
            note("hibernate_watchlist is dropped when act 5 finishes, so it should be absent.");
            note("Run 'reset-data' if a run was interrupted.");
        }
    }

    private static void reportOffType(
            com.mongodb.client.MongoCollection<BsonDocument> collection, String field, String expectedType) {
        var offType = collection.countDocuments(
                Filters.and(Filters.exists(field), Filters.not(Filters.type(field, expectedType))));
        row("%-14s not a %s in %d documents".formatted(field, expectedType, offType));
    }
}
