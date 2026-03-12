package com.polo.Blog.Service.Impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.polo.Blog.Domain.DTO.LoginUserDTO;
import com.polo.Blog.Domain.DTO.PasswordUpdateDTO;
import com.polo.Blog.Domain.DTO.UserDTO;
import com.polo.Blog.Domain.Entity.Role;
import com.polo.Blog.Domain.Entity.User;
import com.polo.Blog.Domain.Entity.UserRole;
import com.polo.Blog.Domain.VO.UserVO;
import com.polo.Blog.Mapper.UserMapper;
import com.polo.Blog.Service.RoleService;
import com.polo.Blog.Service.UserRoleService;
import com.polo.Blog.Service.UserService;
import com.polo.Blog.Utils.EntityListToVOList;
import com.polo.Blog.Utils.JwtUtils;
import com.polo.Blog.Utils.Result;
import com.polo.Blog.Utils.UserContext;
import jakarta.servlet.http.HttpServletResponse;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Objects;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private static final String OAUTH_TOKEN_COOKIE_NAME = "oauth_token_bridge";
    private static final Duration OAUTH_TOKEN_COOKIE_TTL = Duration.ofMinutes(2);
    @Autowired
    private RoleService roleService;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Value("${gitee.clientId}")
    private String giteeClientId;
    @Value("${gitee.clientSecret}")
    private String giteeClientSecret;
    @Value("${github.clientId}")
    private String githubClientId;
    @Value("${github.clientSecret}")
    private String githubClientSecret;
    @Value("${callback.url}")
    private String callbackUrl;
    @Value("${frontend.url:http://localhost:6678/}")
    private String frontendUrl;
    private AuthRequest getAuthRequest(String type){
        AuthRequest authRequest = null;
        switch (type){
            case "gitee" :
                if(giteeClientId == null || giteeClientId.isBlank() || giteeClientSecret == null || giteeClientSecret.isBlank()) {
                    return null;
                }
                authRequest = new AuthGiteeRequest(AuthConfig.builder()
                        .clientId(giteeClientId)
                        .clientSecret(giteeClientSecret)
                        .redirectUri(callbackUrl + "gitee")
                        .build());
                break;

            case  "github" :
                if(githubClientId == null || githubClientId.isBlank() || githubClientSecret == null || githubClientSecret.isBlank()) {
                    return null;
                }
                authRequest = new AuthGithubRequest(AuthConfig.builder()
                        .clientId(githubClientId)
                        .clientSecret(githubClientSecret)
                        .redirectUri(callbackUrl + "github")
                        .build());
                break;
        }
        return authRequest;
    }
    @Override
    public Result<String> login(String type){
        AuthRequest authRequest = getAuthRequest(type);
        if(authRequest == null) {
            return Result.fail(400, "当前登录方式未配置");
        }
        String url = authRequest.authorize(AuthStateUtils.createState());
        return Result.success(url);
    }

    @Override
    public void loginHandel(String type, AuthCallback callback, HttpServletResponse response) throws IOException {
        AuthRequest authRequest = getAuthRequest(type);
        if(authRequest == null) {
            clearOAuthTokenBridge(response);
            redirectToFrontend(response);
            return;
        }
        try {
            AuthResponse<AuthUser> authResponse = authRequest.login(callback);
            if(authResponse.ok()) {
                AuthUser authUser = authResponse.getData();
                String uuid = authUser.getUuid();
                String source = authUser.getSource();
                //构造新的数据传输对象并赋值
                LoginUserDTO loginUserDTO = new LoginUserDTO();
                BeanUtils.copyProperties(authUser, loginUserDTO);

                LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
                LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
                LambdaQueryWrapper<Role> roleWrapper = new LambdaQueryWrapper<>();
                wrapper.eq(User::getUuid, uuid).eq(User::getSource, source);
                User user = this.getOne(wrapper);
                if (user == null) {
                    user = new User();
                    BeanUtils.copyProperties(loginUserDTO, user);
                    user.setCreateTime(LocalDateTime.now());
                    user.setLoginDate(LocalDateTime.now());
                    user.setStatus("1");
                    this.save(user);
                    UserRole userRole = new UserRole();
                    userRole.setUserId(user.getId());
                    userRole.setRoleId(2L);
                    userRoleService.save(userRole);
                    //Redis记录新用户 + 1
                    String today = LocalDate.now().toString();
                    String key = "sys_daily_statistics:" + today + ":new_login_user";
                    redisTemplate.opsForValue().increment(key);
                } else {
                    user.setStatus("1");
                    user.setUsername(loginUserDTO.getUsername());
                    user.setAvatar(loginUserDTO.getAvatar());
                    user.setEmail(loginUserDTO.getEmail());
                    user.setLoginDate(LocalDateTime.now());
                    this.updateById(user);

                }
                //获取角色
                userRoleWrapper.eq(UserRole::getUserId, user.getId());
                UserRole userRole = userRoleService.getOne(userRoleWrapper);
                roleWrapper.eq(Role::getId, userRole.getRoleId());
                Role role = roleService.getOne(roleWrapper);
                //登录成功返回Token
                writeOAuthTokenBridge(response, JwtUtils.generateToken(user.getId(), role.getRoleKey()));
                redirectToFrontend(response);
                return;
            }
        } catch (Exception e) {
            clearOAuthTokenBridge(response);
            redirectToFrontend(response);
            return;
        }
        clearOAuthTokenBridge(response);
        redirectToFrontend(response);
    }

    @Override
    public Result adminLoginHandle(UserDTO userDTO){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userDTO.getUsername());//.eq(User::getPassword, loginUserDTO.getPassword());
        User user = this.getOne(wrapper);
        if(user == null){
            return new Result<>( 500, "用户名错误", "");
        }
        //密文比较
        if(!BCrypt.checkpw(userDTO.getPassword(), user.getPassword())){
            return new Result<>( 500, "密码错误", "");
        }
        //上线
        user.setStatus("1");
        user.setLoginDate(LocalDateTime.now());
        this.updateById(user);
        //获取角色权限
        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getUserId, user.getId());
        UserRole userRole = userRoleService.getOne(userRoleWrapper);
        if(userRole == null) return new Result<>(500, "成功响应", "登录失败");
        LambdaQueryWrapper<Role> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(Role::getId, userRole.getRoleId());
        Role role = roleService.getOne(roleWrapper);
        if(role == null || !Objects.equals(role.getRoleKey(), "admin")) return new Result<>(500, "成功响应", "登录失败");
        return new Result<>(200, "成功响应", JwtUtils.generateToken(user.getId(), role.getRoleKey()));
    }
    @Override
    public Result<String> logoutHandle(){
        UserContext.LoginUser loginUser = UserContext.get();
        User user = this.getById(loginUser.getId());
        user.setStatus("1");
        this.updateById(user);
        return Result.success("下线成功");
    }

    private void writeOAuthTokenBridge(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(OAUTH_TOKEN_COOKIE_NAME, token)
                .httpOnly(false)
                .secure(callbackUrl.startsWith("https://") || frontendUrl.startsWith("https://"))
                .sameSite("Lax")
                .path("/")
                .maxAge(OAUTH_TOKEN_COOKIE_TTL)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearOAuthTokenBridge(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(OAUTH_TOKEN_COOKIE_NAME, "")
                .httpOnly(false)
                .secure(callbackUrl.startsWith("https://") || frontendUrl.startsWith("https://"))
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void redirectToFrontend(HttpServletResponse response) throws IOException {
        response.sendRedirect(frontendUrl);
    }
    @Override
    public Result<IPage<UserVO>> getDeletedUser(int page, int size){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        IPage<User> pageInfo = new Page<>(page, size);
        wrapper.eq(User::getIsDeleted, 1);
        IPage<User> userIPage = this.page(pageInfo, wrapper);

        IPage<UserVO> userVOIPage = new Page<>();

        return Result.success(userVOIPage.setRecords(EntityListToVOList.userListToVOList(userIPage.getRecords())));

    }
    @Override
    public Result<UserVO> getUserDetail(Long id){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Role> roleWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId, id);
        UserVO userVO = new UserVO();
        User user = this.getOne(wrapper);

        //复制详细信息
        BeanUtils.copyProperties(user, userVO);
        //获取角色
        userRoleWrapper.eq(UserRole::getUserId, user.getId());
        long roleId = userRoleService.getOne(userRoleWrapper).getRoleId();
        roleWrapper.eq(Role::getId, roleId);
        Role role = roleService.getOne(roleWrapper);
        userVO.setRoleName(role.getRoleName());
        userVO.setRoleKey(role.getRoleKey());
        return Result.success(userVO);
    }
    @Override
    public Result<UserVO> getUserDetailById(Long id){
        UserContext.LoginUser loginUser = UserContext.get();
        if(!Objects.equals(loginUser.getRoleKey(), "admin")) return Result.success(new UserVO());
        User user = this.getById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        //查用户角色关联
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, user.getId());
        UserRole userRole = userRoleService.getOne(wrapper);
        //查角色
        LambdaQueryWrapper<Role> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(Role::getId, userRole.getRoleId());
        Role role = roleService.getOne(roleWrapper);
        //复制给VO
        userVO.setRoleName(role.getRoleName());
        userVO.setRoleKey(role.getRoleKey());
        return Result.success(userVO);
    }
    @Override
    public Result<String> deleteUserById(Long id){
        UserContext.LoginUser loginUser = UserContext.get();
        if(!Objects.equals(loginUser.getRoleKey(), "admin")) return Result.fail(403, "权限不足");
        User user = this.getById(id);
        user.setIsDeleted(1);
        this.updateById(user);
        return Result.success("删除成功");
    }

    @Override
    public Result<IPage<UserVO>> getUserList(int pageNum, int pageSize){
        //分页
        Page<User> pageInfo = new Page<>(pageNum, pageSize);
        UserContext.LoginUser loginUser = UserContext.get();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if(!Objects.equals(loginUser.getRoleKey(), "admin")) {
            return Result.success(new Page<>());
        }
        wrapper.eq(User::getIsDeleted, 0);
        //按浏览量降序
        IPage<User> result = this.page(pageInfo, wrapper);
        IPage<UserVO> userVOList = new Page<>();

        BeanUtils.copyProperties(result, userVOList);
        return Result.success(userVOList.setRecords(EntityListToVOList.userListToVOList(pageInfo.getRecords())));
    }

    @Override
    public Result<IPage<UserVO>> searchUserByKeyWord(int pageNum, int pageSize, String keyword){
        //分页
        Page<User> pageInfo = new Page<>(pageNum, pageSize);
        UserContext.LoginUser loginUser = UserContext.get();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if(!Objects.equals(loginUser.getRoleKey(), "admin")) {
            return Result.success(new Page<>());
        }
        wrapper.eq(User::getIsDeleted, 0);
        wrapper.like(User::getUsername, keyword);
        //按浏览量降序
        IPage<User> result = this.page(pageInfo, wrapper);
        IPage<UserVO> userVOList = new Page<>();

        BeanUtils.copyProperties(result, userVOList);
        return Result.success(userVOList.setRecords(EntityListToVOList.userListToVOList(pageInfo.getRecords())));
    }

    @Override
    public void visitMyBlog(){
        String today = LocalDate.now().toString();
        String key = "sys_daily_statistics:" + today + ":view_count";
        redisTemplate.opsForValue().increment(key);
    }

    @Override
    public Result<String> updateUserInfo(UserDTO userDTO){
        UserContext.LoginUser loginUser = UserContext.get();
        if(!loginUser.getRoleKey().equals( "admin")) return Result.fail(403, "权限不足");
        User user = this.getById(loginUser.getId());
        BeanUtils.copyProperties(userDTO, user);
        user.setUpdateTime(LocalDateTime.now());
        this.updateById(user);
        return Result.success("更新成功");
    }

    @Override
    public Result<String> updatePassword(PasswordUpdateDTO passwordUpdateDTO) {
        UserContext.LoginUser loginUser = UserContext.get();
        User user = this.getById(loginUser.getId());
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        if (!StringUtils.hasText(passwordUpdateDTO.getOldPassword()) || !StringUtils.hasText(passwordUpdateDTO.getNewPassword())) {
            return Result.fail(400, "密码不能为空");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            return Result.fail(400, "当前账号暂不支持修改本地密码");
        }
        if (!BCrypt.checkpw(passwordUpdateDTO.getOldPassword(), user.getPassword())) {
            return Result.fail(400, "原密码错误");
        }
        if (passwordUpdateDTO.getNewPassword().length() < 6) {
            return Result.fail(400, "新密码长度不能少于6位");
        }
        if (Objects.equals(passwordUpdateDTO.getOldPassword(), passwordUpdateDTO.getNewPassword())) {
            return Result.fail(400, "新密码不能与原密码相同");
        }

        user.setPassword(BCrypt.hashpw(passwordUpdateDTO.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        this.updateById(user);
        return Result.success("密码修改成功");
    }
}
