# MongoDB Extension for Hibernate ORM — `sample_mflix` demo

A small, plain-Hibernate application that runs against the Atlas `sample_mflix` sample dataset
using the **MongoDB Extension for Hibernate ORM**, release `1.0.0-alpha2`.

It is built as five additive acts. Code for the later acts ships commented out and is revealed
during the talk, so the application grows in front of the audience.

## What it demonstrates

| Act | Feature |
| --- | ------- |
| 1 | Familiar Hibernate against MongoDB — `@Entity`, `Session`, HQL, and no MongoDB-specific code |
| 2 | The document model as a first-class mapping — embedded sub-documents (`@Struct`) and arrays |
| 3 | Schema flexibility — add a field with no `ALTER TABLE`, no migration, no downtime |
| 4 | The real query surface — HQL functions, `CASE`, `GROUP BY`, JOINs and the Criteria API, all translated to MQL |
| 5 | The escape hatch and the production details — native MQL pipelines, `@Version` optimistic locking, JPA-declared indexes |

## Requirements

- **Java 17+** (this project targets 21)
- **Maven 3.9+**
- An **Atlas cluster** (or any replica set) with the **`sample_mflix`** sample dataset loaded.
  Standalone deployments are not supported: the extension needs transactions.

The extension itself is the single dependency; it pulls in Hibernate ORM 7.4.5 and the MongoDB
Java driver transitively.

```xml
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-hibernate</artifactId>
    <version>1.0.0-alpha2</version>
</dependency>
```

## Setup

Point the demo at your cluster. The database name is added for you if the connection string
does not already carry one, so a string copied straight out of the Atlas UI works as-is.

```powershell
$env:MONGODB_URI = "mongodb+srv://<user>:<password>@<cluster>.mongodb.net/sample_mflix"
```

```bash
export MONGODB_URI='mongodb+srv://<user>:<password>@<cluster>.mongodb.net/sample_mflix'
```

Then run the pre-flight check:

```bash
mvn -q compile exec:java -Dexec.args=verify
```

## Running the acts

```bash
mvn -q compile exec:java -Dexec.args=1
mvn -q compile exec:java -Dexec.args=2
mvn -q compile exec:java -Dexec.args=3
mvn -q compile exec:java -Dexec.args=4
mvn -q compile exec:java -Dexec.args=5
```

Acts 2 and 3 do nothing until their code is revealed. Reveal it by deleting the `//~` prefixes
inside the `//@ACT2-…` / `//@ACT3-…` blocks, or let the helper do it:

```powershell
.\reveal.ps1 2        # uncomment act 2
.\reveal.ps1 3        # uncomment act 3
.\reveal.ps1 reset    # put both back
```

```bash
./reveal 2
./reveal 3
./reveal reset
```

## Configuration, in full

The entire MongoDB configuration is two settings (see `Persistence.java`):

```properties
jakarta.persistence.jdbc.url=mongodb+srv://.../sample_mflix
com.mongodb.hibernate.semantics.nulls=MQL
```

There is no `hibernate.dialect` and no JDBC driver to name: both the dialect and the connection
provider are inferred from the `mongodb://` scheme. `semantics.nulls=MQL` is required, and is an
explicit acknowledgement that null comparisons follow MongoDB's semantics rather than SQL's
three-valued logic.

## What this demo touches on your cluster

- `movies` and `comments` are **read only**, except in act 3, which sets a `staffPick` field on a
  small number of documents and clears it again.
- `hibernate_watchlist` is **created, written and dropped again** by act 5. It is the only entity
  with schema generation enabled, and it lives in its own persistence unit, so the sample
  collections can never be altered or dropped.

To put everything back:

```bash
mvn -q compile exec:java -Dexec.args=reset-data
```

## A note on the sample data

`sample_mflix` is genuinely heterogeneous: a handful of movie documents store `year` or
`imdb.rating` as a string rather than a number. Reading one of those into a typed entity field
fails, so the queries here constrain `year` numerically (`m.year >= 1900`), which excludes them.
`verify` reports exactly how many such documents your copy has. This is a fair thing to show
rather than hide — it is what a flexible schema actually looks like, and how you put a typed view
over it.

## Known alpha2 boundaries worth knowing before you present

- **Aggregate accumulators are not supported yet.** `GROUP BY` and `HAVING` work over columns and
  expressions, but `count()`, `sum()` and `avg()` are the next piece of that work. Do not reach
  for them.
- `SELECT DISTINCT` is not supported.
- `RIGHT`/`FULL`/`CROSS` joins, lateral joins, subquery joins and the `FROM A a, B b` join syntax
  are not supported. `INNER` and `LEFT OUTER` are.
- Atlas Search, Vector Search and geospatial queries have no HQL spelling — they go through
  `createNativeQuery`, which act 5 demonstrates.
- **A native query must end in a `$project`**, and its keys must match the fields of the entity
  being hydrated. As a consequence a native query **cannot return an entity that has a `@Struct`
  field**: those are asked for by leaf path (`imdb.rating`), which `$project` cannot emit as a
  flat key. Act 5 hydrates the flat `MovieSummary` read model for exactly this reason.
- **Schema generation must be `create` / `create-drop`, not `update`.** `update` has to diff
  against the existing schema, and the JDBC adapter does not implement `getTables` metadata.
- `@CurrentTimestamp(source = DB)` is rejected at bootstrap. `@Version` over `int`, `long` or
  `Instant` works.

## Links

- Release: <https://github.com/mongodb/mongo-hibernate/releases/tag/r1.0.0-alpha2>
- Docs: <https://www.mongodb.com/docs/languages/java/mongodb-hibernate/current/>
- Repository: <https://github.com/mongodb/mongo-hibernate>
