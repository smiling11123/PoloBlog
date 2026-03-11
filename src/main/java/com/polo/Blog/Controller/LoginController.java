package com.polo.Blog.Controller;

import com.polo.Blog.Annootation.RequireAuth;
import com.polo.Blog.Domain.DTO.UserDTO;
import com.polo.Blog.Service.UserService;
import com.polo.Blog.Utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/url")
    public Result<String> getLoginUrl(@RequestParam String type){
        return userService.login(type);
    }

    @PostMapping("/admin")
    public Result adminLogin(@RequestBody UserDTO userDTO){
        return userService.adminLoginHandle(userDTO);
    }
    @RequireAuth
    @PostMapping("/admin/logout")
    public Result<String> logout(){
        return userService.logoutHandle();
    }

}
