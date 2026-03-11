package com.polo.Blog.Controller;

import com.polo.Blog.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import me.zhyd.oauth.model.AuthCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/oauth")
public class OAuthController {
    @Autowired
    private UserService userService;

    @GetMapping("/callback/{type}")
    public void loginHandle(@PathVariable String type, AuthCallback callback, HttpServletResponse response) throws IOException {
         userService.loginHandel(type, callback, response);
    }

}