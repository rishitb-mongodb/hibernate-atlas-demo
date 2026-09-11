package com.mongodb.demo.model;

import com.mongodb.hibernate.annotations.ObjectIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import org.bson.types.ObjectId;
import org.hibernate.annotations.DynamicUpdate;

/** Maps {@code sample_mflix.movies}. Plain JPA, plus two embedded structs and three arrays. */
@Entity(name = "Movie")
@Table(name = "movies")
@DynamicUpdate // updates $set only the fields that changed, not the whole document
public class Movie {

    @Id
    @ObjectIdGenerator
    private ObjectId id;

    private String title;
    private Integer year;
    private String rated;
    private Integer runtime;

    @Column(name = "num_mflix_comments")
    private Integer numMflixComments;

    private Imdb imdb;
    private Awards awards;
    private List<String> genres;
    private List<String> directors;

    //@REVEAL-START
    //~ private Boolean staffPick;
    //@REVEAL-END

    protected Movie() {}

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

    public Integer getNumMflixComments() {
        return numMflixComments;
    }

    public Imdb getImdb() {
        return imdb;
    }

    public Awards getAwards() {
        return awards;
    }

    public List<String> getGenres() {
        return genres;
    }

    public List<String> getDirectors() {
        return directors;
    }

    //@REVEAL-START
    //~ public Boolean getStaffPick() {
        //~ return staffPick;
    //~ }
    //~
    //~ public Movie setStaffPick(Boolean staffPick) {
        //~ this.staffPick = staffPick;
        //~ return this;
    //~ }
    //@REVEAL-END

    @Override
    public String toString() {
        return "%s (%s) rated=%s runtime=%s imdb=%s awards=%s genres=%s directors=%s"
                .formatted(title, year, rated, runtime, imdb, awards, genres, directors);
    }
}
