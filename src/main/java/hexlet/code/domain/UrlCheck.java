package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.Instant;


@Entity
public final class UrlCheck extends Model {

    @Id
    private long id;

    private int responseCode;
    private String title;
    private String header;

    @Lob
    private String description;

    @ManyToOne
    private Url url;

    @WhenCreated
    private Instant createdAt;

    public UrlCheck(int responseCode, String title, String header, String description, Url url) {
        this.responseCode = responseCode;
        this.title = title;
        this.header = header;
        this.description = description;
        this.url = url;
    }

    public long getId() {
        return id;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getTitle() {
        return title;
    }

    public String getHeader() {
        return header;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
