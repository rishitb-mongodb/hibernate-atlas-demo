package com.mongodb.demo.acts;

import static com.mongodb.demo.Mql.heading;
import static com.mongodb.demo.Mql.note;
import static com.mongodb.demo.Mql.row;

import com.mongodb.demo.Persistence;
import com.mongodb.demo.model.Movie;

/**
 * ACT 3 -- Schema flexibility.
 *
 * <p>Reveal the {@code ACT3} blocks in {@link Movie} and in this file.
 *
 * <p>This is the act that has no relational equivalent. Adding {@code staffPick} to the entity is
 * the entire change: no {@code ALTER TABLE}, no migration script, no backfill, no downtime, and no
 * coordination with whoever owns the schema. Documents that predate the field simply read back
 * {@code null}.
 */
public final class Act3 {

    private Act3() {}

    private static final String TITLE = "The Shawshank Redemption";

    public static void run() {
        try (var sessionFactory = Persistence.sampleMflix()) {

            //@ACT3-START
            //~ heading("3a. The field does not exist in the data yet -- it reads back as null");
            //~ var before = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            //~ "from Movie m where m.title = :title and m.year >= 1900", Movie.class)
                    //~ .setParameter("title", TITLE)
                    //~ .setMaxResults(1)
                    //~ .getSingleResultOrNull());
            //~ row(before.getTitle() + " staffPick = " + before.getStaffPick());
            //~ note("No migration ran. The field is absent from the document, so it is null.");
            //~
            //~ heading("3b. Write it on one entity -- only that document changes");
            //~ sessionFactory.inTransaction(session -> {
                //~ var movie = session.find(Movie.class, before.getId());
                //~ movie.setStaffPick(true);
            //~ });
            //~ note("A dirty-checked $set on a single field. Every other document is untouched,");
            //~ note("and no reader needed to be redeployed to tolerate the new shape.");
            //~
            //~ heading("3c. Query on the brand-new field immediately");
            //~ var picks = sessionFactory.fromTransaction(session -> session.createSelectionQuery(
                            //~ "from Movie m where m.staffPick = true and m.year >= 1900", Movie.class)
                    //~ .getResultList());
            //~ picks.forEach(m -> row(m.getTitle() + " staffPick = " + m.getStaffPick()));
            //~
            //~ heading("3d. Backfill a whole slice with one bulk HQL update");
            //~ var updated = sessionFactory.fromTransaction(session -> session.createMutationQuery(
                            //~ """
                            //~ update Movie m
                            //~ set m.staffPick = true
                            //~ where m.imdb.rating >= :rating and m.imdb.votes >= :votes and m.year >= 1900
                            //~ """)
                    //~ .setParameter("rating", 8.8d)
                    //~ .setParameter("votes", 1_000_000)
                    //~ .executeUpdate());
            //~ row(updated + " documents now carry staffPick");
            //~ note("One updateMany. On a relational database this is a migration ticket.");
            //~
            //~ heading("3e. Cleaning up after the demo");
            //~ var cleared = sessionFactory.fromTransaction(session -> session.createMutationQuery(
                            //~ "update Movie m set m.staffPick = null where m.staffPick is not null")
                    //~ .executeUpdate());
            //~ row("reset " + cleared + " documents");
            //~ note("This sets the field to null rather than removing it. A true $unset is a native");
            //~ note("query -- see Act 5 for that escape hatch. 'reset-data' does it for you.");
            //@ACT3-END
        }
    }
}
