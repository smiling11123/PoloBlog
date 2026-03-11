package com.polo.Blog.Controller;

import com.polo.Blog.Annootation.RequireAuth;
import com.polo.Blog.Domain.DTO.AuthInfoDTO;
import com.polo.Blog.Domain.Entity.AuthInfo;
import com.polo.Blog.Service.AuthInfoService;
import com.polo.Blog.Utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/authInfo")
public class AuthInfoController {
    @Autowired
    private AuthInfoService authInfoService;

    @GetMapping("/get")
    public Result<AuthInfo> getAuthInfo(){
        return authInfoService.getAuthInfo();
    }

    @RequireAuth
    @PostMapping("/update")
    public Result<String> updateAuthInfo(@RequestBody AuthInfoDTO authInfoDTO){
        return authInfoService.updateAuthInfo(authInfoDTO);
    }
}
