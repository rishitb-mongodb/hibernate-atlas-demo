# MongoDB Extension for Hibernate ORM — `sample_mflix` demo

A small Hibernate application, built for a **10-minute live demo**, that runs against the Atlas
`sample_mflix` sample dataset using the **MongoDB Extension for Hibernate ORM**, release
`1.0.0-alpha2`.

Three acts:

| Act | Shows |
| --- | --- |
| 1 | Familiar Hibernate (HQL, `find()`) + embedded structs (`@Struct`) and arrays as first-class mappings |
| 2 | Schema flexibility — add a field with no migration, no downtime. Code ships commented out and is revealed live. |
| 3 | JOINs across two collections, translated to `$lookup` |

The narration lives in [`DEMO_SCRIPT.md`](DEMO_SCRIPT.md) (gitignored — it's the presenter's
copy) and in the console output the acts print, not in code comments. The code is meant to be read
on a screen in a few seconds.

## Requirements

- **Java 17+** (this project targets 21), **Maven 3.9+**
- An **Atlas cluster** (or any replica set) with the **`sample_mflix`** sample dataset loaded.
  Standalone deployments are not supported: the extension needs transactions.

The extension is the only dependency; it pulls in Hibernate ORM 7.4.5 and the MongoDB Java driver.

```xml
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-hibernate</artifactId>
    <version>1.0.0-alpha2</version>
</dependency>
```

## Setup

```powershell
$env:MONGODB_URI = "mongodb+srv://<user>:<password>@<cluster>.mongodb.net/sample_mflix"
```

```bash
export MONGODB_URI='mongodb+srv://<user>:<password>@<cluster>.mongodb.net/sample_mflix'
```

The database name is spliced in for you if the connection string doesn't already have one.

```bash
mvn -q compile exec:java -Dexec.args=verify
```

## Running

```bash
mvn -q compile exec:java -Dexec.args=1
mvn -q compile exec:java -Dexec.args=2   # prints nothing until revealed
mvn -q compile exec:java -Dexec.args=3
```

Act 2 does nothing until its code is revealed:

```bash
./reveal show      # ./reveal.ps1 show on Windows
./reveal hide       # ./reveal.ps1 hide
```

Clean up any leftover `staffPick` fields with:

```bash
mvn -q compile exec:java -Dexec.args=reset-data
```

## Configuration, in full

The entire MongoDB configuration is two settings (`Persistence.java`):

```properties
jakarta.persistence.jdbc.url=mongodb+srv://.../sample_mflix
com.mongodb.hibernate.semantics.nulls=MQL
```

No `hibernate.dialect`, no JDBC driver — both are inferred from the `mongodb://` scheme.
`semantics.nulls=MQL` is required: it's an explicit acknowledgement that null comparisons follow
MongoDB's semantics rather than SQL's three-valued logic.

## What this touches on your cluster

`movies` and `comments` are read-only, except Act 2, which sets `staffPick` on a small number of
documents and clears it again at the end of its own run. `reset-data` clears any that are left
over from an interrupted run.

## A note on the sample data

`sample_mflix` is genuinely heterogeneous: a handful of documents store `year` or `imdb.rating` as
a string rather than a number, which fails a typed entity read. Every query here guards
`m.year >= 1900` to exclude them; `verify` reports how many your copy has.

## Known alpha2 boundaries worth knowing before you present

- Aggregate accumulators (`count`, `sum`, `avg`) are not supported yet. `GROUP BY`/`HAVING` over
  columns and expressions is.
- `RIGHT`/`FULL`/`CROSS` joins, lateral joins, and subquery joins are not supported. `INNER` and
  `LEFT OUTER` are.
- A native query must end in a `$project`, and can't hydrate an entity with a `@Struct` field.
- `hbm2ddl` schema generation must be `create`/`create-drop`, not `update`.

## Links

- Release: <https://github.com/mongodb/mongo-hibernate/releases/tag/r1.0.0-alpha2>
- Docs: <https://www.mongodb.com/docs/languages/java/mongodb-hibernate/current/>
- Repository: <https://github.com/mongodb/mongo-hibernate>
