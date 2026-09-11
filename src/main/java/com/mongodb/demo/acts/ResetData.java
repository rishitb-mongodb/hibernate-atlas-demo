package com.mongodb.demo.acts;

import static com.mongodb.demo.Mql.heading;
import static com.mongodb.demo.Mql.row;

import com.mongodb.client.MongoClients;
import com.mongodb.demo.Persistence;
import org.bson.BsonDocument;

/** Puts the cluster back the way the demo found it. Safe to run repeatedly. */
public final class ResetData {

    private ResetData() {}

    public static void run() {
        try (var mongoClient = MongoClients.create(Persistence.connectionString())) {
            heading("Resetting demo state");
            var result = mongoClient
                    .getDatabase(Persistence.DATABASE)
                    .getCollection("movies", BsonDocument.class)
                    .updateMany(
                            BsonDocument.parse("{ \"staffPick\": { \"$exists\": true } }"),
                            BsonDocument.parse("{ \"$unset\": { \"staffPick\": \"\" } }"));
            row("removed staffPick from " + result.getModifiedCount() + " movies");
        }
    }
}
