package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: pavelaksenkin
 * Date: 06.05.13
 * Time: 5:17
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class Agency extends Model {
    @Id
    public Integer id;

    public String name;
    public String address;
    public String email;
    public int trust;

    public static Model.Finder<Integer,Agency> find = new Model.Finder<Integer, Agency>(
            Integer.class, Agency.class
    );
}
