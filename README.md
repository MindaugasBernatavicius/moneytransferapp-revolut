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

## Implementation details

The following tools were used:
- Spark microframework with Java - far handling HTTP level.
- Junit5 - test running tests mechanisms and assertions.
- Gson - object serialization and deserialization from to JSON.

The following project structure was chosen:
```
├───src
│   ├───itest
│   │   ├───java      <-- integration functional tests and integration concurrency tests
│   │   └───resources <-- test resources
│   ├───main
│   │   └───moneytransferapp
│   │   │         ├───controller
│   │   │         ├───model
│   │   │         ├───repository
│   │   │         └───service
│   │   └───resources
│   └───test
│       └───moneytransferapp
│       │               ├───repository
│       │               └───service
│       └───resources

```
