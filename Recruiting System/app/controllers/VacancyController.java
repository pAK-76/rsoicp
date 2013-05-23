package controllers;

import Transport.Signer;
import com.avaje.ebean.Expr;
import models.PersVacancy;
import models.Vacancy;
import models.Vacancy_Employee;
import org.codehaus.jackson.JsonNode;
import play.Configuration;
import play.Play;
import play.mvc.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import views.html.*;

import static play.libs.Json.fromJson;
import static play.libs.Json.parse;
import javax.mail.*;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 23.05.13
 * Time: 23:45
 * To change this template use File | Settings | File Templates.
 */
public class VacancyController extends Controller {
    public static Integer readMessage;

    public static void updateVacancies() {
//        readMessage = -1;
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
                if (messages[i].getSubject().equals("Create vacancy")) {
                    try {
                        Multipart multipart = (Multipart) messages[i].getContent();

                        for (int x = 0; x < multipart.getCount(); x++) {
                            BodyPart bodyPart = multipart.getBodyPart(x);

                            String disposition = bodyPart.getDisposition();

                            if (disposition != null && (disposition.equals(BodyPart.ATTACHMENT))) {
                                // Do nothing, there should be no disposition
                            } else {

                                String s = (String) bodyPart.getContent();
                                System.out.println(s);

                                JsonNode node = parse(s);
                                Map<String, Object> params = new TreeMap<>();
                                String signature = new String();
                                Iterator<String> iter = node.getFieldNames();
                                while(iter.hasNext()) {
                                    String key = iter.next();
                                    if (key.equals("signature")) {
                                        signature = node.get(key).getTextValue();
                                    } else {
                                        params.put(key, node.get(key).getTextValue());
                                    }
                                }
                                Integer empId = Signer.checkSignature(params, signature);
                                if (empId != -1) {
                                    System.out.println("Valid");
                                    Vacancy vac = new Vacancy();
                                    vac.employerId = empId;
                                    vac.requirements = (String)params.get("requirements");
                                    vac.date = new SimpleDateFormat("yyyy-MM-dd").parse((String)params.get("date"));
                                    vac.projectName =  (String)params.get("projectName");
                                    vac.save();
                                } else {
                                    System.out.println("Invalid");
                                }

                                if (messages[i].getMessageNumber() > readMessage) {
                                    readMessage = messages[i].getMessageNumber();
                                }
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

        System.out.println("Finished updating");
    }

    public static Result vacancies() {
        VacancyController.updateVacancies();


        Integer userId = Integer.parseInt(session("id"));

        List<Vacancy> list = Vacancy.find.all();
        List<PersVacancy> list2 = new ArrayList<>();
        for (Vacancy vac: list) {
            PersVacancy pv = new PersVacancy(vac);
            pv.isChecked = Vacancy_Employee.find.where(Expr.and(Expr.eq("vacancyId", vac.id), Expr.eq("employeeId", userId))).findRowCount() == 1;
            list2.add(pv);
        }
         return ok(vacancies.render("email", list2));
    }

    public static Result toggle(Integer id) {
        Vacancy vac = Vacancy.find.byId(id);
        Integer userId = Integer.parseInt(session("id"));
        Vacancy_Employee ve = Vacancy_Employee.find.where(Expr.and(Expr.eq("vacancyId", vac.id), Expr.eq("employeeId", userId))).findUnique();
        if (ve == null) {
            ve = new Vacancy_Employee();
            ve.employeeId = userId;
            ve.vacancyId = vac.id;
            ve.save();
        } else {
            ve.delete();
        }

        return redirect("/vacancies");
    }
}
