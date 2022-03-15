package com.ameda.springsecurityclient.Event.Listener;

import com.ameda.springsecurityclient.Entity.User;
import com.ameda.springsecurityclient.Event.RegistrationCompleteEvent;
import com.ameda.springsecurityclient.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;
@Component
@Slf4j
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {
    @Autowired
    private UserService userService;
    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        //create verification token for the user with Link
        User user=event.getUser();
        String token= UUID.randomUUID().toString();
        userService.saveVerificationTokenForUser(token,user); //We are sending the user and token to the database
        //Sending mail to user but for ours will send the link to the console.
        String url=event.getApplicationUrl()+"/verifyRegistration?token="+token;
        //Below we are basically printing it to the console, but we should actually send the email:
        //sendVerificationEmail(): // This is a mocking we are passing over here
        log.info("Click link to verify your account:{}",url);
    }
}
