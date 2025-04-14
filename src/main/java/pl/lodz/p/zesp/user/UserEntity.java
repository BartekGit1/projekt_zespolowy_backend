package pl.lodz.p.zesp.user;

import jakarta.persistence.*;

import java.io.Serializable;


@Entity
@Table(name = "users")
public class UserEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    private Role role = Role.CUSTOMER;

    public UserEntity(String username, String email, AccountStatus accountStatus, Role role) {
        this.username = username;
        this.email = email;
        this.accountStatus = accountStatus;
        this.role = role;
    }

    public UserEntity() {
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }
}
