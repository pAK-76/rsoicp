package controllers;

import com.avaje.ebean.Expr;
import play.Configuration;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.*;
import views.html.*;
import models.Agency;
import models.Review;

import java.util.*;

import javax.activation.DataHandler;
import javax.mail.*;

import org.codehaus.jackson.JsonNode;
import static play.libs.Json.fromJson;
import static play.libs.Json.parse;
import static play.libs.Json.toJson;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 05.05.13
 * Time: 0:55
 * To change this template use File | Settings | File Templates.
 */
public class AgencyController extends Controller {
    public static Integer readMessage;

    public static Result getList() {
        if (!session().containsKey("auth"))      {
            return redirect("/");
        }

        List<Agency> list = Agency.find.all();

        return ok(agency.render(list));
    }
    public static Result getJSONList() {
        List<Agency> list = Agency.find.all();

        List<Object> outList = new ArrayList<Object>();
        for(Agency a: list) {
            Map<String, Object> outA = new HashMap<String, Object>();
            outA.put("id", a.id);
            outA.put("name", a.name);
            outA.put("address", a.address);
            outA.put("trust", a.mean());
            outList.add(outA);
        }

        return ok(toJson(outList));
    }

    public static Result add() {
        if (!session().containsKey("auth"))      {
            return redirect("/");
        }

        Form<Agency> form = Form.form(Agency.class);
        Agency agency = form.bindFromRequest().get();
        agency.save();

        return redirect("/agency");
    }

    public static Result delete(Integer id) {
        if (!session().containsKey("auth"))      {
            return redirect("/");
        }
        Agency.find.byId(id).delete();

        return redirect("/agency");
    }

    public static Result reviews(Integer id) {
        if (!session().containsKey("auth"))      {
            return redirect("/");
        }

        Agency agency = Agency.find.byId(id);
        List<Review> reviewList = Review.find.where(Expr.eq("agencyId", id)).findList();

        return ok(reviews.render(agency, reviewList));
    }

    public static Result generate(Integer id) {
        if (!session().containsKey("auth"))      {
            return redirect("/");
        }

        Integer count = Integer.parseInt(DynamicForm.form().bindFromRequest().get("count"));
        Integer mean = Integer.parseInt(DynamicForm.form().bindFromRequest().get("mean"));
        Integer variation = Integer.parseInt(DynamicForm.form().bindFromRequest().get("variation"));

        Random randomGenerator = new Random();
        for(Integer i=0; i<count; ++i) {
            Review review = new Review();

            Integer value = (variation > 0) ? mean - variation + randomGenerator.nextInt(2*variation+1) : mean;
            review.value = value;
            review.text = "Lorem ipsum dolores amet";
            review.author = "abyrvalg" + i + "@rsoi.ru";
            review.agencyId = id;
            review.moderated = true;
            review.save();
        }

        return redirect("/agency/" + id);
    }

    public static Result deleteReview(Integer id) {
        if (!session().containsKey("auth"))      {
            return redirect("/");
        }
        Review review = Review.find.byId(id);
        Integer agencyId = review.agencyId;
        review.delete();
        Map<String, String[]> qs = request().queryString();
        String[] redirectArr = qs.get("redirect");
        String redirect;
        if (redirectArr == null) {
            redirect = "/agency/" + agencyId;
        } else {
            redirect = redirectArr[0];
        }

        return redirect(redirect);
    }
    public static Result approveReview(Integer id) {
        if (!session().containsKey("auth"))      {
            return redirect("/");
        }
        Review review = Review.find.byId(id);
        review.moderated = true;
        review.save();

        return redirect("/feedback");
    }

    public static Result updateReviews() {
        if (!session().containsKey("auth"))      {
            return redirect("/");
        }

        Properties props = new Properties();

        Configuration conf = Play.application().configuration();
        String host = conf.getString("pop3.host");
        String username = conf.getString("pop3.user");
        String password = conf.getString("pop3.password");
        int port = conf.getInt("pop3.port");
        String provider = "pop3";

        try {
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore(provider);
            store.connect(host, port, username, password);

            Folder inbox = store.getFolder("INBOX");
            if (inbox == null) {
                System.out.println("No INBOX");
                System.exit(1);
            }
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();
            for (int i = 0; i < messages.length; i++) {
                if (readMessage != null && messages[i].getMessageNumber() <= readMessage) {
                    continue;
                }
                if (messages[i].getSubject().equals("Review")) {
                    try {
                        Multipart multipart = (Multipart) messages[i].getContent();

                        for (int x = 0; x < multipart.getCount(); x++) {
                            BodyPart bodyPart = multipart.getBodyPart(x);

                            String disposition = bodyPart.getDisposition();

                            if (disposition != null && (disposition.equals(BodyPart.ATTACHMENT))) {
                                // Do nothing, there should be no disposition
                            } else {

                                String s = (String) bodyPart.getContent();

                                JsonNode node = parse(s);

                                Review review = fromJson(node, Review.class);
                                review.moderated = false;
                                review.save();
                            }
                        }
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }
                if (readMessage==null || messages[i].getMessageNumber() > readMessage) {
                    readMessage = messages[i].getMessageNumber();
                }
            }
            inbox.close(false);
            store.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        List<Review> result =  Review.find.where(Expr.eq("moderated", false)).findList();
        return ok(moderate.render(result));
    }
}
