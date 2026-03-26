package com.jobportal.user.dto;

import com.jobportal.user.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z\\s'-]{2,50}$", message = "Name must be 2-50 characters and contain only letters, spaces, hyphens, or apostrophes")
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
             message = "Password must be at least 8 characters with one uppercase letter, one digit, and one special character (@$!%*?&)")
    private String password;

    @NotNull
    private User.Role role;

    public RegisterRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public User.Role getRole() { return role; }
    public void setRole(User.Role role) { this.role = role; }
}
