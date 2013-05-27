package controllers;

import Transport.Signer;
import com.avaje.ebean.Expr;
import models.*;
import org.codehaus.jackson.JsonNode;
import play.Configuration;
import play.Play;
import play.db.ebean.Transactional;
import play.mvc.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import views.html.*;

import static play.libs.Json.fromJson;
import static play.libs.Json.parse;
import static play.libs.Json.toJson;

import javax.mail.*;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 23.05.13
 * Time: 23:45
 * To change this template use File | Settings | File Templates.
 */
public class VacancyController extends Controller {

    public static void createVacancy(Message message) {
        try {
            Multipart multipart = (Multipart) message.getContent();

            for (int x = 0; x < multipart.getCount(); x++) {
                BodyPart bodyPart = multipart.getBodyPart(x);

                String disposition = bodyPart.getDisposition();

                if (disposition != null && (disposition.equals(BodyPart.ATTACHMENT))) {
                    // Do nothing, there should be no disposition
                } else {

                    String s = (String) bodyPart.getContent();
                    System.out.println(s);

                    JsonNode node = parse(s);
                    Map<String, Object> params = new TreeMap<String, Object>();
                    String signature = new String();
                    Iterator<String> iter = node.getFieldNames();
                    while(iter.hasNext()) {
                        String key = iter.next();
                        if (key.equals("signature")) {
                            signature = node.get(key).getTextValue();
                        } else {
                            JsonNode cur = node.get(key);
                            if (cur.isTextual()) {
                                params.put(key, cur.asText());
                            }  else if (cur.isInt()) {
                                params.put(key, cur.asInt());
                            }
                        }
                    }
                    Integer empId = Signer.checkSignature(params, signature);
                    if (empId != -1) {
                        System.out.println("Valid");
                        Integer inner_id = (Integer)params.get("id");
                        Vacancy vac = Vacancy.find.where(Expr.and(Expr.eq("inner_id", inner_id), Expr.eq("employerId", empId))).findUnique();
                        if (vac != null) {
                            System.out.println("Duplicate vacancy for project");
                            return;
                        }
                        vac = new Vacancy();
                        vac.inner_id = inner_id;
                        vac.employerId = empId;
                        vac.requirements = (String)params.get("requirements");
                        vac.date = new SimpleDateFormat("yyyy-MM-dd").parse((String)params.get("date"));
                        vac.projectName =  (String)params.get("projectName");
                        vac.save();
                    } else {
                        System.out.println("Invalid");
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    @Transactional
    public static void deleteVacancy(Message message) {
        try {
            Multipart multipart = (Multipart) message.getContent();

            for (int x = 0; x < multipart.getCount(); x++) {
                BodyPart bodyPart = multipart.getBodyPart(x);

                String disposition = bodyPart.getDisposition();

                if (disposition != null && (disposition.equals(BodyPart.ATTACHMENT))) {
                    // Do nothing, there should be no disposition
                } else {

                    String s = (String) bodyPart.getContent();
                    System.out.println(s);

                    JsonNode node = parse(s);
                    Map<String, Object> params = new TreeMap<String, Object>();
                    String signature = new String();
                    Iterator<String> iter = node.getFieldNames();
                    while(iter.hasNext()) {
                        String key = iter.next();
                        if (key.equals("signature")) {
                            signature = node.get(key).getTextValue();
                        } else {
                            JsonNode cur = node.get(key);
                            if (cur.isTextual()) {
                                params.put(key, cur.asText());
                            }  else if (cur.isInt()) {
                                params.put(key, cur.asInt());
                            }
                        }
                    }
                    Integer empId = Signer.checkSignature(params, signature);
                    if (empId != -1) {
                        Integer inner_id = (Integer)params.get("id");
                        Vacancy vac = Vacancy.find.where(Expr.and(Expr.eq("inner_id", inner_id), Expr.eq("employerId", empId))).findUnique();
                        List<Vacancy_Employee> responses =  Vacancy_Employee.find.where(Expr.eq("vacancyId", vac.id)).findList();
                        for (Vacancy_Employee resp: responses) {
                            resp.delete();
                        }
                        vac.delete();
                        System.out.println("Valid, deleted vacancy and all responses");
                    } else {
                        System.out.println("Invalid");
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    public static void updateVacancies() {
        Integer lastChecked = Globals.getValue("checkedMail");
        Long nowMillis = System.currentTimeMillis()/1000L;
        Integer now = nowMillis.intValue();

        if (lastChecked != null && now-lastChecked < 120) return;

        Integer readMessage = Globals.getValue("readMessage");
        System.out.println(String.format("readMessage: %d", readMessage));

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
                    VacancyController.createVacancy(messages[i]);
                } else if (messages[i].getSubject().equals("Delete vacancy")) {
                    VacancyController.deleteVacancy(messages[i]);
                }
                if (readMessage == null || messages[i].getMessageNumber() > readMessage) {
                    readMessage = messages[i].getMessageNumber();
                    Globals.putValue("readMessage", readMessage);
                }
            }
            inbox.close(false);
            store.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        Globals.putValue("checkedMail", now);
        System.out.println("Finished updating");
    }

    public static Result responses(String author, Integer id, String time, String signature) {
        String url = "http://" + request().host() + request().path();
        Map<String, Object> params = new TreeMap<String, Object>();
        params.put("author", author);
        params.put("id", id);
        params.put("time", time);

        Integer empId = Signer.checkSignature(url, params, signature);
        if (empId == -1) {
            return Results.forbidden();
        }

        Map<String, Object> result = new HashMap<String, Object>();

        VacancyController.updateVacancies();
        Vacancy vac = Vacancy.find.where(Expr.and(Expr.eq("inner_id", id), Expr.eq("employerId", empId))).findUnique();
        if (vac == null) {
            result.put("status", "closed");
            return ok(toJson(result));
        }
        List<Vacancy_Employee> responses = Vacancy_Employee.find.where(Expr.eq("vacancyId", vac.id)).findList();
        List<Employee> resumes = new ArrayList<Employee>();
        for(Vacancy_Employee resp: responses) {
            Employee emp = Employee.find.byId(resp.employeeId);
            resumes.add(emp);
        }
        result.put("responses", resumes);
        result.put("status", "ok");
        return ok(toJson(result));
    }

    public static Result vacancies() {
        VacancyController.updateVacancies();


        Integer userId = Integer.parseInt(session("id"));

        List<Vacancy> list = Vacancy.find.all();
        List<PersVacancy> list2 = new ArrayList<PersVacancy>();
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
