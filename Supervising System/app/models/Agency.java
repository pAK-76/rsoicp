package models;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Query;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import models.Review;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 05.05.13
 * Time: 0:48
 * To change this template use File | Settings | File Templates.
 */

@Entity
public class Agency extends Model {
   @Id
    public Integer id;

    public String name;
    public String address;
    public String email;

    public Double mean() {
        List<Review> list = Review.find.select("value").where(Expr.and(Expr.eq("agencyId", this.id), Expr.eq("moderated", true))).findList();
        Double result = 0.0;
        for(Integer i=0; i < list.size(); ++i) {
            result += list.get(i).value;
        }
        if(list.size() > 0) {
            result /= list.size();
        }

        return result;
//        Review.find.setQuery("SELECT AVG(r.value) from Review r WHERE agencyId=" + this.id).fetch()
//        Review rew = Review.find.where(Expr.eq("agencyId", this.id)).select("AVG(value)").findUnique();
//        return rew.value;
    }

    public static Finder<Integer,Agency> find = new Finder<Integer, Agency>(
            Integer.class, Agency.class
    );
}
