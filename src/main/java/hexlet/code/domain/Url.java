package hexlet.code.domain;

import io.ebean.annotation.WhenCreated;
import io.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public final class Url extends Model {

    @Id
    private long id;

    @Column(unique = true)
    private String name;

    @WhenCreated
    private Instant createdAt;

    public Url(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
