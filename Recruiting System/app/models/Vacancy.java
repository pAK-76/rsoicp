package models;

import play.db.ebean.Model;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Column;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 22.05.13
 * Time: 20:51
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class Vacancy extends Model {
    @Id
    public Integer id;

    public Integer employerId;
    public String projectName;
    public Date date;

    @Column(length=2000)
    public String requirements;

    public static Model.Finder<Integer,Vacancy> find = new Model.Finder<Integer, Vacancy>(
            Integer.class, Vacancy.class
    );
}
