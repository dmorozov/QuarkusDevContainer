package com.bric.core.domain.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.time.ZonedDateTime;
import java.util.UUID;

@MappedSuperclass
public abstract class UpdatableEntity extends PanacheEntityBase {

    @Column(name = "CREATED_ON", nullable = false, updatable = false)
    public ZonedDateTime createdTime;

    @Column(name = "CREATED_BY", nullable = false, updatable = false)
    public UUID createdBy;

    @Version
    @Column(name = "UPDATED_ON", nullable = false)
    public ZonedDateTime modifiedTime;

    @Column(name = "UPDATED_BY", nullable = false)
    public UUID modifiedBy;

    public void prePersistAudit() {
        UUID dummyUser = UUID.fromString("00000000-0000-0000-0000-000000000000");
        ZonedDateTime now = ZonedDateTime.now();
        this.createdTime = now;
        this.createdBy = dummyUser;
        this.modifiedTime = now;
        this.modifiedBy = dummyUser;
    }

    public void preUpdateAudit() {
        UUID dummyUser = UUID.fromString("00000000-0000-0000-0000-000000000000");
        this.modifiedBy = dummyUser;
    }
}
