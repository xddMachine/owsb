package com.owsb.domain;

public abstract class User {
    private String userId;
    private Role role;
    private String username;
    private String password;
    private String fullName;
    private String phone;
    private String email;
    private String status;
    
    public User() {}
    
    public User(String userId, Role role, String username, String password, 
                String fullName, String phone, String email, String status) {
        this.userId = userId;
        this.role = role;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.status = status;
    }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", role=" + role +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
    
    public String toCSV() {
        return userId + "," + role.toString() + "," + username + "," + password + "," + 
               fullName + "," + phone + "," + email + "," + status;
    }
}