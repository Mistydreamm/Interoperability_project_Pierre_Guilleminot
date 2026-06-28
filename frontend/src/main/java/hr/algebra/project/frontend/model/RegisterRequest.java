package hr.algebra.project.frontend.model;

public class RegisterRequest
{
    private String username;
    private String password;
    private String mail;

    public RegisterRequest(String username, String password, String mail) {
        this.username = username;
        this.password = password;
        this.mail = mail;
    }

    public RegisterRequest() {}

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
