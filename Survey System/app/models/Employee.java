package models;

import play.db.ebean.Model;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Column;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 23.05.13
 * Time: 20:33
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class Employee extends Model {
    @Id
    public Integer id;

    public String email;
    public String salt;
    public String password;

    public String firstName;
    public String lastName;

    public String city;

    public Date birthDate;

    public String job;

    @Column(length=2000)
    public String features;

    public static Finder<Integer,Employee> find = new Finder<Integer, Employee>(
            Integer.class, Employee.class
    );
}
