package com.mongodb.demo.model;

import jakarta.persistence.Embeddable;
import org.hibernate.annotations.Struct;

/** The {@code awards} sub-document of a movie. */
@Embeddable
@Struct(name = "Awards")
public record Awards(Integer wins, Integer nominations, String text) {}
