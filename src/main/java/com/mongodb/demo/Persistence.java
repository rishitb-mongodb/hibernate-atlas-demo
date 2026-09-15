package com.mongodb.demo;

import com.mongodb.ConnectionString;
import com.mongodb.demo.model.Comment;
import com.mongodb.demo.model.Movie;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

/**
 * Bootstraps Hibernate against MongoDB Atlas. The whole integration is two settings:
 * {@code jakarta.persistence.jdbc.url} (a connection string -- dialect and connection provider are
 * inferred from the {@code mongodb://} scheme) and {@code com.mongodb.hibernate.semantics.nulls=MQL}
 * (required; null comparisons follow MongoDB's rules, not SQL's).
 */
public final class Persistence {

    public static final String DATABASE = "sample_mflix";
    public static final String URI_ENV_VAR = "MONGODB_URI";

    private Persistence() {}

    /**
     * A {@link SessionFactory} over the sample collections, with schema generation off: nothing is
     * created, altered, or dropped at bootstrap. That's unrelated to whether the app can write data
     * -- Act 2's HQL updates against {@code Movie} go through this same factory.
     */
    public static SessionFactory sampleMflix() {
        var registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.JAKARTA_JDBC_URL, connectionString())
                .applySetting("com.mongodb.hibernate.semantics.nulls", "MQL")
                .applySetting(AvailableSettings.JAKARTA_HBM2DDL_DATABASE_ACTION, "none")
                .build();
        try {
            var sources = new MetadataSources(registry);
            sources.addAnnotatedClass(Movie.class);
            sources.addAnnotatedClass(Comment.class);
            return sources.buildMetadata().buildSessionFactory();
        } catch (RuntimeException e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw e;
        }
    }

    /** Reads {@value #URI_ENV_VAR} and splices in {@value #DATABASE} if the URI doesn't name a database. */
    public static String connectionString() {
        var raw = System.getenv(URI_ENV_VAR);
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException(
                    """
                    %s is not set.

                    PowerShell:  $env:%s = "mongodb+srv://<user>:<password>@<cluster>/%s"
                    bash:        export %s='mongodb+srv://<user>:<password>@<cluster>/%s'
                    """
                            .formatted(URI_ENV_VAR, URI_ENV_VAR, DATABASE, URI_ENV_VAR, DATABASE));
        }
        var uri = raw.trim();
        if (DATABASE.equals(new ConnectionString(uri).getDatabase())) {
            return uri;
        }
        var schemeEnd = uri.indexOf("://") + 3;
        var queryStart = uri.indexOf('?', schemeEnd);
        var beforeQuery = queryStart < 0 ? uri : uri.substring(0, queryStart);
        var query = queryStart < 0 ? "" : uri.substring(queryStart);
        var hostEnd = beforeQuery.indexOf('/', schemeEnd);
        var hosts = hostEnd < 0 ? beforeQuery : beforeQuery.substring(0, hostEnd);
        return hosts + "/" + DATABASE + query;
    }
}
