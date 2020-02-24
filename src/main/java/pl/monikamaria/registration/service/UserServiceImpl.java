package pl.monikamaria.registration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.monikamaria.registration.entity.Authority;
import pl.monikamaria.registration.entity.User;
import pl.monikamaria.registration.entity.VerificationToken;
import pl.monikamaria.registration.repository.UserRepo;
import pl.monikamaria.registration.repository.VerificationTokenRepo;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private UserRepo userRepo;
    private PasswordEncoder passwordEncoder;
    private VerificationTokenRepo verificationTokenRepo;
    private MailSenderService mailSenderService;

    @Value("${address}")
    private String address;

    @Autowired
    public UserServiceImpl(UserRepo userRepo, PasswordEncoder passwordEncoder, VerificationTokenRepo verificationTokenRepo, MailSenderService mailSenderService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepo = verificationTokenRepo;
        this.mailSenderService = mailSenderService;
    }

    @Override
    public Boolean addNewUser(User user, HttpServletRequest request){
        //check if username is unique
        Optional<User> userOptional =  userRepo.findAllByUsername(user.getUsername());
        if(userOptional.isPresent()){
            return Boolean.FALSE;
        }
        //check if passwords are the same
        if(!user.getPassword().equals(user.getPasswordRepeat())){
            return Boolean.FALSE;
        }
        user.setEnabled(Boolean.FALSE);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.saveAndFlush(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepo.save(verificationToken);

        String url = address + "/verify-token?token="+token;

        try {
            mailSenderService.sendMail(user.getUsername(), "Verification Token", url, false);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean verifyToken(String token) {
        VerificationToken verificationToken = verificationTokenRepo.findByValue(token);
        if(verificationToken != null){
            User user = verificationToken.getUser();
            user.setEnabled(true);

            if(user.getAuthority().equals(Authority.ROLE_ADMIN)){
                sendEmailToSuperAdmin(user);
            }
            user.setAuthority(Authority.ROLE_USER);
            userRepo.save(user);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private void sendEmailToSuperAdmin(User user){
        try {
            String url = address + "/accept-admin?token="+verificationTokenRepo.findByUser_Id(user.getId());
            mailSenderService.sendMail("e1124219@urhen.com", "Accept new Admin", url, false);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean acceptAdmin(String token) {
        VerificationToken verificationToken = verificationTokenRepo.findByValue(token);
        if(verificationToken != null){
            User user = verificationToken.getUser();
            user.setAuthority(Authority.ROLE_ADMIN);
            userRepo.save(user);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

}
