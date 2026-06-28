package hr.algebra.gympr.entity;

import hr.algebra.gympr.enums.Role;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return enabled; }

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    @Override public String getUsername()        { return username; }
    public void setUsername(String username)     { this.username = username; }
    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }
    @Override public String getPassword()        { return password; }
    public void setPassword(String password)     { this.password = password; }
    public Role getRole()                        { return role; }
    public void setRole(Role role)               { this.role = role; }
    public void setEnabled(boolean enabled)      { this.enabled = enabled; }
    public LocalDateTime getRegisteredAt()       { return registeredAt; }
    public void setRegisteredAt(LocalDateTime t) { this.registeredAt = t; }
}
