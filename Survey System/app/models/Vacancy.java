package models;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 23.05.13
 * Time: 0:01
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class Vacancy extends Model {
    @Id
    public Integer id;

    public Integer agencyId;
    public Integer projectId;
}
