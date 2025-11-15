package service;

import dataaccess.auth.AuthDAO;
import dataaccess.user.UserDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import requests.LoginRequest;
import requests.LogoutRequest;
import requests.RegisterRequest;
import results.LoginResult;
import results.RegisterResult;

import java.util.UUID;

/**
 * Service class for user-related operations (register, login, logout).
 * Handles authentication and user management business logic.
 */
public class UserService {
    private UserDAO userDAO;
    private AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest request) throws ServiceException {
        try {
            // Validate input
            if (request.username() == null || request.password() == null || request.email() == null) {
                throw new ServiceException("Error: bad request");
            }

            // Check if user already exists
            UserData existingUser = userDAO.getUser(request.username());
            if (existingUser != null) {
                throw new ServiceException("Error: already exists");
            }

            // Create new user
            String hashedPassword = BCrypt.hashpw(request.password(), BCrypt.gensalt());
            UserData newUser = new UserData(request.username(), hashedPassword, request.email());
            userDAO.createUser(newUser);

            // Create auth token
            String authToken = UUID.randomUUID().toString();
            AuthData authData = new AuthData(authToken, request.username());
            authDAO.createAuth(authData);

            return new RegisterResult(request.username(), authToken);
        } catch (DataAccessException e) {
            throw new ServiceException("Error: " + e.getMessage());
        }
    }

    public LoginResult login(LoginRequest request) throws ServiceException {
        try {
            // Validate input
            if (request.username() == null || request.password() == null) {
                throw new ServiceException("Error: bad request");
            }

            // Check credentials
            UserData user = userDAO.getUser(request.username());
            if (user == null || !verifyUser(user, request.password())) {
                throw new ServiceException("Error: invalid credentials");
            }

            // Create auth token
            String authToken = UUID.randomUUID().toString();
            AuthData authData = new AuthData(authToken, request.username());
            authDAO.createAuth(authData);

            return new LoginResult(request.username(), authToken);
        } catch (DataAccessException e) {
            throw new ServiceException("Error: " + e.getMessage());
        }
    }

    private boolean verifyUser(UserData user, String passwordReq){

        var hashedPassword = user.password();
        boolean result = BCrypt.checkpw(passwordReq, hashedPassword);
        System.out.println("Password provided: " + passwordReq);  // Debug
        System.out.println("Hash stored: " + hashedPassword);  // Debug
        System.out.println("BCrypt check result: " + result);  // Debug
        return result;
    };

    public void logout(LogoutRequest request) throws ServiceException {
        try {
            // Check if auth token exists
            AuthData authData = authDAO.getAuth(request.authToken());
            if (authData == null) {
                throw new ServiceException("Error: unauthorized");
            }

            // Delete auth token
            authDAO.deleteAuth(request.authToken());
        } catch (DataAccessException e) {
            throw new ServiceException("Error: " + e.getMessage());
        }
    }
}
