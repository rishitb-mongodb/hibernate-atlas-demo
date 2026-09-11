package com.mongodb.demo.acts;

import static com.mongodb.demo.Mql.heading;
import static com.mongodb.demo.Mql.note;
import static com.mongodb.demo.Mql.row;

import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;
import com.mongodb.demo.Persistence;
import org.bson.BsonDocument;

/** Pre-flight check. Run before the demo, not during it. */
public final class Verify {

    private Verify() {}

    public static void run() {
        try (var mongoClient = MongoClients.create(Persistence.connectionString())) {
            var database = mongoClient.getDatabase(Persistence.DATABASE);

            heading("Cluster and dataset");
            var movies = database.getCollection("movies", BsonDocument.class);
            var comments = database.getCollection("comments", BsonDocument.class);
            row("movies   = " + movies.countDocuments() + " documents");
            row("comments = " + comments.countDocuments() + " documents");

            heading("Documents that would break a typed read");
            var offYear = movies.countDocuments(Filters.and(Filters.exists("year"), Filters.not(Filters.type("year", "number"))));
            var offRating = movies.countDocuments(
                    Filters.and(Filters.exists("imdb.rating"), Filters.not(Filters.type("imdb.rating", "number"))));
            row("year not a number: " + offYear);
            row("imdb.rating not a number: " + offRating);
            note("Every query in the demo guards m.year >= 1900, which excludes these.");

            heading("Replica set check");
            var hello = database.runCommand(BsonDocument.parse("{ \"hello\": 1 }"), BsonDocument.class);
            if (hello.containsKey("setName")) {
                row("replica set: " + hello.getString("setName").getValue());
            } else {
                note("WARNING: this looks like a standalone deployment; the extension needs a replica set.");
            }

            heading("Leftovers from a previous run");
            row("movies carrying staffPick: " + movies.countDocuments(Filters.exists("staffPick")));
            note("Run 'reset-data' if that isn't 0.");
        }
    }
}
