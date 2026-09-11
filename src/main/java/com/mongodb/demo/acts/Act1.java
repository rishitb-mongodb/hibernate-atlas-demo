package com.mongodb.demo.acts;

import static com.mongodb.demo.Mql.heading;
import static com.mongodb.demo.Mql.note;
import static com.mongodb.demo.Mql.row;

import com.mongodb.demo.Persistence;
import com.mongodb.demo.model.Movie;

/**
 * ACT 1 -- Familiar Hibernate, no MongoDB-specific code.
 *
 * <p>Same {@code @Entity}, same {@code Session}, same HQL. The only configuration is a MongoDB
 * connection string; the dialect and the connection provider are inferred from it.
 *
 * <p>Watch the log: every statement Hibernate "sends as SQL" is a MongoDB aggregation command.
 */
public final class Act1 {

    private Act1() {}

    public static void run() {
        try (var sessionFactory = Persistence.sampleMflix()) {

            heading("1a. HQL selection query -- translated to $match + $project");
            note("The year guard keeps us off the handful of sample documents whose 'year' is a string.");
            var titanic = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            "from Movie m where m.title = :title and m.year >= 1900", Movie.class)
                    .setParameter("title", "The Shawshank Redemption")
                    .setMaxResults(1)
                    .getSingleResultOrNull());
            row(titanic);

            if (titanic == null) {
                note("No match -- is the sample_mflix dataset loaded on this cluster?");
                return;
            }

            heading("1b. find() by identifier -- a plain _id lookup");
            note("The identifier is a real org.bson.types.ObjectId: " + titanic.getId());
            var byId = sessionFactory.fromTransaction(session -> session.find(Movie.class, titanic.getId()));
            row(byId);

            heading("1c. Scalar projection, ordering and pagination");
            var rows = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            """
                            select m.title, m.year, m.runtime
                            from Movie m
                            where m.rated = :rated and m.year >= 2010
                            order by m.year desc, m.title asc
                            """,
                            Object[].class)
                    .setParameter("rated", "PG-13")
                    .setMaxResults(5)
                    .getResultList());
            rows.forEach(r -> row("%s (%s), %s min".formatted(r[0], r[1], r[2])));

            note("order by / setMaxResults became $sort + $limit. No MongoDB API in sight.");
        }
    }
}
