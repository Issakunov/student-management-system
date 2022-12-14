package jam.workspace.studentmanagementsystem.resource;

import jam.workspace.studentmanagementsystem.domain.ExceptionHandling;
import jam.workspace.studentmanagementsystem.domain.HttpResponse;
import jam.workspace.studentmanagementsystem.domain.User;
import jam.workspace.studentmanagementsystem.domain.UserPrincipal;
import jam.workspace.studentmanagementsystem.exception.*;
import jam.workspace.studentmanagementsystem.service.UserService;
import jam.workspace.studentmanagementsystem.utility.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static jam.workspace.studentmanagementsystem.constant.EmailConstant.EMAIL_SENT;
import static jam.workspace.studentmanagementsystem.constant.FileConstant.*;
import static jam.workspace.studentmanagementsystem.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static jam.workspace.studentmanagementsystem.constant.UserImplConstant.USER_DELETED_SUCCESSFULLY;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@RestController
@RequestMapping(path ={"/api/v1/users/", "/"})
@CrossOrigin("http://localhost:4200")
@AllArgsConstructor
public class UserResource extends ExceptionHandling {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("login")
    public ResponseEntity<User> login(@RequestBody User user) {
        authenticate(user.getUsername(), user.getPassword());
        User userLogin = userService.findUserByUsername(user.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(userLogin);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(userLogin, jwtHeader, OK);
    }
    @PostMapping("register")
    public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        return new ResponseEntity<>(userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail()), OK);
    }
    @PostMapping("add")
//    @PreAuthorize("hasAnyAuthority('user:delete', 'user:read')")
    public ResponseEntity<User> addNewUser(@RequestParam("firstName") String firstName,
                                           @RequestParam("lastName") String lastName,
                                           @RequestParam("username") String username,
                                           @RequestParam("email") String email,
                                           @RequestParam("role") String role,
                                           @RequestParam("isEnabled") String isEnabled,
                                           @RequestParam("isNotLocked") String isNotLocked,
                                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException {
        return new ResponseEntity<>(userService.addNewUser(firstName, lastName, username, email, role, Boolean.parseBoolean(isEnabled), Boolean.parseBoolean(isNotLocked), profileImage), OK);
    }

    @PutMapping("update")
//    @PreAuthorize("hasAnyAuthority('user:delete', 'user:read')")
    public ResponseEntity<User> updateUser(@RequestParam("currentUsername") String currentUsername,
                                           @RequestParam("firstName") String firstName,
                                           @RequestParam("lastName") String lastName,
                                           @RequestParam("username") String username,
                                           @RequestParam("email") String email,
                                           @RequestParam("role") String role,
                                           @RequestParam("isEnabled") String isEnabled,
                                           @RequestParam("isNotLocked") String isNotLocked,
                                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException {
        return new ResponseEntity<>(userService.updateUser(currentUsername, firstName, lastName, username, email, role, Boolean.parseBoolean(isEnabled), Boolean.parseBoolean(isNotLocked), profileImage), OK);
    }
    @PostMapping("update-profile-image")
//    @PreAuthorize("hasAnyAuthority('user:delete', 'user:read')")
    public ResponseEntity<User> updateProfileImage(@RequestParam("username") String username,
                                           @RequestParam(value = "profileImage") MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException {

        return new ResponseEntity<>(userService.updateProfileImage(username, profileImage), OK);
    }
    @GetMapping("find/{username}")
//    @PreAuthorize("hasAnyAuthority('user:delete', 'user:read')")
    public ResponseEntity<User> getUser(@PathVariable("username") String username) {
        return new ResponseEntity<>(userService.findUserByUsername(username), OK);
    }
    @GetMapping("list")
//    @PreAuthorize("hasAnyAuthority('user:delete', 'user:read')")
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(userService.getUsers(), OK);
    }
    @GetMapping("reset-password/{email}")
//    @PreAuthorize("hasAnyAuthority('user:delete', 'user:read')")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) throws EmailNotFoundException {
        userService.resetPassword(email);
        return response(OK, EMAIL_SENT + email);
    }

    @DeleteMapping("delete/{username}")
//    @PreAuthorize("hasAnyAuthority('user:delete', 'user:read')")
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable("username") String username) throws IOException {
        userService.deleteUser(username);
        return response(OK, USER_DELETED_SUCCESSFULLY);
    }

    @GetMapping(value = "image/{username}/{fileName}", produces = {IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE})
//    @PreAuthorize("hasAnyAuthority('user:delete', 'user:read')")
    public byte[] getProfileImage(@PathVariable("username") String username, @PathVariable("fileName") String fileName) throws IOException {
        return Files.readAllBytes(Paths.get(USER_FOLDER + username + FORWARD_SLASH + fileName));
    }

    @GetMapping(path = "image/profile/{username}", produces = {IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE})
//    @PreAuthorize("hasAnyAuthority('user:delete', 'user:read')")
    public byte[] getTempProfileImage(@PathVariable("username") String username) throws MalformedURLException {
        URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL + username);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = url.openStream()) {
            int byteRead;
            byte[] chunk = new byte[1024];
            while ((byteRead = inputStream.read(chunk)) > 0) {
                byteArrayOutputStream.write(chunk, 0, byteRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message), OK);
    }

    private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userPrincipal));
        return headers;
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

    }
}
