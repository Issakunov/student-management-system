package jam.workspace.studentmanagementsystem.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "Users")
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    @Id
    @SequenceGenerator(
            name = "users_sequence",
            sequenceName = "users_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_sequence")
    @Column(name = "id", nullable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long id;
    @Column(name = "user_id", unique = true)
    private String userId;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    @Column(name = "last_login_date")
    private Date lastLoginDate;
    @Column(name = "last_login_date_display")
    private Date lastLoginDateDisplay;
    @Column(name = "join_date")
    private Date joinDate;
    @Column(name = "role")
    private String role;
    private String[] authorities;
    @Column(name = "is_enabled")
    private boolean isEnabled;
    @Column(name = "is_not_locked")
    private boolean isNotLocked;

}
