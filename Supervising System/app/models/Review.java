package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 05.05.13
 * Time: 0:51
 * To change this template use File | Settings | File Templates.
 */

@Entity
public class Review extends Model{
    @Id
    public Integer id;

    public Integer agencyId;
    public String author;
    public String text;
    public int value;
    public Boolean moderated;

    public static Finder<Integer,Review> find = new Finder<Integer, Review>(
            Integer.class, Review.class
    );
}
