package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Chatroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String roomName;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "participant1_id")),
            @AttributeOverride(name = "name", column = @Column(name = "participant1_name"))
    })
    private UserDTO participant1;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "participant2_id")),
            @AttributeOverride(name = "name", column = @Column(name = "participant2_name"))
    })
    private UserDTO participant2;
    private boolean isGroup;
    @JsonIgnore
    private LocalDateTime lastModified;

    private String lastMessage;

}
