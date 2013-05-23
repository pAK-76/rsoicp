package models;

import com.avaje.ebean.Expr;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 23.05.13
 * Time: 21:38
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class Globals extends Model {
    @Id
    public Integer id;

    public String key;
    public Object value;

    public static Finder<Integer,Globals> find = new Finder<Integer, Globals>(
            Integer.class, Globals.class
    );

    public static Object getValue(String key) {
        Globals item = Globals.find.where(Expr.eq("key", key)).findUnique();
        return item.value;
    }
    public static void putValue(String key, Object value) {
        Globals item = Globals.find.where(Expr.eq("key", key)).findUnique();
        if (item == null) {
            item = new Globals();
            item.key = key;
        }
        item.value = value;
        item.save();
    }
}
