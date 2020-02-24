package pl.monikamaria.registration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import pl.monikamaria.registration.entity.User;
import pl.monikamaria.registration.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
public class MainController {

    private UserService userService;

    @Autowired
    public MainController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public ModelAndView getRegistrationForm(){
        ModelAndView model = new ModelAndView();
        model.setViewName("registration");
//        return new ModelAndView("registration", "user", new AppUser());
        return model;
    }

    @PostMapping("/register")
    public ModelAndView postRegistration(@ModelAttribute User user, HttpServletRequest request){
        Boolean odp = userService.addNewUser(user, request);
        System.out.println("odp: " + odp);
        return new ModelAndView("login");
    }

    @GetMapping("/login")
    public ModelAndView getLoginForm(){
        ModelAndView model = new ModelAndView();
        model.setViewName("login");
        return model;
    }

    @GetMapping("/forAll")
    public ModelAndView getForAll(){
        ModelAndView model = new ModelAndView();
        model.setViewName("page");
        model.addObject("logged", false);
        model.addObject("text", "Hello Guest! Register in our service!");
        return model;
    }

    @GetMapping("/forUser")
    public ModelAndView getForUser(HttpServletRequest request, Principal principal){
        ModelAndView model = new ModelAndView();
        model.setViewName("page");
        model.addObject("logged", true);
        model.addObject("text", "Hello User " + principal.getName().split("@")[0] + "!");
        return model;
    }

    @GetMapping("/forAdmin")
    public ModelAndView getForAdmin(HttpServletRequest request, Principal principal){
        ModelAndView model = new ModelAndView();
        model.setViewName("page");
        model.addObject("logged", true);
        model.addObject("text", "Hello Admin " + principal.getName().split("@")[0] + "!");
        return model;
    }

    @GetMapping("/verify-token")
    public ModelAndView verifyToken(@RequestParam String token){
        userService.verifyToken(token);
        return new ModelAndView("redirect://login");
    }

    @GetMapping("/accept-admin")
    public ModelAndView acceptAdmin(@RequestParam String token){
        userService.acceptAdmin(token);
        return new ModelAndView("redirect://login");
    }
}
