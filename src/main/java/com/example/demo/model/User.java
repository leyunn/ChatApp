package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {

    @Id
    private String id;
    private String name;
    private String password;
    private String email;

    private String APIKey;
    @JsonIgnore
    public boolean isValid() {
        if (id == null || id.length() == 0) {
            return false;
        }
        if (password == null || password.length() < 6) {
            return false;
        }
        if (email == null || !email.matches(".+@.+\\..+")) {
            return false;
        }
        return true;
    }
    @JsonIgnore
    public UserDTO toDTO(){
        return new UserDTO(this.id, this.name);
    }

}
