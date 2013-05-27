package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

;

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
