package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class Token extends Model {
    @Id
    public String id;

    public String user_id;
    public String token;
}
