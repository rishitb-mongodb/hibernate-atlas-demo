package com.mongodb.demo.acts;

import static com.mongodb.demo.Mql.heading;
import static com.mongodb.demo.Mql.note;
import static com.mongodb.demo.Mql.row;

import com.mongodb.demo.Persistence;

/** JOINs across two collections, translated to $lookup. See DEMO_SCRIPT.md. */
public final class Act3 {

    private Act3() {}

    public static void run() {
        try (var sessionFactory = Persistence.sampleMflix()) {

            var email = sessionFactory.fromTransaction(
                    session -> session.createSelectionQuery("select c.email from Comment c", String.class)
                            .setMaxResults(1)
                            .getSingleResultOrNull());
            note("commenter: " + email);

            heading("1. INNER JOIN -- comments to their movie");
            var joined = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            """
                            select c.name, m.title, m.year
                            from Comment c
                            join c.movie m
                            where c.email = :email and m.year >= 1900
                            order by m.year desc
                            """,
                            Object[].class)
                    .setParameter("email", email)
                    .setMaxResults(5)
                    .getResultList());
            joined.forEach(r -> row("%s commented on %s (%s)".formatted(r[0], r[1], r[2])));

            heading("2. LEFT OUTER JOIN -- keeps comments whose movie is missing");
            var left = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            """
                            select c.name, m.title
                            from Comment c
                            left join c.movie m
                            where c.email = :email
                            """,
                            Object[].class)
                    .setParameter("email", email)
                    .setMaxResults(5)
                    .getResultList());
            left.forEach(r -> row("%s -> %s".formatted(r[0], r[1] == null ? "<no movie>" : r[1])));
        }
    }
}
