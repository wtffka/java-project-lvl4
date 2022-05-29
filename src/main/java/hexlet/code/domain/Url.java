package hexlet.code.domain;

import io.ebean.annotation.WhenCreated;

import javax.persistence.Id;
import java.util.Date;

public class Url {

    @Id
    private String id;
    private String name;
    @WhenCreated
    private Date createdAt;

}
