package com.cvd.springboot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Admin — stored in the `admins` table.
 *
 * The `password` column always holds a BCrypt hash.
 * Raw passwords are NEVER stored anywhere.
 *
 * After startup you will see in psql:
 *
 *  SELECT admin_id, name, password, role FROM admins;
 *
 *  admin_id  | name          | password           | role
 *  ----------+---------------+--------------------+-----------
 *  ADMCVD001 | Sinchana Raj  | $2a$12$xK9mP...    | SUPER_ADMIN
 *  ADMCVD002 | Priya Sharma  | $2a$12$hJ3nQ...    | ADMIN
 *  ADMCVD003 | Rahul Kumar   | $2a$12$wL7vR...    | ADMIN
 *  ADMCVD004 | Meera Singh   | $2a$12$tM2sN...    | ADMIN
 */
@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sl_no")
    private Long slNo;

    @Column(name = "admin_id", unique = true, nullable = false, length = 50)
    private String adminId;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * BCrypt hash — format: $2a$12$<22-char-salt><31-char-hash>
     * VARCHAR(255) is intentional — BCrypt output is always 60 chars,
     * the extra space future-proofs for stronger algorithms.
     * This field is NEVER returned in any API response.
     */
    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Getters ──────────────────────────────────────────────
    public Long          getSlNo()      { return slNo; }
    public String        getAdminId()   { return adminId; }
    public String        getName()      { return name; }
    public String        getPassword()  { return password; }
    public String        getRole()      { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ── Setters ──────────────────────────────────────────────
    public void setSlNo(Long slNo)                    { this.slNo = slNo; }
    public void setAdminId(String adminId)            { this.adminId = adminId; }
    public void setName(String name)                  { this.name = name; }
    public void setPassword(String password)          { this.password = password; }
    public void setRole(String role)                  { this.role = role; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
