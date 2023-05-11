package se.sundsvall.citizenassets.integration.db.model;


import java.sql.Timestamp;
import java.time.LocalDateTime;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@MappedSuperclass
public abstract class BaseEntity {

   private Timestamp created;
   private Timestamp updated;

    @PrePersist
    void setCreated(){
        created = Timestamp.valueOf(LocalDateTime.now());
    }

    @PreUpdate()
    void setUpdated() {
        updated = Timestamp.valueOf(LocalDateTime.now());
    }
}
