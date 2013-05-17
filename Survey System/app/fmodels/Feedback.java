package fmodels;

import play.db.ebean.Model;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 04.05.13
 * Time: 14:51
 * To change this template use File | Settings | File Templates.
 */
public class Feedback extends Model {
    public String text;
    public int value;
    public Integer id;
}
