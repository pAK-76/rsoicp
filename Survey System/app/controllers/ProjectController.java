package controllers;

import models.Project;
import play.data.Form;
import play.mvc.*;
import transport.Async;
import transport.Signer;
import views.html.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

        Map<String, Object> map = new HashMap<>();
        map.put("projectName", project.name);
        map.put("date", new SimpleDateFormat("yyyy-MM-dd").format(project.date));
        String requirements = "";
        requirements += "Профессия: " + project.job + "\n";
        requirements += "Не младше: " + project.startAge + "\n";
        requirements += "Не старше: " + project.endAge + "\n";
        requirements += "Количество: " + project.empCount + "\n";
        requirements += "Вознаграждение: " + project.reward + "\n";
        requirements += "Требования: " + project.features;
        map.put("requirements", requirements);

        Async async = new Async("Create vacancy", "mbox1497-02@dev.iu7.bmstu.ru", map, true);
        async.send();

        return redirect("/projects");
    }

    public static Result deleteProject(Integer id) {
        Project item = Project.find.byId(id);
        item.delete();
        return redirect("/projects");
    }
}
