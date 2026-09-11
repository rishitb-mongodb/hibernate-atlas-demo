package com.mongodb.demo.model;

import com.mongodb.hibernate.annotations.ObjectIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import org.bson.types.ObjectId;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Maps the {@code sample_mflix.movies} collection.
 *
 * <p>Nothing here is MongoDB-specific except {@link ObjectId} and {@link ObjectIdGenerator} -- the
 * rest is plain JPA. There is no schema, no DDL and no migration behind it.
 *
 * <p>Lines prefixed {@code //~} are revealed during the demo. Either delete the prefix by hand or
 * run {@code reveal 2} / {@code reveal 3}.
 */
@Entity(name = "Movie")
@Table(name = "movies")
// Without this, Hibernate writes every mapped field on update. With it, the $set carries only
// the fields that actually changed -- which is what you want against a document store.
@DynamicUpdate
public class Movie {

    /** {@code @Column(name = "_id")} is implied for the identifier, so it is not written out. */
    @Id
    @ObjectIdGenerator
    private ObjectId id;

    private String title;
    private Integer year;
    private String rated;
    private Integer runtime;
    private String plot;

    /** A BSON date maps to {@code java.time.Instant}. */
    private Instant released;

    /** Hibernate uses the Java property name as the field name, so map this one explicitly. */
    @Column(name = "num_mflix_comments")
    private Integer numMflixComments;

    //@ACT2-START -- the document model is first class
    //~ /** A BSON sub-document. One document, one read: no join, no second table. */
    //~ private Imdb imdb;
    //~
    //~ private Awards awards;
    //~
    //~ /** BSON arrays map straight onto Java collections. No join table, no @ElementCollection. */
    //~ private List<String> genres;
    //~
    //~ private List<String> directors;
    //~
    //~ private List<String> countries;
    //@ACT2-END

    //@ACT3-START -- schema flexibility: one new field, no ALTER TABLE, no migration, no downtime
    //~ private Boolean staffPick;
    //@ACT3-END

    protected Movie() {
        // required by Hibernate
    }

    public ObjectId getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Integer getYear() {
        return year;
    }

    public String getRated() {
        return rated;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public String getPlot() {
        return plot;
    }

    public Instant getReleased() {
        return released;
    }

    public Integer getNumMflixComments() {
        return numMflixComments;
    }

    //@ACT2-START -- accessors for the fields above
    //~ public Imdb getImdb() {
        //~ return imdb;
    //~ }
    //~
    //~ public Awards getAwards() {
        //~ return awards;
    //~ }
    //~
    //~ public List<String> getGenres() {
        //~ return genres;
    //~ }
    //~
    //~ public List<String> getDirectors() {
        //~ return directors;
    //~ }
    //~
    //~ public List<String> getCountries() {
        //~ return countries;
    //~ }
    //@ACT2-END

    //@ACT3-START -- accessors for staffPick
    //~ public Boolean getStaffPick() {
        //~ return staffPick;
    //~ }
    //~
    //~ public Movie setStaffPick(Boolean staffPick) {
        //~ this.staffPick = staffPick;
        //~ return this;
    //~ }
    //@ACT3-END

    @Override
    public String toString() {
        return "%s (%s) rated=%s runtime=%s comments=%s".formatted(title, year, rated, runtime, numMflixComments);
    }
}
