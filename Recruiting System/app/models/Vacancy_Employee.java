package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 23.05.13
 * Time: 23:40
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class Vacancy_Employee extends Model {
    @Id
    public Integer id;

    public Integer vacancyId;
    public Integer employeeId;

    public static Model.Finder<Integer,Vacancy_Employee> find = new Model.Finder<Integer, Vacancy_Employee>(
            Integer.class, Vacancy_Employee.class
    );
}
