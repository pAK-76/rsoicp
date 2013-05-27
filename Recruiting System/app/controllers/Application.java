package controllers;

import com.avaje.ebean.Expr;
import models.Employee;

import play.*;
import play.data.Form;
import play.mvc.*;

import views.html.*;

import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;

import static play.libs.Json.toJson;

public class Application extends Controller {
    private static String generateString(Random rng, String characters, int length)
    {
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }
    private static final Pattern rfc2822 = Pattern.compile(
            "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$"
    );
    public static Result register() {
        Employee employee = Form.form(Employee.class).bindFromRequest().get();
        employee.email = employee.email.toLowerCase();
            // Check email
        if (!rfc2822.matcher(employee.email).matches()) {
            return ok(register.render(employee, "Неправильный email"));
        }
        if (Employee.find.where(Expr.eq("email", employee.email.toLowerCase())).findRowCount() > 0) {
            return ok(register.render(employee, "Email зарегистрирован"));
        }

            // Generate hash
        employee.salt = Application.generateString(new Random(), "0123456789abcdefghijklmnopqrstuvwxyzABCDEFJHIJKLMNOPQRSTUVWXYZ?&-%", 6);
        employee.password = DigestUtils.shaHex(employee.password + employee.salt);
        employee.save();

        return redirect("/");
    }
    public static Result registerForm() {
        return ok(register.render(null, null));
    }
    public static Result login() {
        Employee pretender = Form.form(Employee.class).bindFromRequest().get();
        if (pretender.isAdmin()) {
            session("admin", "yes");
            return redirect("/");
        }

        Employee etalon = Employee.find.where(Expr.eq("email", pretender.email.toLowerCase())).findUnique();
        if (etalon == null) {
             return ok(login.render("Неверный логин или пароль"));
        }
        String hash = DigestUtils.shaHex(pretender.password + etalon.salt);
        if (hash.equals(etalon.password)) {
            session("email", pretender.email);
            session("id", String.format("%d", etalon.id));
            return redirect("/");
        } else {
            return ok(login.render("Неверный логин или пароль"));
        }
    }
    public static Result logout() {
        session().remove("email");
        session().remove("admin");
        session().clear();
        return redirect("/");
    }
    public static Result index() {
        if (session().get("email") != null) {
            return ok(index.render(session().get("email")));
        } else if("yes".equals(session("admin"))) {
            return ok(index.render("admin"));
        } else {
            return ok(login.render(null));
        }
    }

    public static Result users() {
        List<Employee> users = Employee.find.findList();
        return ok(toJson(users));
    }
  
}
