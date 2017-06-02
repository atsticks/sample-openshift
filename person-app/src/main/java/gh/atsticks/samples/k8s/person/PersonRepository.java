package gh.atsticks.samples.k8s.person;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;

import javax.persistence.*;
import javax.transaction.Transaction;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;


public class PersonRepository extends AbstractVerticle{

    static final String LIST = "Person.getAll(int[] fromTo) -> List<Person>";
    static final String STORE = "Person.store(Person person)";
    static final String DELETE = "Person.delete(String id)";
    static final String GET = "Person.get(String id) -> Person";

    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;


    @Override
    public void start(Future<Void> fut) {
            try {
                vertx.executeBlocking(this::initDB, fut);
                vertx.eventBus().<String>consumer(LIST).handler(this::list);
                vertx.eventBus().<String>consumer(GET).handler(this::get);
                vertx.eventBus().<String>consumer(DELETE).handler(this::delete);
                vertx.eventBus().<String>consumer(STORE).handler(this::store);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
    }


    private void initDB(Future<Void> future) {
        try{
            entityManagerFactory = Persistence.createEntityManagerFactory("myPU",
                    getDBProperties());
            entityManager = entityManagerFactory.createEntityManager();
            if(countPersons()==0){
                EntityTransaction ta = entityManager.getTransaction();
                if(!ta.isActive()) {
                    ta.begin();
                }
                entityManager.persist(new Person("Uzumaki Naruto", "Konoha", "http://img1.wikia.nocookie.net/__cb20140523045537/naruto/images/thumb/3/36/Naruto_Uzumaki.png/300px-Naruto_Uzumaki.png"));
                entityManager.persist(new Person("Hatake Kakashi", "Konoha", "http://img1.wikia.nocookie.net/__cb20140616090940/naruto/images/thumb/b/b3/KakashiHatake.png/300px-KakashiHatake.png"));
                entityManager.persist(new Person("Haruno Sakura", "Konoha", "http://vignette2.wikia.nocookie.net/naruto/images/b/ba/Sakurap2.png/revision/latest/scale-to-width-down/300?cb=20150825101949"));
                entityManager.persist(new Person("Uchiha Sasuke", "Missing-nin", "http://vignette1.wikia.nocookie.net/naruto/images/9/98/Sasuke_p2_headshot.png/revision/latest?cb=20150110232732"));
                entityManager.persist(new Person("Gaara", "Sunagakure", "http://img3.wikia.nocookie.net/__cb20130910220958/naruto/images/thumb/0/0f/Gaara_Part_II.png/300px-Gaara_Part_II.png"));
                entityManager.persist(new Person("Killer Bee", "Kumogakure", "http://vignette3.wikia.nocookie.net/naruto/images/6/63/Killer_B.png/revision/latest/scale-to-width-down/300?cb=20150917075056"));
                entityManager.persist(new Person("Jiraya", "Konoha", "http://img2.wikia.nocookie.net/__cb20120925123905/naruto/images/thumb/2/21/Profile_Jiraiya.PNG/300px-Profile_Jiraiya.PNG"));
                entityManager.persist(new Person("Namikaze Minato", "Konoha", "http://img4.wikia.nocookie.net/__cb20140209115534/naruto/images/thumb/1/1f/Minato_Namikaze.PNG/300px-Minato_Namikaze.PNG"));
                entityManager.persist(new Person("Uchiha Madara", "Missing-nin", "http://vignette3.wikia.nocookie.net/naruto/images/0/0c/Madara_img2.png/revision/latest?cb=20160115141947"));
                entityManager.persist(new Person("Senju Hashirama", "Konoha", "http://img2.wikia.nocookie.net/__cb20120915132454/naruto/images/thumb/7/7e/Hashirama_Senju.png/300px-Hashirama_Senju.png"));
                entityManager.persist(new Person("Might Guy", "Konoha", "http://img1.wikia.nocookie.net/__cb20150401084456/naruto/images/3/31/Might_Guy.png"));
                entityManager.persist(new Person("Hyuga Neji", "Konoha", "http://img1.wikia.nocookie.net/__cb20150123214015/naruto/images/6/63/Neji_Part_2.png"));
                entityManager.persist(new Person("Rock Lee", "Konoha", "http://img1.wikia.nocookie.net/__cb20131029112352/naruto/images/thumb/7/7d/Lee_timeskip.png/300px-Lee_timeskip.png"));
                entityManager.persist(new Person("Uchiha Obito", "Missing-nin", "http://vignette4.wikia.nocookie.net/naruto/images/4/4a/Obito_Uchiha.png/revision/latest?cb=20160312115221"));
                entityManager.persist(new Person("Kurama", "Tailed Beast", "http://img1.wikia.nocookie.net/__cb20140818171718/naruto/images/thumb/7/7b/Kurama2.png/300px-Kurama2.png"));
                entityManager.persist(new Person("Uzumaki Kushina", "Konoha", "http://img4.wikia.nocookie.net/__cb20121006054451/naruto/images/thumb/4/4d/Kushina_2.png/300px-Kushina_2.png"));
                entityManager.persist(new Person("Nara Shikamaru", "Konoha", "http://img1.wikia.nocookie.net/__cb20130917013425/naruto/images/thumb/9/9a/Shikamaru_Nara.png/300px-Shikamaru_Nara.png"));
                entityManager.persist(new Person("Sarutobi Hiruzen", "Konoha", "http://img4.wikia.nocookie.net/__cb20120912121115/naruto/images/thumb/e/e4/Hiruzen_Sarutobi.png/300px-Hiruzen_Sarutobi.png"));
                entityManager.persist(new Person("Tsunade", "Konoha", "http://img4.wikia.nocookie.net/__cb20150108211132/naruto/images/b/b3/Tsunade_infobox2.png"));
                entityManager.persist(new Person("Orochimaru", "Missing-nin", "http://vignette2.wikia.nocookie.net/naruto/images/1/14/Orochimaru_Infobox.png/revision/latest/scale-to-width-down/300?cb=20150925223113"));
                entityManager.persist(new Person("Uchicha Itachi", "Missing-nin", "http://vignette2.wikia.nocookie.net/naruto/images/b/bb/Itachi.png/revision/latest/scale-to-width-down/300?cb=20150602102445"));
                ta.commit();
                future.complete();
            }
        }catch(Exception e){
            future.fail(e);
        }
    }


    @Override
    public void stop() throws Exception {
        this.entityManager.close();
        this.entityManagerFactory.close();
    }


    private long countPersons() {
        Query query = entityManager.createQuery("SELECT COUNT(p.id) FROM Person p");
        return (long)query.getSingleResult();
    }

    private void get(Message<String> msg) {
        String id = msg.body();
        msg.reply(Json.encode(entityManager.find(Person.class, id)));
    }

    private void store(Message<String> msg) {
        final Person person = Json.decodeValue(msg.body(), Person.class);
        EntityTransaction ta = entityManager.getTransaction();
        vertx.executeBlocking(h -> {
            if(!ta.isActive()) {
                ta.begin();
            }
            if (person.getId() == 0) {
                entityManager.persist(person);
                msg.reply(Json.encodePrettily(person));
            } else {
                Person personToUpdate = entityManager.find(Person.class, person.getId());
                personToUpdate.setName(person.getName());
                personToUpdate.setDescription(person.getDescription());
                personToUpdate.setImageUrl(person.getImageUrl());
                Person merged = entityManager.merge(personToUpdate);
                msg.reply(Json.encodePrettily(merged));
            }
        }, s -> {
            if(s.failed()){
                s.cause().printStackTrace();
                ta.rollback();
            }else{
                ta.commit();
            }
        });
    }

    private void list(Message<String> msg) {
        PaginatedListWrapper wrapper = Json.decodeValue(msg.body(), PaginatedListWrapper.class);
        vertx.executeBlocking(h -> {
            wrapper.setTotalResults(countPersons());
            int start = (wrapper.getCurrentPage() - 1) * wrapper.getPageSize();
            wrapper.setList(findPersons(start,
                    wrapper.getPageSize(),
                    wrapper.getSortFields(),
                    wrapper.getSortDirections()));
            msg.reply(Json.encode(wrapper));
        }, s -> {
            if(s.failed()){
                s.cause().printStackTrace();
            }
        });
    }

    private List<Person> findPersons(int startPosition, int maxResults, String sortFields, String sortDirections) {
        Query query =
                entityManager.createQuery("SELECT p FROM Person p ORDER BY p." + sortFields + " " + sortDirections);
        query.setFirstResult(startPosition);
        query.setMaxResults(maxResults);
        return query.getResultList();
    }

    public void delete(Message<String> msg) {
        EntityTransaction ta = entityManager.getTransaction();
        vertx.executeBlocking(h -> {
            if(!ta.isActive()) {
                ta.begin();
            }
            Person person = entityManager.find(Person.class, msg.body());
            if(person!=null){
                entityManager.remove(person);
            }
        }, s -> {
            if(s.failed()){
                s.cause().printStackTrace();
                ta.rollback();
            }else{
                ta.commit();
            }
        });
    }

    public Map<String,String> getDBProperties() {
        final Map<String,String> props = new HashMap<>();
        try {
            Properties cpProps = new Properties();
            cpProps.load(getClass().getClassLoader().getResourceAsStream("db.properties"));
            cpProps.forEach((k, v) -> {
                props.put(k.toString(), v.toString());
            });
            // lookup on file system, e.g. for a resource provided via secrets/configMaps
            File configFile = new File("/deployments/db.properties");
            System.out.println("Checking for external config: /deployments/db.properties...");

            if(configFile.exists()){
                System.out.println("Reading DB config from file: /deployments/db.properties...");
                cpProps.load(new FileInputStream(configFile));
                cpProps.forEach((k, v) -> {
                    props.put(k.toString(), v.toString());
                });
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        replaceVars(props);
        System.out.println("Using DB properties: " + props);
        return props;
    }

    public static void main(String[] args) {
        Map<String,String> m = new HashMap<>();
        m.put("a", "b");
        m.put("HOME", "${user.home}");
        m.put("NAME", "Name: ${user.name}.");
        m.put("Mixed", "Name: ${user.name}, home: ${user.home}");
        replaceVars(m);
        System.out.println(m);
    }

    private static void replaceVars(Map<String, String> props) {
        StringBuilder b = new StringBuilder();
        props.replaceAll((k,v) -> {
            b.setLength(0);
            int pos = 0;
            int index0 = v.indexOf("${", pos);
            if(index0>=0){
                while (index0>=0) {
                    b.append(v.substring(pos, index0));
                    int index1 = v.indexOf('}', pos);
                    String var = v.substring(index0 + 2, index1);
                    b.append(resolveVar(var));
                    pos = index1 + 1;
                    index0 = v.indexOf("${", pos);
                }
                if(pos<(v.length()-1)){
                    b.append(v.substring(pos));
                }
                v = b.toString();
            }
            return v;
        });
    }

    private static String resolveVar(String var) {
        return Optional.ofNullable(System.getProperty(var))
                .orElse(System.getenv(var));
    }
}