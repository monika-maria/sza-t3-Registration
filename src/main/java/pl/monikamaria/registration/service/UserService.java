package pl.monikamaria.registration.service;

import pl.monikamaria.registration.entity.User;

import javax.servlet.http.HttpServletRequest;

public interface UserService {

    Boolean addNewUser(User user, HttpServletRequest request);
    Boolean verifyToken(String token);
    Boolean acceptAdmin (String token);
}
