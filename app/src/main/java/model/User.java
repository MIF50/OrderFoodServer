package model;

/**
 * Created by mohamed on 12/5/17.
 */

public class User {
    private String name;
    private String phone;
    private String password;
    private String isStuff;

    public User() {
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIsStuff() {
        return isStuff;
    }

    public void setIsStuff(String isStuff) {
        this.isStuff = isStuff;
    }
}
