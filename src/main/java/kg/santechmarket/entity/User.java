package kg.santechmarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kg.santechmarket.enums.UserRole;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Сущность пользователя
 * Реализует UserDetails для интеграции с Spring Security
 *
 * Бизнес-логика: пользователи создаются менеджером через админку,
 * самостоятельная регистрация отсутствует
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "phone_number")
})
@Getter
@Setter
@ToString(exclude = "password") // Исключаем пароль из toString для безопасности
public class User extends BaseEntity implements UserDetails {

    /**
     * Уникальный логин пользователя
     */
    @NotBlank(message = "Логин не может быть пустым")
    @Size(min = 3, max = 50, message = "Логин должен содержать от 3 до 50 символов")
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    /**
     * Пароль пользователя (хранится в зашифрованном виде)
     */
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Полное имя пользователя
     */
    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 100, message = "Имя не может превышать 100 символов")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    /**
     * Email пользователя
     */
    @Email(message = "Некорректный формат email")
    @Column(name = "email", unique = true)
    private String email;

    /**
     * Номер телефона в формате +996XXXXXXXXX
     */
    @Pattern(
            regexp = "^\\+996\\d{9}$",
            message = "Номер телефона должен быть в формате +996XXXXXXXXX"
    )
    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    /**
     * Роль пользователя в системе
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.CLIENT;

    /**
     * Активен ли пользователь (может ли войти в систему)
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Заказы пользователя
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    /**
     * Корзина пользователя
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cart cart;

    // Реализация UserDetails для Spring Security

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}