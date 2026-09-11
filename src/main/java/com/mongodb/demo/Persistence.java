package com.mongodb.demo;

import com.mongodb.ConnectionString;
import com.mongodb.demo.model.Comment;
import com.mongodb.demo.model.Movie;
import com.mongodb.demo.model.MovieSummary;
import com.mongodb.demo.model.WatchlistEntry;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

/**
 * Bootstraps Hibernate against MongoDB Atlas.
 *
 * <p>The whole integration is two settings:
 *
 * <ul>
 *   <li>{@code jakarta.persistence.jdbc.url} -- a MongoDB connection string. The extension infers
 *       the dialect and the connection provider from the {@code mongodb://} scheme, so there is no
 *       {@code hibernate.dialect} and no JDBC driver to name.
 *   <li>{@code com.mongodb.hibernate.semantics.nulls=MQL} -- required, and must be {@code MQL}. It
 *       is an explicit acknowledgement that null comparisons follow MongoDB's semantics rather than
 *       SQL's three-valued logic.
 * </ul>
 */
public final class Persistence {

    public static final String DATABASE = "sample_mflix";
    public static final String URI_ENV_VAR = "MONGODB_URI";

    private Persistence() {}

    /** Read access to the sample collections. Schema generation is off: nothing is created or dropped. */
    public static SessionFactory sampleMflix() {
        return build("none", Movie.class, MovieSummary.class, Comment.class);
    }

    /**
     * The collection this demo owns. Schema generation is on so that the JPA {@code @Index} and
     * {@code @UniqueConstraint} declarations are actually built in MongoDB.
     *
     * <p>{@code create-drop} rather than {@code update}: {@code update} has to diff against the
     * existing schema, and the JDBC adapter does not implement {@code getTables} metadata. It only
     * ever applies to {@code hibernate_watchlist}, because that is the only entity in this unit --
     * the sample collections are bootstrapped separately, with schema generation off.
     */
    public static SessionFactory watchlist() {
        return build("create-drop", WatchlistEntry.class);
    }

    private static SessionFactory build(String schemaAction, Class<?>... annotatedClasses) {
        var registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.JAKARTA_JDBC_URL, connectionString())
                .applySetting("com.mongodb.hibernate.semantics.nulls", "MQL")
                .applySetting(AvailableSettings.JAKARTA_HBM2DDL_DATABASE_ACTION, schemaAction)
                .applySetting(AvailableSettings.HBM2DDL_HALT_ON_ERROR, "true")
                // The MQL that Hibernate sends is printed by the org.hibernate.SQL logger at
                // DEBUG (see logback.xml). show_sql would print a second, duplicate copy.
                .applySetting(AvailableSettings.SHOW_SQL, "false")
                .applySetting(AvailableSettings.WRAPPER_ARRAY_HANDLING, "allow")
                .build();
        try {
            var sources = new MetadataSources(registry);
            for (var annotatedClass : annotatedClasses) {
                sources.addAnnotatedClass(annotatedClass);
            }
            return sources.buildMetadata().buildSessionFactory();
        } catch (RuntimeException e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw e;
        }
    }

    /**
     * Reads {@value #URI_ENV_VAR} and makes sure it names the {@value #DATABASE} database, so that
     * a connection string copied straight out of the Atlas UI works as-is.
     */
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
        var parsed = new ConnectionString(uri);
        if (DATABASE.equals(parsed.getDatabase())) {
            return uri;
        }
        // Splice the database name into the path, keeping any query string intact.
        var schemeEnd = uri.indexOf("://") + 3;
        var queryStart = uri.indexOf('?', schemeEnd);
        var beforeQuery = queryStart < 0 ? uri : uri.substring(0, queryStart);
        var query = queryStart < 0 ? "" : uri.substring(queryStart);
        var hostEnd = beforeQuery.indexOf('/', schemeEnd);
        var hosts = hostEnd < 0 ? beforeQuery : beforeQuery.substring(0, hostEnd);
        return hosts + "/" + DATABASE + query;
    }
}
