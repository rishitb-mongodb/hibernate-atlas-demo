package com.mongodb.demo.model;

import com.mongodb.hibernate.annotations.ObjectIdGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.bson.types.ObjectId;

/**
 * A narrow read model over the same {@code movies} collection as {@link Movie}.
 *
 * <p>Two entities may map the same collection, which is a useful pattern in its own right: a wide
 * write model and a flat projection for reads.
 *
 * <p>It also sidesteps a real alpha2 limitation. A native query hydrates entities by reading the
 * field names off the pipeline's trailing {@code $project}, and a {@code @Struct} field is asked
 * for by its leaf paths ({@code imdb.rating}), which {@code $project} cannot emit as flat keys. So
 * a native query cannot currently return an entity that has a {@code @Struct} field. This read
 * model has none.
 */
@Entity(name = "MovieSummary")
@Table(name = "movies")
public class MovieSummary {

    @Id
    @ObjectIdGenerator
    private ObjectId id;

    private String title;
    private Integer year;
    private Integer runtime;
    private String rated;

    protected MovieSummary() {
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

    @Override
    public String toString() {
        return "%s (%s) rated=%s runtime=%s".formatted(title, year, rated, runtime);
    }
}
