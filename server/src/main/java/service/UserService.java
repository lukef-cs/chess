package service;

import dataaccess.auth.AuthDAO;
import dataaccess.user.UserDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;

import java.util.UUID;

/**
 * Service class for user-related operations (register, login, logout).
 * Handles authentication and user management business logic.
 */
public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    /**
     * Register a new user.
     *
     * @param request contains username, password, and email
     * @return RegisterResult with username and generated authToken
     * @throws ServiceException if user already exists or validation fails
     */
    public RegisterResult register(RegisterRequest request) throws ServiceException {
        try {
            validateRegisterRequest(request);

            if (userExists(request.username())) {
                throw new ServiceException("Error: already taken");
            }

            createUser(request);
            String authToken = createAuthToken(request.username());

            return new RegisterResult(request.username(), authToken);
        } catch (DataAccessException exception) {
            throw new ServiceException("Error: " + exception.getMessage());
        }
    }

    /**
     * Login an existing user.
     *
     * @param request contains username and password
     * @return LoginResult with username and generated authToken
     * @throws ServiceException if credentials are invalid
     */
    public LoginResult login(LoginRequest request) throws ServiceException {
        try {
            validateLoginRequest(request);

            UserData user = userDAO.getUser(request.username());
            if (user == null || !isPasswordValid(user, request.password())) {
                throw new ServiceException("Error: unauthorized");
            }

            String authToken = createAuthToken(request.username());
            return new LoginResult(request.username(), authToken);
        } catch (DataAccessException exception) {
            throw new ServiceException("Error: " + exception.getMessage());
        }
    }

    /**
     * Logout a user by invalidating their auth token.
     *
     * @param request contains authToken to invalidate
     * @throws ServiceException if authToken is invalid
     */
    public void logout(LogoutRequest request) throws ServiceException {
        try {
            if (authDAO.getAuth(request.authToken()) == null) {
                throw new ServiceException("Error: unauthorized");
            }

            authDAO.deleteAuth(request.authToken());
        } catch (DataAccessException exception) {
            throw new ServiceException("Error: " + exception.getMessage());
        }
    }

    private void validateRegisterRequest(RegisterRequest request) throws ServiceException {
        if (request.username() == null || request.password() == null || request.email() == null) {
            throw new ServiceException("Error: bad request");
        }
    }

    private void validateLoginRequest(LoginRequest request) throws ServiceException {
        if (request.username() == null || request.password() == null) {
            throw new ServiceException("Error: bad request");
        }
    }

    private boolean userExists(String username) throws DataAccessException {
        return userDAO.getUser(username) != null;
    }

    private void createUser(RegisterRequest request) throws DataAccessException {
        UserData newUser = new UserData(request.username(), request.password(), request.email());
        userDAO.createUser(newUser);
    }

    private String createAuthToken(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        authDAO.createAuth(authData);
        return authToken;
    }

    private boolean isPasswordValid(UserData user, String providedPassword) {
        return user.password().equals(providedPassword);
    }
}
