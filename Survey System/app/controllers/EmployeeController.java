package controllers;

import com.avaje.ebean.Expr;
import models.Employee;
import play.data.Form;
import play.mvc.*;

import views.html.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 23.05.13
 * Time: 22:58
 * To change this template use File | Settings | File Templates.
 */
public class EmployeeController extends Controller {
    public static Result employees() {
        List<Employee> list = Employee.find.where(Expr.eq("isTemporary", false)).findList();

        return ok(employees.render(list));
    }
    public static Result addEmployeeForm() {
        return ok(employee.render(null));
    }
    public static Result editEmployeeForm(Integer id) {
        Employee item = Employee.find.byId(id);
        return ok(employee.render(item));
    }

    public static Result saveEmployee() {
        Employee item = Form.form(Employee.class).bindFromRequest().get();
        if(item.id != null) {
            item.update();
        } else {
            item.isTemporary = false;
            item.save();
        }

        return redirect("/employees");
    }

    public static Result deleteEmployee(Integer id) {
        Employee item = Employee.find.byId(id);
        item.delete();
        return redirect("/employees");
    }
}
