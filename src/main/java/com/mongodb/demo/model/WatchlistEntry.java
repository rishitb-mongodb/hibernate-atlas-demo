package com.mongodb.demo.model;

import com.mongodb.hibernate.annotations.ObjectIdGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import org.bson.types.ObjectId;

/**
 * A collection this demo owns, written to on every run: {@code sample_mflix.hibernate_watchlist}.
 *
 * <p>Kept separate from the sample collections so that schema generation and concurrent writes
 * never touch {@code movies} or {@code comments}.
 *
 * <p>The index and the unique constraint are ordinary JPA declarations. Hibernate's schema
 * generation turns them into {@code createIndexes} commands against MongoDB.
 */
@Entity(name = "WatchlistEntry")
@Table(
        name = "hibernate_watchlist",
        indexes = @Index(name = "idx_watchlist_user", columnList = "userEmail"),
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_watchlist_user_movie",
                        columnNames = {"userEmail", "movieTitle"}))
public class WatchlistEntry {

    @Id
    @ObjectIdGenerator
    private ObjectId id;

    private String userEmail;
    private String movieTitle;
    private Integer priority;

    /**
     * Optimistic locking. Hibernate adds the expected version to the update filter and checks the
     * matched count, so a lost update raises {@code OptimisticLockException} instead of silently
     * overwriting. {@code int}, {@code long} and {@code Instant} are all supported here.
     */
    @Version
    private long version;

    protected WatchlistEntry() {
        // required by Hibernate
    }

    public WatchlistEntry(String userEmail, String movieTitle, Integer priority) {
        this.userEmail = userEmail;
        this.movieTitle = movieTitle;
        this.priority = priority;
    }

    public ObjectId getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public Integer getPriority() {
        return priority;
    }

    public WatchlistEntry setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "%s -> %s (priority=%s, version=%d)".formatted(userEmail, movieTitle, priority, version);
    }
}
