package models;

import play.db.ebean.Model;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Column;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 22.05.13
 * Time: 20:55
 * To change this template use File | Settings | File Templates.
 */

@Entity
public class Employer extends Model {
    @Id
    public Integer id;

    public String email;
    public String name;
    public String secret;

    public static Model.Finder<Integer,Employer> find = new Model.Finder<Integer, Employer>(
            Integer.class, Employer.class
    );
}
