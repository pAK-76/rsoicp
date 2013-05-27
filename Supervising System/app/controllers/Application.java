package controllers;

import play.*;
import play.data.Form;
import play.mvc.*;

import views.html.*;
import fmodels.User;

public class Application extends Controller {
  
    public static Result index() {
        if (session().containsKey("auth")) {
            return ok(index.render());
        } else {
            return ok(unauthed.render());
        }
    }

    public static Result login() {
        Form<User> form = Form.form(User.class);
        User user = form.bindFromRequest().get();

//        return ok(user.username + ":" + user.password);
        if (user.authenticate()) {
            session("auth", "authed");
        }
        return redirect("/");
    }

    public static Result logout() {
        session().remove("auth");
        return redirect("/");
    }
  
}
