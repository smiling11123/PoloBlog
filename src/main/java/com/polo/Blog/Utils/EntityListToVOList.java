package com.polo.Blog.Utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.polo.Blog.Domain.Entity.Article;
import com.polo.Blog.Domain.Entity.Comment;
import com.polo.Blog.Domain.Entity.User;
import com.polo.Blog.Domain.Entity.Works;
import com.polo.Blog.Domain.VO.ArticleVO;
import com.polo.Blog.Domain.VO.CommentVO;
import com.polo.Blog.Domain.VO.UserVO;
import com.polo.Blog.Domain.VO.WorksVO;
import com.polo.Blog.Domain.Entity.Category;
import com.polo.Blog.Service.CategoryService;
import com.polo.Blog.Service.UserService;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 实体类文章数据转换成给前端的数据
 */
public class EntityListToVOList {

    public static List<ArticleVO> articleListToVOList(List<Article> articles, UserService userService, CategoryService categoryService){

        List<ArticleVO> articleVOList = new ArrayList<>();
        //转换成 VO
        for(Article article : articles){
            ArticleVO articleVO = new ArticleVO();
            BeanUtils.copyProperties(article, articleVO);
            //获取作者名
            if (article.getCreateBy() != null) {
                User user = userService.getById(article.getCreateBy());
                if (user != null) {
                    articleVO.setAuth(user.getUsername());
                }
            }
            // 获取分类名
            if (article.getCategoryId() != null) {
                Category category = categoryService.getById(article.getCategoryId());
                if (category != null) {
                    articleVO.setCategoryName(category.getName());
                }
            }
            articleVO.setContent(null);
            articleVOList.add(articleVO);
        }
        return articleVOList;
    }

    public static List<UserVO> userListToVOList(List<User> users){

        List<UserVO> userVOList = new ArrayList<>();
        //转换成 VO
        for(User user : users){
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            userVOList.add(userVO);
        }
        return userVOList;
    }

    public static  List<CommentVO> commentListToVOList(List<Comment> comments){
        List<CommentVO> commentVOList = new ArrayList<>();
        for(Comment comment : comments){
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(comment, commentVO);
            commentVOList.add(commentVO);
        }
        return commentVOList;
    }
    public static  List<WorksVO> worksListToVOList(List<Works> works){
        List<WorksVO> worksVOList = new ArrayList<>();
        for(Works work : works){
            WorksVO worksVO = new WorksVO();
            BeanUtils.copyProperties(work, worksVO);
            worksVOList.add(worksVO);
        }
        return worksVOList;
    }
}
