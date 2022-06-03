package hexlet.code.domain;

import io.ebean.annotation.WhenCreated;
import io.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.List;

@Entity
public final class Url extends Model {

    @Id
    private long id;

    @Column(unique = true)
    private String name;

    @WhenCreated
    private Instant createdAt;

    @OneToMany
    List<UrlCheck> urlChecks;

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

    public Instant getLastCheckDate() {
        if (!urlChecks.isEmpty()) {
            return urlChecks.get(urlChecks.size() - 1).getCreatedAt();
        }
        return null;
    }

    public Integer getLastCheckStatus() {
        if (!urlChecks.isEmpty()) {
            return urlChecks.get(urlChecks.size() - 1).getStatusCode();
        }
        return null;
    }

}
