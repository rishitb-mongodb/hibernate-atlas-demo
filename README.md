# MongoDB Extension for Hibernate ORM — `sample_mflix` demo

A small Hibernate application that runs against the Atlas `sample_mflix` sample dataset using the
[MongoDB Extension for Hibernate ORM](https://github.com/mongodb/mongo-hibernate), release
`1.0.0-alpha2`. It's built as three short, self-contained acts, originally for a 10-minute
conference demo — that structure also makes it a decent guided tour if you're evaluating the
extension on your own: each act runs independently and prints the MQL it generates, so you can see
exactly what Hibernate is doing under the hood.

> **Status:** independent, unofficial companion code — not MongoDB's official documentation.
> It targets a **pre-GA alpha release** (`1.0.0-alpha2`); check the
> [releases page](https://github.com/mongodb/mongo-hibernate/releases) for the current version,
> since behavior may change before GA.

| Act | Shows |
| --- | --- |
| 1 | Familiar Hibernate (HQL, `find()`) + embedded structs (`@Struct`) and arrays as first-class mappings |
| 2 | Schema flexibility — add a field with no migration, no downtime. Ships commented out, revealed with one command. |
| 3 | JOINs across two collections, translated to `$lookup` |

The narration lives in the console output the acts print, not in code comments — the code is meant
to be read in a few seconds. (A separate, more detailed presenter script exists locally but isn't
part of this repo.)

## Requirements

- **Java 17+** (this project targets 21), **Maven 3.9+**
- An **Atlas cluster** (or any MongoDB replica set — standalone deployments aren't supported, since
  the extension needs transactions) with the **`sample_mflix`** sample dataset loaded. If you don't
  have it yet, Atlas can load it for you in a couple of clicks — see
  [Load Sample Data](https://www.mongodb.com/docs/atlas/sample-data/).

The extension is the only dependency; it pulls in Hibernate ORM 7.4.5 and the MongoDB Java driver.

```xml
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-hibernate</artifactId>
    <version>1.0.0-alpha2</version>
</dependency>
```

## Setup

Set `MONGODB_URI` to a connection string for your cluster (from Atlas: **Connect → Drivers**):

```powershell
$env:MONGODB_URI = "mongodb+srv://<user>:<password>@<cluster>.mongodb.net/sample_mflix"
```

```bash
export MONGODB_URI='mongodb+srv://<user>:<password>@<cluster>.mongodb.net/sample_mflix'
```

The database name is spliced in for you if the connection string doesn't already have one.

Then check the connection and the dataset:

```bash
mvn -q compile exec:java "-Dexec.args=verify"
```

## Running

```bash
mvn -q compile exec:java "-Dexec.args=1"
mvn -q compile exec:java "-Dexec.args=2"   # prints nothing until revealed, see below
mvn -q compile exec:java "-Dexec.args=3"
```

Act 2 ships with its code commented out and does nothing until it's revealed:

```bash
./reveal show
./reveal hide
```

```powershell
.\reveal.cmd show
.\reveal.cmd hide
```

`reveal.cmd` runs `reveal.ps1` with `-ExecutionPolicy Bypass`, so it works from PowerShell or
`cmd.exe` without touching your system's script-execution policy. Don't double-click `reveal.ps1`
directly — Windows opens `.ps1` files in a text editor rather than running them.

Clean up any leftover `staffPick` fields left by an interrupted Act 2 run with:

```bash
mvn -q compile exec:java "-Dexec.args=reset-data"
```

## Configuration, in full

The entire MongoDB configuration is two settings (see `Persistence.java`):

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
over from an interrupted run. Nothing else is created, modified, or dropped.

## A note on the sample data

`sample_mflix` is genuinely heterogeneous: a handful of documents store `year` or `imdb.rating` as
a string rather than a number, which fails a typed entity read. Every query here guards
`m.year >= 1900` to exclude them; `verify` reports how many documents in your copy of the dataset
are affected.

## Known limitations in `1.0.0-alpha2`

- Aggregate accumulators (`count`, `sum`, `avg`) are not supported yet. `GROUP BY`/`HAVING` over
  columns and expressions is.
- `RIGHT`/`FULL`/`CROSS` joins, lateral joins, and subquery joins are not supported. `INNER` and
  `LEFT OUTER` are.
- A native query must end in a `$project`, and can't hydrate an entity with a `@Struct` field.
- `hbm2ddl` schema generation must be `create`/`create-drop`, not `update`.

These were current as of `1.0.0-alpha2` — check the extension's own
[release notes](https://github.com/mongodb/mongo-hibernate/releases) for what's changed since.

## License

The code in this repository is licensed under the [Apache License 2.0](LICENSE), the same license
as the MongoDB Extension for Hibernate ORM itself.

## Links

- Release: <https://github.com/mongodb/mongo-hibernate/releases/tag/r1.0.0-alpha2>
- Docs: <https://www.mongodb.com/docs/languages/java/mongodb-hibernate/current/>
- Repository: <https://github.com/mongodb/mongo-hibernate>
