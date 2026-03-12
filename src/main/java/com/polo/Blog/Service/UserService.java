package com.polo.Blog.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.polo.Blog.Domain.DTO.LoginUserDTO;
import com.polo.Blog.Domain.DTO.PasswordUpdateDTO;
import com.polo.Blog.Domain.DTO.UserDTO;
import com.polo.Blog.Domain.Entity.User;
import com.polo.Blog.Domain.VO.UserVO;
import com.polo.Blog.Utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import me.zhyd.oauth.model.AuthCallback;

import java.io.IOException;

public interface UserService extends IService<User> {
    /**
     * 获取第三方登录授权地址
     * @param type 登录类型
     * @return 返回授权地址
     */
    Result<String> login(String type);

    /**
     * 处理第三方登录
     * @param type 登录类型
     * @param callback 回调
     * @param response
     * @return
     */
    void loginHandel(String type, AuthCallback callback, HttpServletResponse response) throws IOException;

    /**
     * 管理员登录
     * @param userDTO 登录对象
     * @return Token
     */
    Result adminLoginHandle(UserDTO userDTO);

    /**
     * 下线
     * @return 下线成功信息
     */
    Result<String> logoutHandle();

    /**
     * 获取删除用户
     * @param page 页数
     * @param size 页大小
     * @return 返回一页
     */
    Result<IPage<UserVO>> getDeletedUser(int page, int size);

    /**
     * 获取用户信息信息
     * @param id 前端传入的操作对象id
     * @return 返回VO给前端展示
     */
    Result<UserVO> getUserDetail(Long id);

    /**
     * 根据id获取用户详情
     * @param id 用户id
     * @return 返回用户详情
     */
    Result<UserVO> getUserDetailById(Long id);

    /**
     * 删除用户
     * @param id 用户id
     * @return 返回删除成功信息
     */
    Result<String> deleteUserById(Long id);

    /**
     * 分页获取用户列表
     * @param pageNum 页数
     * @param pageSize 页大小
     * @return 返回一页
     */
    Result<IPage<UserVO>> getUserList(int pageNum, int pageSize);

    /**
     * 分页获取搜索用户列表
     * @param pageNum 页数
     * @param pageSize 页大小
     * @param keyword 搜索词
     * @return 返回一页
     */
    Result<IPage<UserVO>> searchUserByKeyWord(int pageNum, int pageSize, String keyword);

    /**
     * 网站访问量 + 1 存入Redis
     */
    void visitMyBlog();

    /**
     * 更新用户信息
     * @param userDTO 用户
     * @return 返回更新成功信息
     */
    Result<String> updateUserInfo(UserDTO userDTO);

    /**
     * 修改当前登录用户密码
     * @param passwordUpdateDTO 密码信息
     * @return 返回处理结果
     */
    Result<String> updatePassword(PasswordUpdateDTO passwordUpdateDTO);
}
