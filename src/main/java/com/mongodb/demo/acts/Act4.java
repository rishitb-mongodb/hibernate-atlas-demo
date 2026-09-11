package com.mongodb.demo.acts;

import static com.mongodb.demo.Mql.heading;
import static com.mongodb.demo.Mql.note;
import static com.mongodb.demo.Mql.row;

import com.mongodb.demo.Persistence;
import com.mongodb.demo.model.Movie;

/**
 * ACT 4 -- The real query surface: HQL, Criteria and JOINs, all translated to MQL.
 *
 * <p>This act deliberately uses only the fields mapped in Act 1, so it runs whether or not the
 * later blocks have been revealed.
 */
public final class Act4 {

    private Act4() {}

    public static void run() {
        try (var sessionFactory = Persistence.sampleMflix()) {

            heading("4a. CASE expressions and string functions");
            var classified = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            """
                            select concat(m.title, ' [', m.rated, ']'),
                                   character_length(m.plot),
                                   case
                                       when m.runtime >= 150 then 'epic'
                                       when m.runtime >= 100 then 'standard'
                                       else 'short'
                                   end
                            from Movie m
                            where m.runtime is not null
                              and m.plot is not null
                              and m.rated is not null
                              and m.year >= 2015
                            order by m.runtime desc
                            """,
                            Object[].class)
                    .setMaxResults(5)
                    .getResultList());
            classified.forEach(r -> row("%-46s plot=%s chars  %s".formatted(r[0], r[1], r[2])));
            note("concat -> $concat, character_length -> $strLenCP, case/when -> $switch.");

            heading("4b. Date functions over a BSON date");
            var dated = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            """
                            select m.title,
                                   extract(year from m.released),
                                   extract(month from m.released)
                            from Movie m
                            where m.released is not null and m.year >= 2015
                            order by m.released desc
                            """,
                            Object[].class)
                    .setMaxResults(5)
                    .getResultList());
            dated.forEach(r -> row("%-40s released %s-%02d".formatted(r[0], r[1], (Integer) r[2])));

            heading("4c. in / between / like");
            var shortlist = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            """
                            from Movie m
                            where m.rated in :ratings
                              and m.runtime between 80 and 100
                              and m.title like 'The %'
                              and m.year >= 1990
                            order by m.title
                            """,
                            Movie.class)
                    .setParameterList("ratings", java.util.List.of("G", "PG"))
                    .setMaxResults(5)
                    .getResultList());
            shortlist.forEach(m -> row(m));
            note("$in, a two-sided $gte/$lte, and like -> a $regularExpression match.");

            heading("4d. group by");
            var ratings = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            """
                            select m.rated
                            from Movie m
                            where m.rated is not null and m.year >= 2010
                            group by m.rated
                            order by m.rated
                            """,
                            String.class)
                    .getResultList());
            row(ratings);
            note("GROUP BY over columns and over expressions landed in alpha2. Accumulators");
            note("(count/sum/avg) are the next piece of that work, so do not reach for them yet.");

            heading("4e. INNER JOIN across two collections -- $lookup + $unwind");
            var email = sessionFactory
                    .fromTransaction(session -> session.createSelectionQuery("select c.email from Comment c", String.class)
                            .setMaxResults(1)
                            .getSingleResultOrNull());
            note("Using a real commenter from the sample data: " + email);
            var joined = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            """
                            select c.name, m.title, m.year
                            from Comment c
                            join c.movie m
                            where c.email = :email and m.year >= 1900
                            order by c.date desc
                            """,
                            Object[].class)
                    .setParameter("email", email)
                    .setMaxResults(5)
                    .getResultList());
            joined.forEach(r -> row("%s commented on %s (%s)".formatted(r[0], r[1], r[2])));

            heading("4f. LEFT OUTER JOIN -- keeps comments whose movie is missing");
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
            note("JOINs exist so that a migrating codebase keeps compiling. For a MongoDB-native");
            note("design you would embed the comments in the movie document and skip the lookup.");

            heading("4g. The Criteria API works too -- same translation, no strings");
            var criteriaResults = sessionFactory.fromTransaction(session -> {
                var builder = session.getCriteriaBuilder();
                var query = builder.createQuery(Movie.class);
                var movie = query.from(Movie.class);
                query.select(movie)
                        .where(
                                builder.equal(movie.get("rated"), "PG"),
                                builder.greaterThanOrEqualTo(movie.<Integer>get("year"), 2000),
                                builder.greaterThanOrEqualTo(movie.<Integer>get("runtime"), 120))
                        .orderBy(builder.desc(movie.get("runtime")));
                return session.createQuery(query).setMaxResults(5).getResultList();
            });
            criteriaResults.forEach(m -> row(m));

        }
    }
}
