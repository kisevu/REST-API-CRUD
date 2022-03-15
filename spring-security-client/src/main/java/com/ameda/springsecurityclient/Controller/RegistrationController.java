package com.ameda.springsecurityclient.Controller;

import com.ameda.springsecurityclient.Entity.User;
import com.ameda.springsecurityclient.Entity.VerificationToken;
import com.ameda.springsecurityclient.Event.RegistrationCompleteEvent;
import com.ameda.springsecurityclient.Model.PasswordModel;
import com.ameda.springsecurityclient.Model.UserModel;
import com.ameda.springsecurityclient.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
public class RegistrationController {
    @Autowired
    private UserService userService;
    @Autowired
    private ApplicationEventPublisher publisher;
    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request){
        User user=userService.registerUser(userModel); // here we are populating the user table
        publisher.publishEvent(new RegistrationCompleteEvent(user,
                applicationUrl(request))); //here we triggered the event that is going to populate the VerificationToken
        //table
        return  "SUCCESS";
    }
    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token){
        //Here, enable field is false, we need verifying the link by clicking it in order to turn to true
        String result=userService.validateVerificationToken(token);
        if(result.equalsIgnoreCase("valid")){
            return "User verified successfully.";
        }
         return "Bad user";
    }
    private String applicationUrl(HttpServletRequest request) {
        return "http://"+
                request.getServerName()+
                ":"+
                request.getServerPort()+
                request.getContextPath();
    }
    @GetMapping("/resendVerifyToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken, HttpServletRequest request){
        VerificationToken verificationToken=userService.generateNewVerificationToken(oldToken); //returns  a new token
        User user=verificationToken.getUser();
        resendVerificationTokenMail(user,applicationUrl(request),verificationToken);
        return "Verification link sent.";
    }

    private void resendVerificationTokenMail(User user, String applicationUrl, VerificationToken verificationToken) {
        String url=applicationUrl+"/verifyRegistration?token="+verificationToken.getToken();

        log.info("Click link to verify your account:{}",url);
    }
    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel,HttpServletRequest request){
        User user=userService.findUserByEmail(passwordModel.getEmail());
        String url="";
        if(user!=null){
            String token= UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user,token);
            url=passwordResetTokenMail(user,applicationUrl(request),token);
        }
        return url;
    }
    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token,
                               @RequestBody PasswordModel passwordModel){
        String result=userService.validatePasswordResetToken(token);
        if(!result.equalsIgnoreCase("valid")){
            return "invalid token";
        }
        Optional<User> user=userService.getUserByPasswordResetToken(token);
        if(user.isPresent()){
            userService.changePassword(user.get(),passwordModel.getNewPassword());
            return "Password reset successful.";
        }else{
            return "invalid token.";
        }
    }
    private String passwordResetTokenMail(User user, String applicationUrl, String token) {
        String url=applicationUrl+"/savePassword?token="+token;

        log.info("Click link to Reset your password:{}",url);
        return  url;
    }
    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordModel passwordModel){
        User user=userService.findUserByEmail(passwordModel.getEmail());
        if(!userService.checkIfValidOldPassword(user,passwordModel.getPassword())){
            return "invalid old password";
        }
        //save the new password
        userService.changePassword(user,passwordModel.getNewPassword());
        return "Password changed succcessfully";
    }
}
