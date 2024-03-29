# Money Transfer Application for Revolut

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
- `mvn surefire:test` - run unit tests
- `mvn failsafe:integration-test` - run integration tests

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

## REST 
Response structure:
```
{
    "status": "SUCCESS",
    "data": \[ ... \]
}
```
Endpoints:

| Verb / URL                        | Usage               |
| ----------------------------------|---------------------|
| GET  /api/v1/accounts             | Obtain all accounts |
| GET  /api/v1/accounts/{accountId} | Get account by id   |
| POST /api/v1/accounts             | Create a new empty account |
| PUT  /api/v1/accounts/{accountId}<br> {"balance": 55} | Change account |
| GET  /api/v1/transfers            | Obtain all transfers |
| GET  /api/v1/transfers/{id}       | Get transfer by id  |
| POST /transfers <br>{ "from": 1, "to":2, "amount":500.00 } | Transfer given amount of money from account to other

## General remarks
Optimistic locking and thread safety
- Account and transaction creation are sychronised using default java monitori locking at the service layer:
```public synchronized void createTransfer(int benefactorId, int beneficiaryId, BigDecimal amount)```
```public synchronized int createAccount()```
- Sychronisation for account and transaction creation is tested by spawning a lot of threads - core_count * 3 to increase thread interleaving probability. Removing the `synchronized` keyword from the method signatures would show incorrect behavior (for example dublicated id fields for accounts).
- Regarding optimistic locking: I wanted to experiment and implement Optimistic Locking around primitive non-thread-safe Java Collections. The rationale was that not adding a database layer would be an interesting challenge also it would comply to the requirement in the task "no heavy frameworks". I was able to immitate a condition where an entity having a version field was updated by another thread before it persisted by the first one using this implementation. So a simple version of optimistic locking is implemented.
```
    @Override
    public void update(Account account) throws OptimisticLockException, AccountNotFoundException {
        boolean found = false;
        for (int i = 0; i < accounts.size(); i++){
            var tempAccount = accounts.get(i);
            var version = tempAccount.getVersion();
            if(tempAccount.getId().equals(account.getId())){
                found = true;
                var accountToBePersisted = new Account(account.getId(), account.getBalance(), ++version);
                // imitating database transaction w/ OCC
                synchronized (this){
                    if (accounts.get(i).getVersion().equals(tempAccount.getVersion()))
                        accounts.set(i, accountToBePersisted);
                    else throw new OptimisticLockException();
                }
            }
        }
        if(!found)  throw new AccountNotFoundException();
    }
 ```
 - I did not, however, succeed in implementing transactions in pure Java (see section: "What is still lacking"). So for example if account to is updated using account update (PUT /account) action while transfer is happening optimistic locking will reject the update, but the transfer transaction would not be fully reverted.

What is still lacking
- Logging
- Transaction mechanism in the service layer for transfers does not handle all cases (when transfer to the second account fails due to OptimisticLockingException the transfer to the first account is not reverted leading to possible inconsistency).
