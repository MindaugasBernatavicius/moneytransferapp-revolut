# Money Transfer Application for Revolut Java Backend Engineer possition

## Explicit requirements
1. Java / Kotlin can be used as an implementation language.
2. Keep it simple and to the point (e.g. no need to implement any authentication).
3. Assume the API is invoked by multiple systems and services on behalf of end users.
4. You can use frameworks/libraries if you like (except Spring), but heavy frameworks.
5. The datastore should run in-memory for the sake of this test.
6. The final result should be executable as a standalone program (should not require a pre-installed container/server).
7. Demonstrate with tests that the API works as expected.

## Implicit requirements
1. The code produced by you is expected to be of high quality.
2. There are no detailed requirements, use common sense.
3. Please put your work on github or bitbucket.

## Launching
- `mvn clean install`
- `java -jar target/money-transfer-app-1.0-SNAPSHOT-jar-with-dependencies.jar`

## Testing
- `

## Implementation details

The following tools were used:
- Spark microframework with Java - far handling HTTP level.
- Junit5 - test running tests mechanisms and assertions.
- Gson - object serialization and deserialization from to JSON.
- Slf4j-simple - logging provider w/ java.util.logging implementation.
- Dependency injection was handled manually - no framework, but Guice would have been the choice otherwise.
- Data stored in-memory, primitive collections (explicit choice) - jOOQ or JDBI would have been the choice otherwise. 

The following project structure was chosen:
```
src
├───itest
│   ├───moneytransferapp  <-- integration functional tests and integration concurrency tests
│   └───resources         <-- integration test resources
├───main
│   └───moneytransferapp
│   │         ├───controller  <-- request handling responsibilies (request validation, json (de)serialization, response codes).
│   │         ├───model       <-- domain entities (account, transfer, user, etc.).
│   │         ├───repository  <-- data handling layer for (in-memory) persistence and OCC.
│   │         ├───service     <-- bussines logic (bussines logic validation and implementation using the underlying repositories).
|   |         └─────App.java  <-- initial loading and configuration of Spark.
│   └───resources
└───test
    └───moneytransferapp  <-- functional unit tests and (where appropriate) concurrency tests
    └───resources         <-- unit test resources (logging configuration for tests)
```

REST response structure
```
{
    "status": "SUCCESS",
    "data": \[ ... \]
}
```

General remarks
- 

What is still lacking
- 
