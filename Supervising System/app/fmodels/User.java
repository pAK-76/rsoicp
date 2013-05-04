package fmodels;

/**
 * Created with IntelliJ IDEA.
 * User: pavelaksenkin
 * Date: 11.04.13
 * Time: 5:24
 * To change this template use File | Settings | File Templates.
 */
public class User {
    public String username;
    public String password;

    public Boolean authenticate() {
          return this.username.equals("admin") &&
                  this.password.equals("avtorkursa");
    }
}
