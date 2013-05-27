package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 23.05.13
 * Time: 21:22
 * To change this template use File | Settings | File Templates.
 */

enum EmployeeStatus {
    EmployeeStatusPending,  // Ждет обзвона
    EmployeeStatusDeal,     // Договорились
    EmployeeStatusSuccess   // Пришел на проект
};

@Entity
public class Project_Employee extends Model {
    @Id
    public Integer id;

    public Integer projectId;
    public Integer employeeId;
    public EmployeeStatus status;

    public static Finder<Integer,Project_Employee> find = new Finder<Integer, Project_Employee>(
            Integer.class, Project_Employee.class
    );
}
