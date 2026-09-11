package com.mongodb.demo.model;

import com.mongodb.hibernate.annotations.ObjectIdGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.bson.types.ObjectId;

/** Maps {@code sample_mflix.comments}; {@code movie_id} references {@code movies._id}. */
@Entity(name = "Comment")
@Table(name = "comments")
public class Comment {

    @Id
    @ObjectIdGenerator
    private ObjectId id;

    private String name;
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    protected Comment() {}

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Movie getMovie() {
        return movie;
    }
}
