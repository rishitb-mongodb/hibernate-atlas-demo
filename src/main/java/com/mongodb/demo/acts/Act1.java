package com.mongodb.demo.acts;

import static com.mongodb.demo.Mql.heading;
import static com.mongodb.demo.Mql.row;

import com.mongodb.demo.Persistence;
import com.mongodb.demo.model.Movie;

/** Familiar Hibernate, and the document model as a first-class mapping. See DEMO_SCRIPT.md. */
public final class Act1 {

    private Act1() {}

    public static void run() {
        try (var sessionFactory = Persistence.sampleMflix()) {

            heading("1. Plain HQL, by title");
            var movie = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            "from Movie m where m.title = :title and m.year >= 1900", Movie.class)
                    .setParameter("title", "The Dark Knight")
                    .setMaxResults(1)
                    .getSingleResultOrNull());
            row(movie);

            heading("2. Path expression into a sub-document, in WHERE and ORDER BY");
            var acclaimed = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            """
                            from Movie m
                            where m.imdb.rating >= :rating and m.year >= 1900
                            order by m.imdb.rating desc
                            """,
                            Movie.class)
                    .setParameter("rating", 9.0d)
                    .setMaxResults(3)
                    .getResultList());
            acclaimed.forEach(m -> row("%-28s imdb=%s".formatted(m.getTitle(), m.getImdb())));

            heading("3. array_contains() over the directors array");
            var nolan = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            """
                            from Movie m
                            where array_contains(m.directors, :director) and m.year >= 1900
                            order by m.year desc
                            """,
                            Movie.class)
                    .setParameter("director", "Christopher Nolan")
                    .setMaxResults(5)
                    .getResultList());
            nolan.forEach(m -> row("%-28s %s".formatted(m.getTitle(), m.getGenres())));
        }
    }
}
