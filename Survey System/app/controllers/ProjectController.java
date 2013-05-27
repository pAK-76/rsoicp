package controllers;

import com.avaje.ebean.Expr;
import models.*;
import org.codehaus.jackson.JsonNode;
import play.data.Form;
import play.libs.WS;
import play.mvc.*;
import transport.Async;
import transport.Signer;
import views.html.*;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static play.libs.Json.fromJson;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 23.05.13
 * Time: 0:06
 * To change this template use File | Settings | File Templates.
 */
public class ProjectController extends Controller {
    public static Result projects() {
        List<Project> list = Project.find.all();

        return ok(projects.render(list));
    }
    public static Result addProjectForm() {
        return ok(project.render(null));
    }
    public static Result editProjectForm(Integer id) {
        Project item = Project.find.byId(id);
        return ok(project.render(item));
    }

    public static Result saveProject() {
        Project item = Form.form(Project.class).bindFromRequest().get();
        if(item.id != null) {
            item.update();
        } else {
            item.save();
        }

        return redirect("/projects");
    }

    public static Result createVacancy(Integer id) {
        Project project = Project.find.byId(id);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("projectName", project.name);
        map.put("id", project.id);
        map.put("date", new SimpleDateFormat("yyyy-MM-dd").format(project.date));
        String requirements = "";
        requirements += "Профессия: " + project.job + "\n";
        requirements += "Не младше: " + project.startAge + "\n";
        requirements += "Не старше: " + project.endAge + "\n";
        requirements += "Количество: " + project.empCount + "\n";
        requirements += "Вознаграждение: " + project.reward + "\n";
        requirements += "Требования: " + project.features;
        map.put("requirements", requirements);

        FeedbackController.recache();
        for (Agency a: Agency.find.where(Expr.gt("trust", 5)).findList()) {
            Vacancy vac = new Vacancy();
            vac.agencyId = a.id;
            vac.projectId = id;
            vac.save();
            Async async = new Async("Create vacancy", a.email, map, true);
            async.send();
        }

        return redirect("/projects");
    }
    public static Result deleteVacancy(Integer id) {
        if (Project.find.byId(id)==null) {
            return Results.notFound();
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);

        List<Vacancy> vacancies = Vacancy.find.where(Expr.eq("projectId", id)).findList();
        for(Vacancy vacancy: vacancies) {
            Agency a = Agency.find.byId(vacancy.agencyId);
            Async async = new Async("Delete vacancy", a.email, map, true);
            async.send();
            vacancy.delete();
        }

        return redirect("/projects");
    }

    public static Result responses(Integer id) {
        List<Vacancy> list = Vacancy.find.findList();
        String from = play.Play.application().configuration().getString("smtp.user");

        String result = "";
        for(Vacancy vac: list) {
            Agency a = Agency.find.byId(vac.agencyId);
            try {
                String url = a.address + "/vacancies/responses";
                Map<String, Object> map = new TreeMap<String, Object>();
                map.put("author", from);
                map.put("id", id);
                map.put("time", String.format("%d", System.currentTimeMillis() / 1000L));
                String stringToSign = url + "?" + Signer.stringToSign(map);
                String signature = Signer.signature(stringToSign);

                WS.Response res = WS.url(url).setQueryParameter("author", from).
                        setQueryParameter("id", String.format("%d", id)).
                        setQueryParameter("time", (String)map.get("time")).
                        setQueryParameter("signature", signature).
                        get().get();
                JsonNode json = res.asJson();
                JsonNode resultNode = json.get("status");
                if (resultNode != null && resultNode.asText().equals("ok")) {
                    JsonNode listNode = json.get("responses");
                    Iterator<JsonNode> iter = listNode.iterator();
                    while(iter.hasNext()) {
                        JsonNode cur = iter.next();
                        String email = cur.get("email").asText();
                        Integer count = Employee.find.where(Expr.eq("email", email)).findRowCount();
                        if (count == 0) {
                            Employee emp = new Employee();
                            emp.agencyId = a.id;
                            emp.birthDate = new Date();
                            emp.birthDate.setTime(cur.get("birthDate").asLong());
//                            emp.birthDate = new SimpleDateFormat("yyyy-MM-dd").parse(cur.get("birthDate").asText());
                            emp.city = cur.get("city").asText();
                            emp.email = email;
                            emp.features = cur.get("features").asText();
                            emp.job = cur.get("job").asText();
                            emp.lastName = cur.get("lastName").asText();
                            emp.firstName = cur.get("firstName").asText();
                            emp.phone = cur.get("phone").asText();

                            emp.isTemporary = true;
                            emp.save();
                            Project_Employee pe = new Project_Employee();
                            pe.employeeId = emp.id;
                            pe.projectId = id;
                            pe.status = EmployeeStatus.Pending;
                            pe.save();
                        }
                    }
                }
            } catch(Exception ex) {
                System.out.println(ex);
            }
        }
        List<Employee> responsesList = Employee.find.where(
                Expr.in("id", Project_Employee.find.select("employeeId").where(Expr.eq("projectId", id)))).findList();
        return ok(responses.render(responsesList));
    }

    public static Result deleteProject(Integer id) {
        Project item = Project.find.byId(id);
        item.delete();
        return redirect("/projects");
    }
}
