package com.example.dto;

import com.example.entity.Role;
import javax.validation.constraints.*;

public class UserUpdateDTO {

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;
    @Email
    private String email;
    @NotBlank
    @Size(min = 6)
    private String password;
    @NotNull
    private Role role;
    private Boolean active;

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
