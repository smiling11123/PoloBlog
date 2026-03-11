package com.polo.Blog.Interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polo.Blog.Annootation.RequireAuth;
import com.polo.Blog.Utils.JwtUtils; // 假设你有这个工具类
import com.polo.Blog.Utils.Result;
import com.polo.Blog.Utils.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 放行 OPTIONS 请求 (关键！否则跨域会报错)
        // 浏览器发正式请求前会发一个 OPTIONS 预检请求，这个请求没有 Token，必须直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        boolean isRequireAuth = false; //接口默认不需要登录
        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            //获取注解判断是否需要登录访问
            isRequireAuth = handlerMethod.getMethodAnnotation(RequireAuth.class) != null || handlerMethod.getBeanType().getAnnotation(RequireAuth.class) != null;
        }

        // 2. 获取 Token
        String token = request.getHeader("Authorization"); // 前端 request.ts 里设置的 Key

        // 3. 校验 Token 是否存在
        if (!StringUtils.hasText(token)) {
            //如果接口不需要登录，直接放行
            if(!isRequireAuth){
                UserContext.set(new UserContext.LoginUser(null, "user"));
                return true;
            }
            //response.setStatus(401); // 设置状态码return false; // 拦截，不让进 Controller
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.setContentType("application/json;charset=UTF-8");

            Result<String> failResult = Result.fail(401, "请登录后操作");
            response.getWriter().write(objectMapper.writeValueAsString(failResult));
            return false;
        }

        try {
            // 4. 解析 Token (这里会抛异常如果 Token 过期或被篡改)
            // 假设你的 Token 载荷(Claims)里存的是 Subject = id
            Claims claims = JwtUtils.parseToken(token);
            String id = null;
            String roleKey = null;
            if (claims != null) {
                id = claims.getSubject();
                roleKey = claims.get("role", String.class);
            }

            // 5. 存入 ThreadLocal，供后续 Controller 使用
            if (id != null) {
                UserContext.set(new UserContext.LoginUser(Long.parseLong(id), roleKey));
                return true; // 放行
            }
            if(!isRequireAuth){
                UserContext.set(new UserContext.LoginUser(null, "user"));
                return true;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            Result<String> failResult = Result.fail(401, "登录状态无效，请重新登录");
            response.getWriter().write(objectMapper.writeValueAsString(failResult));
            return false;
        } catch (Exception e) {
            if(!isRequireAuth){
                UserContext.set(new UserContext.LoginUser(null, "user"));
                return true;
            }
            // 步骤 A: 设置 HTTP 状态码为 401 (让前端 Axios 进入 error 回调)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            // 步骤 B: 设置响应类型为 JSON
            response.setContentType("application/json;charset=UTF-8");

            // 步骤 C: 写入标准的 JSON 错误信息 (可选，为了前端展示更好看)
            Result<String> failResult = Result.fail(401, "登录已过期，请重新登录");
            response.getWriter().write(objectMapper.writeValueAsString(failResult));
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 6. 必须清理 ThreadLocal，防止内存泄漏
        UserContext.remove();
    }
}

