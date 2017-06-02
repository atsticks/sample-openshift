package gh.atsticks.samples.k8s.person;


import com.fasterxml.jackson.annotation.*;
import io.vertx.core.json.JsonObject;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Objects;

/**
 * Simple entity.
 *
 * @author Roberto Cortez
 */
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Person {

    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    private long id;

    private String name;

    private String description;

    private String imageUrl;

    // Used by JPA
    public Person(){}

    public Person(JsonObject entries) {
        this.id = entries.getLong("id", 0L);
        this.name = entries.getString("name", "<unnamed>");
        this.description = entries.getString("description", null);
        this.imageUrl = entries.getString("imageUrl", null);
    }

    public Person(String name, String description, String imageUrl) {
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Person(String name, String description) {
        this.name = Objects.requireNonNull(name);
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String link) {
        this.imageUrl = link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        Person person = (Person) o;

        return id == person.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
