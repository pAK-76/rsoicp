package models;

import models.Vacancy;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 24.05.13
 * Time: 0:08
 * To change this template use File | Settings | File Templates.
 */
public class PersVacancy extends Vacancy {
    public PersVacancy (Vacancy vac) {
        this.id = vac.id;
        this.employerId = vac.employerId;
        this.projectName = vac.projectName;
        this.date = vac.date;
        this.requirements = vac.requirements;
    }
    public Boolean isChecked;
}
