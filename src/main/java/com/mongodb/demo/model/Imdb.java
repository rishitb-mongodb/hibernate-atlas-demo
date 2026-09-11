package com.mongodb.demo.model;

import jakarta.persistence.Embeddable;
import org.hibernate.annotations.Struct;

/** The {@code imdb} sub-document, stored as a nested BSON document rather than flattened columns. */
@Embeddable
@Struct(name = "Imdb")
public record Imdb(Double rating, Integer votes) {}
