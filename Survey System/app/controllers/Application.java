package controllers;

import com.avaje.ebean.Expr;
import com.avaje.ebean.text.json.JsonElement;
import oauth.signpost.http.HttpRequest;
import org.codehaus.jackson.JsonNode;
import play.*;
import play.api.mvc.Request$;
import play.data.DynamicForm;
import play.libs.F;

import static play.libs.Json.newObject;
import static play.libs.WS.*;

import play.libs.WS;
import play.mvc.*;
import models.User;
import models.Token;
import play.api.libs.Codecs.*;


import play.db.ebean.Model;

import java.net.URLEncoder;
import java.util.*;

import scala.concurrent.Future;
import play.libs.WS.*;
import views.html.*;
import static play.libs.Json.toJson;

public class Application extends Controller {
  
    public static Result index() {
        String email = null;
        if (session().containsKey("access_token")) {
            String token = session("access_token");
            Token innerToken = (Token)new Model.Finder(String.class, Token.class).where("token='" + token + "'").findUnique();
            if (innerToken != null) {
                User user = (User)new Model.Finder(String.class, User.class).where("id='" + innerToken.user_id + "'").findUnique();
                if (user != null) {
                    email = user.email;
                } else {
                    session().remove("access_token");
                }
            }
        }
        if (email != null) {
            return ok(index.render(email));
        } else {
            return ok(login.render());
        }
    }
    
    public static Result users() {
    	List<User> users = new Model.Finder(String.class, User.class).all();
  
    	return ok(toJson(users));
    }
    public static Result tokens() {
        List<User> tokens = new Model.Finder(String.class, Token.class).all();

        return ok(toJson(tokens));
    }

    public static Result login() {
        String clientId = "30183080371.apps.googleusercontent.com";
        String redirectUri = "http://localhost:9000/auth/google/";
        String scope = "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";
        String state = "testState";
        String url = String.format("https://accounts.google.com/o/oauth2/auth?" +
                "scope=%s&" +
                "state=%s&" +
                "redirect_uri=%s&" +
                "response_type=code&" +
                "client_id=%s",
                URLEncoder.encode(scope),
                URLEncoder.encode(state),
                URLEncoder.encode(redirectUri),
                URLEncoder.encode(clientId));
        return redirect(url);
    }
    public static Result loginWithGoogleCode (String state, String code)
    {
        String url = "https://accounts.google.com/o/oauth2/token";
        String clientId = "30183080371.apps.googleusercontent.com";
        String redirectUri = "http://localhost:9000/auth/google/";
        String clientSecret = "lMqSSqjm7afQmlrj7kBDHzyh";
        String body = String.format("code=%s&" +
                "client_id=%s&" +
                "client_secret=%s&" +
                "redirect_uri=%s&" +
                "grant_type=authorization_code",
                URLEncoder.encode(code),
                URLEncoder.encode(clientId),
                URLEncoder.encode(clientSecret),
                URLEncoder.encode(redirectUri)
        );

        WS.Response res1 = WS.url(url).setContentType("application/x-www-form-urlencoded").post(body).get();
        JsonNode tokenJson = res1.asJson();
        String token = tokenJson.get("access_token").getTextValue();
//        return ok("ok=" + token);

        WS.Response res2 = WS.url("https://www.googleapis.com/oauth2/v1/userinfo").setHeader("Authorization", "Bearer " + token).get().get();
        JsonNode infoJson = res2.asJson();
        String email = infoJson.get("email").getTextValue();
        String name = infoJson.get("name").getTextValue();

        List<User> existing = new Model.Finder(String.class, User.class).where("email='" + email + "'").findList();
        User curUser;
        if (existing.size() > 0) {
            curUser = existing.get(0);
        } else {
            curUser= new User();
            curUser.email = email;
            curUser.name = name;
            curUser.save();
        }

        Token innerToken = new Token();
        innerToken.user_id = curUser.id;
        innerToken.token = play.api.libs.Codecs.sha1(String.format("Salt1%dSalt2", System.currentTimeMillis()));
        innerToken.save();
        session("access_token", innerToken.token);

//        return ok("");
        return redirect("/");
    }
    public static Result logout()
    {
        String token = session("access_token");
        if (token != null) {
            ((Model)new Model.Finder(String.class, Token.class).where(Expr.eq("token", token)).findUnique()).delete();
        }
        session().remove("access_token");
        return redirect("/");
    }



  
}
