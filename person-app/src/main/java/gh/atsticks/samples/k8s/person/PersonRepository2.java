//package gh.atsticks.samples.k8s.person;
//
//
//import io.vertx.core.AbstractVerticle;
//import io.vertx.core.AsyncResult;
//import io.vertx.core.Future;
//import io.vertx.core.Handler;
//import io.vertx.core.eventbus.Message;
//import io.vertx.core.json.Json;
//import io.vertx.core.json.JsonArray;
//import io.vertx.ext.jdbc.JDBCClient;
//import io.vertx.ext.sql.SQLConnection;
//import io.vertx.ext.sql.UpdateResult;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//
//public class PersonRepository2 extends AbstractVerticle{
//
//    static final String GET_COUNT = "Person.count() -> Integer";
//    static final String LIST = "Person.getAll(int[] fromTo) -> List<Person>";
//    static final String STORE = "Person.update(Person person)";
//    static final String DELETE = "Person.delete(String id)";
//    static final String GET = "Person.get(String id) -> Person";
//
//    private JDBCClient db;
//
//
//    @Override
//    public void start(Future<Void> fut) {
//
//        // Create a JDBC client
//        db = JDBCClient.createShared(vertx, config(), "Persons");
//
//        startBackend(
//                (connection) -> createData(connection,
//                        (nothing) -> completeStartup(fut))
//                );
//    }
//
//    private void createData(AsyncResult<SQLConnection> result, Handler<AsyncResult<Void>> next, Future<Void> fut) {
//        if (result.failed()) {
//            fut.fail(result.cause());
//        } else {
//            SQLConnection connection = result.result();
//            connection.execute(
//                    "CREATE TABLE IF NOT EXISTS Person (" +
//                            "id varchar(40), " +
//                            "name varchar(100), " +
//                            "description varchar(256)," +
//                            "imageUrl varchar(512))",
//                    ar -> {
//                        if (ar.failed()) {
//                            fut.fail(ar.cause());
//                            connection.close();
//                            return;
//                        }
//                        connection.query("SELECT * FROM Person", select -> {
//                            if (select.failed()) {
//                                fut.fail(ar.cause());
//                                connection.close();
//                                return;
//                            }
//                            if (select.result().getNumRows() == 0) {
//                                insert(
//                                        new Person("Bowmore 15 Years Laimrig", "Scotland, Islay"), connection,
//                                        (v) -> insert(new Person("Talisker 57° North", "Scotland, Island"), connection,
//                                                (r) -> {
//                                                    next.handle(Future.<Void>succeededFuture());
//                                                    connection.close();
//                                                }));
//                            } else {
//                                next.handle(Future.<Void>succeededFuture());
//                                connection.close();
//                            }
//                        });
//
//                    });
//        }
//    }
//
//    private void insert(Person person, SQLConnection connection, Handler<AsyncResult<Person>> next) {
//        String sql = "INSERT INTO Person (id, name, description, imageUrl) VALUES ?, ?, ?, ?";
//        connection.updateWithParams(sql,
//                new JsonArray().add(person.getId()).add(person.getName())
//                        .add(person.getDescription()).add(person.getImageUrl()),
//                (ar) -> {
//                    if (ar.failed()) {
//                        next.handle(Future.failedFuture(ar.cause()));
//                        connection.close();
//                        return;
//                    }
//                    UpdateResult result = ar.result();
//                    // Build a new whisky instance with the generated id.
//                    Person w = new Person(result.getKeys().getString(0), person.getName(), person.getDescription(), person.getImageUrl());
//                    next.handle(Future.succeededFuture(w));
//                });
//    }
//
//
//    private void startBackend(Handler<AsyncResult<SQLConnection>> next, Future<Void> fut) {
//        db.getConnection(ar -> {
//            if (ar.failed()) {
//                fut.fail(ar.cause());
//            } else {
//                next.handle(Future.succeededFuture(ar.result()));
//            }
//        });
//    }
//
//    @Override
//    public void stop() throws Exception {
//        // Close the JDBC client.
//        db.close();
//    }
//
//
//    private void getCount(Message<Void> countRequest) {
//        db.getConnection(ar -> {
//            SQLConnection connection = ar.result();
//            connection.query("SELECT COUNT(p.id) FROM Person p", result -> {
//                int count = result.result().getRows().get(0).getInteger("count");
//                countRequest.reply(count);
//                connection.close();
//            });
//        });
//    }
//
//    private void get(Message<String> id) {
//        db.getConnection(ar -> {
//            SQLConnection connection = ar.result();
//            connection.query("SELECT * FROM Person p WHERE p.id = '"+id.body()+"'", result -> {
//                Optional<Person> person = result.result().getRows().stream()
//                        .map(Person::new).findFirst();
//                id.reply(person.orElse(null));
//                connection.close();
//            });
//        });
//    }
//
//    private void update(Message<String> msg) {
//        Person person = Json.decodeValue(msg.body(), Person.class);
//        db.getConnection(ar -> {
//            // Read the request's content and create an instance of Whisky.
//            SQLConnection connection = ar.result();
//            insert(person, connection);
//            connection.close();
//        });
//    }
//
//    private void getAll(final Message<int[]> message) {
//        int[] fromTo = message.body();
//        if(fromTo!=null){
//            db.getConnection(ar -> {
//                SQLConnection connection = ar.result();
//                connection.query("SELECT * FROM Person p ORDER BY p.id ASC", result -> {
//                    List<Person> persons = result.result().getRows().stream().map(Person::new).collect(Collectors.toList());
//                    persons.subList(fromTo[0], fromTo[0] + fromTo[1]);
//                    message.reply(persons);
//                    connection.close();
//                });
//            });
//        }else {
//            db.getConnection(ar -> {
//                SQLConnection connection = ar.result();
//                connection.query("SELECT * FROM Person p ORDER BY p.id ASC", result -> {
//                    List<Person> persons = result.result().getRows().stream().map(Person::new).collect(Collectors.toList());
//                    message.reply(persons);
//                    connection.close();
//                });
//            });
//        }
//    }
//
//    public void delete(Person person) {
//        db.getConnection(ar -> {
//            SQLConnection connection = ar.result();
//            connection.execute("DELETE FROM Person WHERE id='" + person.getId() + "'",
//                    result -> {
//                        connection.close();
//                    });
//        });
//    }
//}