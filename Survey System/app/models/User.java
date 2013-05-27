package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User extends Model {
	@Id
	public String id;
	
	public String email;
	public String name;


    public static Finder<Integer,User> find = new Finder<Integer, User>(
            Integer.class, User.class
    );
}
