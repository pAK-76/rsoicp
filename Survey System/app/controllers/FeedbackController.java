package controllers;

import com.avaje.ebean.Expr;
import com.avaje.ebean.text.json.JsonElement;
import oauth.signpost.http.HttpRequest;
import org.codehaus.jackson.JsonNode;
import play.*;
import play.data.Form;
import play.mvc.*;

import static play.libs.Json.newObject;
import static play.libs.WS.*;


import play.db.ebean.Model;
import fmodels.Feedback;
import com.typesafe.plugin.*;
import javax.mail.*;

import java.net.URLEncoder;
import java.util.*;
import views.html.*;
import static play.libs.Json.toJson;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 04.05.13
 * Time: 14:40
 * To change this template use File | Settings | File Templates.
 */
public class FeedbackController extends Controller {
    public static Result feedback() {
        return ok(feedback.render());
    }

    public static Result sendFeedback() {
        Form<Feedback> form = Form.form(Feedback.class);
        Feedback fb = form.bindFromRequest().get();

        MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
        mail.setSubject("Review");
        mail.addRecipient("mbox1497-00@dev.iu7.bmstu.ru");

        mail.addFrom(Play.application().configuration().getString("smtp.user"));

        Map<String, Object> dict = new HashMap<String, Object>();
        dict.put("text", fb.text);
        dict.put("value", fb.value);
        dict.put("author", Play.application().configuration().getString("smtp.user"));
        JsonNode jsonNode = toJson(dict);
        String jsonString = jsonNode.toString();
        mail.send(jsonString);
        return ok("Sent successfully");
    }
}
