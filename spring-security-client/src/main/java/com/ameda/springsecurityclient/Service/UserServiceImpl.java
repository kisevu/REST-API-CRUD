package com.ameda.springsecurityclient.Service;

import com.ameda.springsecurityclient.Entity.PasswordResetToken;
import com.ameda.springsecurityclient.Entity.User;
import com.ameda.springsecurityclient.Entity.VerificationToken;
import com.ameda.springsecurityclient.Model.UserModel;
import com.ameda.springsecurityclient.Repository.PasswordResetTokenRepository;
import com.ameda.springsecurityclient.Repository.UserRepository;
import com.ameda.springsecurityclient.Repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    @Override
    public User registerUser(UserModel userModel) {
        User user=new User();
        user.setEmail(userModel.getEmail());
        user.setFirstName(userModel.getFirstName());
        user.setLastName(userModel.getLastName());
        user.setRole("USER"); // should be dynamic
        user.setPassword(passwordEncoder.encode(userModel.getPassword()));
        userRepository.save(user);
        return  user;
    }

    @Override
    public void saveVerificationTokenForUser(String token, User user) {
        VerificationToken verificationToken=new VerificationToken(user,token);
        verificationTokenRepository.save(verificationToken);
    }

    @Override
    public String validateVerificationToken(String token) {
        VerificationToken verificationToken=verificationTokenRepository.findByToken(token);
        if(verificationToken==null){
            return "invalid";
        }
        User user=verificationToken.getUser();
        Calendar calender=Calendar.getInstance();
        if(verificationToken.getExpirationTime().getTime()
        -calender.getTime().getTime()<=0){
            verificationTokenRepository.delete(verificationToken);
            return "expired";
        }
        user.setEnabled(true);
        userRepository.save(user);
        return "valid";
    }

    @Override
    public VerificationToken generateNewVerificationToken(String oldToken) {
        VerificationToken verificationToken=verificationTokenRepository.findByToken(oldToken); //we confirm if there was
        //a token created earlier
        verificationToken.setToken(UUID.randomUUID().toString()); //new token created over here
        verificationTokenRepository.save(verificationToken); //saving the newly created token now
        return verificationToken; // return newly created token:
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken passwordResetToken=new PasswordResetToken(user,token);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    public String validatePasswordResetToken(String token) {
        PasswordResetToken passwordResetToken=passwordResetTokenRepository.findByToken(token);
        if(passwordResetToken==null){
            return "invalid";
        }
        User user=passwordResetToken.getUser();
        Calendar calender=Calendar.getInstance();
        if(passwordResetToken.getExpirationTime().getTime()
                -calender.getTime().getTime()<=0){
            passwordResetTokenRepository.delete(passwordResetToken);
            return "expired";
        }
        return "valid";
    }

    @Override
    public Optional<User> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(passwordResetTokenRepository.findByToken(token).getUser());
    }
    @Override
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public boolean checkIfValidOldPassword(User user, String password) {
        return  passwordEncoder.matches(password,user.getPassword());
    }
}
