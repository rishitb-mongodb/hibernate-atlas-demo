package com.mongodb.demo.model;

import com.mongodb.hibernate.annotations.ObjectIdGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.bson.types.ObjectId;

/**
 * Maps the {@code sample_mflix.comments} collection, whose {@code movie_id} field references
 * {@code movies._id}.
 *
 * <p>This is the shape a relational codebase arrives with, and the reason JOIN support exists:
 * porting the existing HQL costs nothing. For a MongoDB-native design you would embed the
 * comments in the movie document instead.
 */
@Entity(name = "Comment")
@Table(name = "comments")
public class Comment {

    @Id
    @ObjectIdGenerator
    private ObjectId id;

    private String name;
    private String email;
    private String text;
    private Instant date;

    /** A plain JPA many-to-one over a MongoDB reference field. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    protected Comment() {
        // required by Hibernate
    }

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getText() {
        return text;
    }

    public Instant getDate() {
        return date;
    }

    public Movie getMovie() {
        return movie;
    }

    @Override
    public String toString() {
        var snippet = text == null ? "" : text.substring(0, Math.min(60, text.length()));
        return "%s <%s> on %s: %s...".formatted(name, email, date, snippet);
    }
}
