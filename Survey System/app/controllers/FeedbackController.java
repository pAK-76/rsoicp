package controllers;

import com.avaje.ebean.Expr;
import com.avaje.ebean.text.json.JsonElement;
import models.Agency;
import models.Globals;
import oauth.signpost.http.HttpRequest;
import org.codehaus.jackson.JsonNode;
import play.*;
import play.data.Form;
import play.db.ebean.Transactional;
import play.mvc.*;

import static play.libs.Json.fromJson;
import static play.libs.Json.newObject;
import static play.libs.WS.*;
import play.libs.WS;
import play.libs.WS.*;


import play.db.ebean.Model;
import fmodels.Feedback;
import com.typesafe.plugin.*;
import javax.mail.*;

import java.net.URLEncoder;
import java.util.*;

import transport.Async;
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

    @Transactional
    public static void recache() {
        Integer lastCached = Globals.getValue("cachedAgencies");
        Long nowMillis = System.currentTimeMillis()/1000L;
        Integer now = nowMillis.intValue();

        if (lastCached == null || now-lastCached > 600) {
            System.out.println("Caching agencies");
            List<Agency> newList = new ArrayList<Agency>();

            try {
                WS.Response res = WS.url("http://localhost:9001/agency/json").get().get();
                JsonNode json = res.asJson();

                if (json.isArray()) {
                    Iterator<JsonNode> it = json.getElements();
                    while (it.hasNext()) {
                        JsonNode next = it.next();
                        Agency a = fromJson(next, Agency.class);
                        newList.add(a);
                    }
                }
                Globals.putValue("cachedAgencies", now);
            } catch (Exception ex) {

            }
            // Transactional scope
            if (newList.size() > 0) {
                List<Agency> oldList = Agency.find.all();
                for(Agency a : oldList) {
                    a.delete();
                }
                for(Agency a: newList) {
                    a.save();
                }
            }
        }
    }

    public static Result feedback() {
        FeedbackController.recache();
        List<Agency> list = Agency.find.all();

        return ok(feedback.render(list));
    }

    public static Result sendFeedback() {
        if (!session().containsKey("access_token"))      {
            return redirect("/");
        }
        Form<Feedback> form = Form.form(Feedback.class);
        Feedback fb = form.bindFromRequest().get();

        Map<String, Object> dict = new HashMap<String, Object>();
        dict.put("text", fb.text);
        dict.put("value", fb.value);
        dict.put("agencyId", fb.id);

        Async async = new Async("Review", play.Play.application().configuration().getString("supervising.email"), dict, false);
        async.send();

        return redirect("/");
    }
}
