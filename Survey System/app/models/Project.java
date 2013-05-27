package models;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 22.05.13
 * Time: 23:55
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class Project extends Model {
    @Id
    public Integer id;
    public Integer authorId;

    public String name;
    public String date;
    public Integer startAge, endAge;
    public String job;

    @Column(length=2000)
    public String features;

    @Column(length = 1000)
    public String reward;
}
