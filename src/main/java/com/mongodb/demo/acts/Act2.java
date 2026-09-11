package com.mongodb.demo.acts;

import static com.mongodb.demo.Mql.heading;
import static com.mongodb.demo.Mql.row;

import com.mongodb.demo.Persistence;
import com.mongodb.demo.model.Movie;

/** Schema flexibility. Reveal the {@code //@REVEAL} block in Movie.java first. See DEMO_SCRIPT.md. */
public final class Act2 {

    private Act2() {}

    private static final String TITLE = "The Dark Knight";

    public static void run() {
        try (var sessionFactory = Persistence.sampleMflix()) {

            //@REVEAL-START
            //~ heading("1. The field doesn't exist yet");
            //~ var before = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            //~ "from Movie m where m.title = :title and m.year >= 1900", Movie.class)
                    //~ .setParameter("title", TITLE)
                    //~ .setMaxResults(1)
                    //~ .getSingleResultOrNull());
            //~ row(before.getTitle() + " staffPick = " + before.getStaffPick());
            //~
            //~ heading("2. Write it on one entity");
            //~ sessionFactory.inTransaction(session -> {
                //~ var movie = session.find(Movie.class, before.getId());
                //~ movie.setStaffPick(true);
            //~ });
            //~
            //~ heading("3. Query on the brand-new field immediately");
            //~ var picks = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            //~ "from Movie m where m.staffPick = true and m.year >= 1900", Movie.class)
                    //~ .getResultList());
            //~ picks.forEach(m -> row(m.getTitle() + " staffPick = " + m.getStaffPick()));
            //~
            //~ heading("4. Bulk-backfill a slice with one HQL update");
            //~ var updated = sessionFactory.fromTransaction(session -> session.createMutationQuery(
                            //~ "update Movie m set m.staffPick = true where m.imdb.rating >= :rating and m.year >= 1900")
                    //~ .setParameter("rating", 8.8d)
                    //~ .executeUpdate());
            //~ row(updated + " documents now carry staffPick");
            //~
            //~ heading("5. Cleanup");
            //~ var cleared = sessionFactory.fromTransaction(session -> session.createMutationQuery(
                            //~ "update Movie m set m.staffPick = null where m.staffPick is not null")
                    //~ .executeUpdate());
            //~ row("reset " + cleared + " documents");
            //@REVEAL-END
        }
    }
}
