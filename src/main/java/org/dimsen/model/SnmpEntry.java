package org.dimsen.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

@Data
@Entity
@Builder
public class SnmpEntry extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "oid_key", columnDefinition = "TEXT")
    private String oid;

    @Column(name = "oid_value", columnDefinition = "TEXT")
    private String value;

    @Column(name = "oid_pair", columnDefinition = "TEXT")
    private String pair;

    public SnmpEntry() {
    }

    public SnmpEntry(Long id, String oid, String value, String pair) {
        this.id = id;
        this.oid = oid;
        this.value = value;
        this.pair = pair;
    }

    public SnmpEntry(String oid, String value) {
        this.oid = oid;
        this.value = value;
    }

    public SnmpEntry(String oid, String value, String pair) {
        this.oid = oid;
        this.value = value;
        this.pair = pair;
    }
}
