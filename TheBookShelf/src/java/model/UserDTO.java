package model;


public class UserDTO {
    private String name;
    private String email;
    private String mobile;
    private String verification;  
    private String role;
    private String createdAt; 

    public UserDTO(String name, String email, String mobile, String verification, String role, String createdAt) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.verification = verification;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public String getVerification() { return verification; }
    public String getRole() { return role; }
    public String getCreatedAt() { return createdAt; }
}

