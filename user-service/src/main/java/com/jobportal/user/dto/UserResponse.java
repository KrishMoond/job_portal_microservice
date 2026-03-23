package com.jobportal.user.dto;

import com.jobportal.user.model.User;

public class UserResponse {
    private String userId;
    private String name;
    private String email;
    private User.Role role;

    public UserResponse() {}

    public UserResponse(String userId, String name, String email, User.Role role) {
        this.userId = userId; this.name = name;
        this.email = email; this.role = role;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public User.Role getRole() { return role; }
    public void setRole(User.Role role) { this.role = role; }
}
