package com.mongodb.demo.acts;

import static com.mongodb.demo.Mql.heading;
import static com.mongodb.demo.Mql.note;
import static com.mongodb.demo.Mql.row;

import com.mongodb.demo.Persistence;
import com.mongodb.demo.model.Movie;

/**
 * ACT 2 -- The document model is first class.
 *
 * <p>Reveal the {@code ACT2} blocks in {@link Movie} and in this file.
 *
 * <p>A relational mapping of this entity needs three extra tables and three joins: one for the imdb
 * columns, one for awards, one per array. Here it is one document and one read.
 */
public final class Act2 {

    private Act2() {}

    public static void run() {
        try (var sessionFactory = Persistence.sampleMflix()) {

            //@ACT2-START
            //~ heading("2a. Querying into a sub-document -- m.imdb.rating becomes the path imdb.rating");
            //~ var acclaimed = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            //~ """
                            //~ from Movie m
                            //~ where m.imdb.rating >= :rating and m.imdb.votes >= :votes and m.year >= 1900
                            //~ order by m.imdb.rating desc
                            //~ """,
                            //~ Movie.class)
                    //~ .setParameter("rating", 8.6d)
                    //~ .setParameter("votes", 500_000)
                    //~ .setMaxResults(5)
                    //~ .getResultList());
            //~ acclaimed.forEach(m -> row("%-38s imdb=%s (%s votes)  awards=%s wins / %s nominations"
                    //~ .formatted(
                            //~ m.getTitle(),
                            //~ m.getImdb().rating(),
                            //~ m.getImdb().votes(),
                            //~ m.getAwards().wins(),
                            //~ m.getAwards().nominations())));
            //~ note("Two sub-documents and the scalars all arrived in a single document read.");
            //~
            //~ heading("2b. Querying inside an array -- array_contains() becomes an equality match");
            //~ var nolan = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            //~ """
                            //~ from Movie m
                            //~ where array_contains(m.directors, :director) and m.year >= 1900
                            //~ order by m.year desc
                            //~ """,
                            //~ Movie.class)
                    //~ .setParameter("director", "Christopher Nolan")
                    //~ .setMaxResults(5)
                    //~ .getResultList());
            //~ nolan.forEach(m -> row("%-28s %s".formatted(m.getTitle(), m.getGenres())));
            //~ note("It renders as an array $type guard plus a plain equality. MongoDB matches an");
            //~ note("array by any element, so there is no join table and no DISTINCT to undo a fan-out.");
            //~
            //~ heading("2c. One entity, one document");
            //~ acclaimed.stream().findFirst().ifPresent(m -> {
                //~ row("title     = " + m.getTitle());
                //~ row("genres    = " + m.getGenres());
                //~ row("countries = " + m.getCountries());
                //~ row("directors = " + m.getDirectors());
                //~ row("imdb      = " + m.getImdb());
                //~ row("awards    = " + m.getAwards());
            //~ });
            //@ACT2-END
        }
    }
}
