package controllers;

import models.Employer;
import org.apache.commons.codec.binary.Base64;
import play.data.Form;
import play.mvc.*;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.List;

import views.html.*;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 22.05.13
 * Time: 21:11
 * To change this template use File | Settings | File Templates.
 */
public class EmployerController extends Controller {

    public static Result employers() {
        if (!"yes".equals(session("admin"))) {
            return Results.forbidden();
        }
        List<Employer> list = Employer.find.all();
        return ok(employer.render(list));
    }

    public static Result addEmployer() {
        if (!"yes".equals(session("admin"))) {
            return Results.forbidden();
        }

        Form<Employer> form = Form.form(Employer.class).bindFromRequest();
        Employer employer = form.get();
        employer.save();

        return redirect("/employers");
    }

    public static Result deleteEmployer(Integer id) {
        if (!"yes".equals(session("admin"))) {
            return Results.forbidden();
        }

        Employer employer = Employer.find.byId(id);
        if (employer == null) {
            return Results.notFound();
        }
        employer.delete();

        return redirect("/employers");
    }
}
