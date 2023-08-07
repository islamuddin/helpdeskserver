package devracom.ananke.ananke.User;

import devracom.ananke.ananke.User.dto.*;
import devracom.ananke.ananke.config.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/user")
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public  UserController(UserService userService,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Returns a new JWT token
     * @param userDTO user data
     * @return ResponseEntity<JwtResponse>
     */
    @Operation(summary = "User login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403", description = "Unauthorized", content = @Content),
    })
    @PostMapping("/log-in")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody UserLogin userDTO) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userDTO.getEmail(), userDTO.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        User user = userService.getUser(userDTO.getEmail());
        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getStatus(),
                user.getRoles()
        );

        return ResponseEntity.ok(new AuthResponse(
                userResponse,
                new JwtResponse(token)
        ));
    }

    /**
     * Returns all users
     * @return List<User>
     */
    @Operation(summary = "Get all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", description = "Authorization denied", content = @Content),
    })
    @GetMapping(path = "/all")
    public List<UserResponse> getUsers() {
        return userService.getUsers();
    }

    /**
     * Returns a user by given id
     * @param id user id
     * @return User
     */
    @Operation(summary = "Get user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", description = "Authorization denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
    })
    @GetMapping(path = "/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        return new ResponseEntity<>(userService.getUser(id), HttpStatus.OK);
    }

    /**
     * Create a new user and returns the user created
     * @param user user data
     * @return User
     */
    @Operation(summary = "Create new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created new user"),
            @ApiResponse(responseCode = "401", description = "Authorization denied", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
    })
    @PostMapping(path = "/create")
    public ResponseEntity<User> createUser(@RequestBody UserNew user) {
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    /**
     * Update user data
     * @param id user id
     * @return User
     */
    @Operation(summary = "Update user data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "user updated"),
            @ApiResponse(responseCode = "401", description = "Authorization denied", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
    })
    @PutMapping(path = "/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id,
                                           @RequestBody UserUpdate userUpdate) {
        return new ResponseEntity<User>(userService.updateUser(id, userUpdate), HttpStatus.ACCEPTED);
    }

    /**
     * Delete user by given id and return void
     * @param id user id
     */
    @Operation(summary = "Delete user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "401", description = "Authorization denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
    })
    @DeleteMapping(path = "/delete/{id}")
    public void deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
    }
}
