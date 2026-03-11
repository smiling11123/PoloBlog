package com.polo.Blog.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.polo.Blog.Annootation.RequireAuth;
import com.polo.Blog.Domain.DTO.LoginUserDTO;
import com.polo.Blog.Domain.DTO.UserDTO;
import com.polo.Blog.Domain.VO.UserVO;
import com.polo.Blog.Service.UserService;
import com.polo.Blog.Utils.Result;
import com.polo.Blog.Utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/user")
@RestController
@RequireAuth
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/detail")
    public Result<UserVO> getUserDetail(){
        return userService.getUserDetail(UserContext.get().getId());
    }
    @GetMapping("/deletedUserList")
    public Result<IPage<UserVO>> getDeleteUserList(@RequestParam (defaultValue = "1") int page, @RequestParam (defaultValue = "20") int size){
        return userService.getDeletedUser(page, size);
    }
    @GetMapping("/list")
    public Result<IPage<UserVO>> getUserList(@RequestParam (defaultValue = "1") int page, @RequestParam (defaultValue = "20") int size){
        return userService.getUserList(page, size);
    }

    @GetMapping("/detailById")
    public Result<UserVO> getUserDetailById(@RequestParam Long id){
        return userService.getUserDetailById(id);
    }

    @PostMapping("/deleteById")
    public Result<String> deleteUserById(@RequestParam Long id){
        return userService.deleteUserById(id);
    }

    @GetMapping("/search")
    public Result<IPage<UserVO>> searchUserByKeyWord(@RequestParam (defaultValue = "1") int page, @RequestParam (defaultValue = "20") int size, @RequestParam String keyword){
        return userService.searchUserByKeyWord(page, size, keyword);
    }

    @PostMapping("/update")
    public Result<String> updateUserInfo(@RequestBody UserDTO userDTO){
        return userService.updateUserInfo(userDTO);
    }
}

