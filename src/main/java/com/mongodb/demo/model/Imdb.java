package com.mongodb.demo.model;

import jakarta.persistence.Embeddable;
import org.hibernate.annotations.Struct;

/**
 * The {@code imdb} sub-document of a movie.
 *
 * <p>{@code @Struct} tells Hibernate to store this embeddable as a nested BSON document rather than
 * flattening it into prefixed columns. A Java record is enough.
 *
 * <p>{@code rating} and {@code votes} are boxed on purpose: a handful of documents in the sample
 * data omit them, and a boxed type reads that back as {@code null} instead of failing.
 */
@Embeddable
@Struct(name = "Imdb")
public record Imdb(Double rating, Integer votes, Integer id) {}
