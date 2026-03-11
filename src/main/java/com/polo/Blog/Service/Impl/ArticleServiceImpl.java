package com.polo.Blog.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.polo.Blog.Domain.DTO.ArticleDTO;
import com.polo.Blog.Domain.Entity.*;
import com.polo.Blog.Domain.VO.ArticleVO;
import com.polo.Blog.Mapper.ArticleMapper;
import com.polo.Blog.Service.*;
import com.polo.Blog.Utils.EntityListToVOList;
import com.polo.Blog.Utils.RedisCache;
import com.polo.Blog.Utils.Result;
import com.polo.Blog.Utils.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {
    @Autowired
    private ArticleTagService articleTagService;
    @Autowired
    private UserService userService;
    @Autowired
    private SearchHistoryService searchHistoryService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DailyStatsService dailyStatsService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedisCache redisCache;

    @Override
    public Result<IPage<ArticleVO>> getArticleList(int page, int size) {
        UserContext.LoginUser loginUser = UserContext.get();
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        //不是管理员就过滤草稿和已删除的文章
//        if(!Objects.equals(loginUser.getRoleKey(), "admin")) {
        wrapper.eq(Article::getStatus, 1).eq(Article::getIsDeleted, 0);
//        }
        //创建分页
        Page<Article> pageInfo = new Page<>(page, size);
        //按更新时间排序
        wrapper.orderByDesc(Article::getUpdateTime);
        IPage<Article> articleIPage = this.page(pageInfo, wrapper);
        IPage<ArticleVO> articleVOIPage = new Page<>();
        return Result.success(articleVOIPage.setRecords(EntityListToVOList.articleListToVOList(articleIPage.getRecords(), userService, categoryService)));
    }
    @Override
    public Result<IPage<ArticleVO>> getDeletedArticleList(int page, int size){
        UserContext.LoginUser loginUser = UserContext.get();
        if(!loginUser.getRoleKey().equals("admin")) return Result.success(new Page<>());
        Page<Article> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsDeleted, 1);

        Page<Article> articlePage = this.page(pageInfo, wrapper);
        Page<ArticleVO> articleVOPage = new Page<>();
        return Result.success(articleVOPage.setRecords(EntityListToVOList.articleListToVOList(articlePage.getRecords(), userService, categoryService)));

    }
    @Override
    public Result<ArticleVO> getArticleById(Long id) {
        UserContext.LoginUser loginUser = UserContext.get();
        if (loginUser == null) {
            loginUser = new UserContext.LoginUser(null, "user");
        }
        /// 先查Redis
        String articleCacheKey = "ArticleDetail_" + id;
        ArticleVO articleCache = redisCache.get(articleCacheKey, ArticleVO.class);
        if(articleCache != null) return Result.success(articleCache);
        /// redis未命中
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getId, id);
        Article article = this.getOne(wrapper);
        /// 数据库中未空
        if(article == null){
            redisCache.set(articleCacheKey, null, 5, TimeUnit.MINUTES);
            return Result.success(new ArticleVO());
        }
        boolean isPublicArticle = isPublicArticle(article);
        boolean canViewPrivateArticle = canManageArticle(loginUser, article);
        if(!isPublicArticle && !canViewPrivateArticle){
            return Result.success(new ArticleVO());
        }
        ArticleVO articleVO = new ArticleVO();
        BeanUtils.copyProperties(article, articleVO);
        //获取作者名
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getId, article.getCreateBy());
        User user = userService.getOne(userWrapper);
        articleVO.setAuth(user.getUsername());
        if(article.getCategoryId() != null){
            //分类
            LambdaQueryWrapper<Category> categoryWrapper = new LambdaQueryWrapper<>();
            categoryWrapper.eq(Category::getId, articleVO.getCategoryId());
            Category category = categoryService.getOne(categoryWrapper);
            articleVO.setCategoryName(category.getName());
        }
        if(isPublicArticle){
            /// 写入Redis缓存
            redisCache.set(articleCacheKey, articleVO, 30, TimeUnit.MINUTES);
            //浏览量 + 1 写入Redis
            String today = LocalDate.now().toString();
            String key = "blog_article_daily_stats:" + today + ":view_count";
            String dailyKey = "sys_daily_statistics:" + today + ":view_count";

            redisTemplate.opsForValue().increment(dailyKey);
            redisTemplate.opsForHash().increment(key, String.valueOf(articleVO.getId()),1);
            String articleViewCountBuffer = "blog_article_view_count";
            redisTemplate.opsForHash().increment( articleViewCountBuffer, String.valueOf(articleVO.getId()), 1);
        }
        return Result.success(articleVO);
    }

    @Override
    public Result<IPage<ArticleVO>> getArticleByKeyWord(String keyword, List<Long> categoryId, int page, int size){
        UserContext.LoginUser loginUser = UserContext.get();
        if(loginUser == null){
            loginUser = new UserContext.LoginUser(null, "user");
        }
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        Page<Article> pageInfo = new Page<>(page, size);
        if(keyword != null && !keyword.isEmpty()){
            wrapper.like(Article::getTitle, keyword);
        }

        if(!Objects.equals(loginUser.getRoleKey(), "admin")) {
            wrapper.eq(Article::getStatus, 1).eq(Article::getIsDeleted, 0);
        }
        if(categoryId != null && !categoryId.isEmpty()){
            wrapper.in(Article::getCategoryId, categoryId);
        }
        IPage<Article> articleIPage = this.page(pageInfo, wrapper);
        IPage<ArticleVO> articleVOIPage = new Page<>();
        BeanUtils.copyProperties(articleIPage, articleVOIPage);
        //记录搜索记录
        if(keyword != null && !keyword.isEmpty()) {
            redisTemplate.opsForZSet().incrementScore("search_keyword", keyword, 1);
        }
        return Result.success(articleVOIPage.setRecords(EntityListToVOList.articleListToVOList(articleIPage.getRecords(), userService, categoryService)));
    }

    @Override
    public Result<IPage<ArticleVO>> getArticleByTag(List<String> tagId, int page, int size){
        UserContext.LoginUser loginUser = UserContext.get();
        LambdaQueryWrapper<ArticleTag> articleTagwrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        Page<Article> pageInfo = new Page<>(page, size);
        //查询对应文章id
        articleTagwrapper.in(ArticleTag::getTagId, tagId);
        List<ArticleTag> ArticleTagList =  articleTagService.list(articleTagwrapper);
        if(ArticleTagList.isEmpty()){
            return Result.success(new Page<>());
        }
        //处理关联关系，集合去重提取文章id
        Set<Long> ArticleIdList = ArticleTagList.stream().map(ArticleTag::getArticleId).collect(Collectors.toSet());
        //查询文章
        wrapper.in(Article::getId, ArticleIdList);
        if(!Objects.equals(loginUser.getRoleKey(), "admin")) {
            wrapper.eq(Article::getStatus, 1).eq(Article::getIsDeleted, 0);
        }
        //按更新时间排序
        wrapper.orderByDesc(Article::getUpdateTime);
        this.page(pageInfo, wrapper);
        IPage<ArticleVO> articleVOIPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, articleVOIPage);
        return Result.success(articleVOIPage.setRecords(EntityListToVOList.articleListToVOList(pageInfo.getRecords(), userService, categoryService)));
    }

    @Override
    public Result<IPage<ArticleVO>> getArticleByCategory(Long categoryId, int page, int size){
        UserContext.LoginUser loginUser = UserContext.get();
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        Page<Article> pageInfo = new Page<>(page, size);
        //查询对应文章id
        //查询文章
        wrapper.eq(Article::getCategoryId, categoryId);
        if(!Objects.equals(loginUser.getRoleKey(), "admin")) {
            wrapper.eq(Article::getStatus, 1).eq(Article::getIsDeleted, 0);
        }
        //按更新时间排序
        wrapper.orderByDesc(Article::getUpdateTime);
        this.page(pageInfo, wrapper);
        IPage<ArticleVO> articleVOIPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, articleVOIPage);
        String articleViewCountBuffer = "blog_category_view_count";
        redisTemplate.opsForHash().increment( articleViewCountBuffer, String.valueOf(categoryId), 1);
        return Result.success(articleVOIPage.setRecords(EntityListToVOList.articleListToVOList(pageInfo.getRecords(), userService, categoryService)));
    }
    @Override
    public Result<IPage<ArticleVO>> getHotArticle(int page, int size){
        /// 查redis
        String key = "hotArticleList_" + page + "_" + size;
        Page<ArticleVO> hotArticleCache = redisCache.get(key, new TypeReference<Page<ArticleVO>>() {});
        if(hotArticleCache != null) return Result.success(hotArticleCache);
        /// redis未命中
        //分页
        Page<Article> pageInfo = new Page<>(page, size);
        UserContext.LoginUser loginUser = UserContext.get();
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1).eq(Article::getIsDeleted, 0);
        ///按浏览量降序
        wrapper.orderByDesc(Article::getViewCount);
        IPage<Article> articleIPage = this.page(pageInfo, wrapper);
        IPage<ArticleVO> articleVOIPage = new Page<>();

        BeanUtils.copyProperties(articleIPage, articleVOIPage);
        articleVOIPage.setRecords(EntityListToVOList.articleListToVOList(articleIPage.getRecords(), userService, categoryService));
        /// redis缓存
        redisCache.set(key, articleVOIPage, 30, TimeUnit.MINUTES);
        return  Result.success(articleVOIPage);

    }

    //==============管理员权限===================================//

    @Override
    public Result<IPage<ArticleVO>> getManuscriptList(int page, int size){
        UserContext.LoginUser loginUser = UserContext.get();
        if(!Objects.equals(loginUser.getRoleKey(), "admin")) return Result.success(new Page<>());
        Page<Article> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 0); //选择草稿
        wrapper.orderByDesc(Article::getUpdateTime); //按更新时间排序
        Page<Article> articlePage = this.page(pageInfo, wrapper);
        IPage<ArticleVO> articleVOIPage = new Page<>();
        BeanUtils.copyProperties(articlePage, articleVOIPage);

        return Result.success(articleVOIPage.setRecords(EntityListToVOList.articleListToVOList(articlePage.getRecords(), userService, categoryService)));
    }

    @Override
    @Transactional(rollbackFor =  Exception.class)
    public Result<String> publishArticle(ArticleDTO articleDTO){
        UserContext.LoginUser loginUser = UserContext.get();
        if(Objects.equals(loginUser.getRoleKey(), "user")) return Result.fail(403, "权限不足");
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId, loginUser.getId());
        User user = userService.getOne(wrapper);
        /*
          管理员Token校验
         */
        Article article = new Article();
        BeanUtils.copyProperties(articleDTO, article);
        //填写系统生成数据
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        //创建人
        article.setCreateBy(user.getId());
        if(articleDTO.getCategoryName() != null && !articleDTO.getCategoryName().trim().isEmpty()){
            //分类
            LambdaQueryWrapper<Category> categoryWrapper = new LambdaQueryWrapper<>();
            categoryWrapper.eq(Category::getName, articleDTO.getCategoryName().trim())
                           .orderByAsc(Category::getIsDeleted) // 优先找没删除的
                           .last("limit 1");
            Category category = categoryService.getOne(categoryWrapper, false);
            if(category == null){
                //创建分类
                category = new Category();
                category.setName(articleDTO.getCategoryName().trim());
                category.setCreateTime(LocalDateTime.now());
                category.setUpdateTime(LocalDateTime.now());
                category.setIsDeleted(0);
                categoryService.save(category);
                String key = "categoryList";
                redisCache.deleteCache(key);
            }
            else if (category.getIsDeleted() == 1) {
                category.setIsDeleted(0);
                categoryService.updateById(category);
            }
            article.setCategoryId(category.getId());
        } else {
            article.setCategoryId(null);
        }

        this.save(article);
        //只有发布才记录redis
        if(article.getStatus() == 1){
            // 处理表关联
            //新发布 + 1 记录到Redis
            String key = LocalDate.now().toString();
            redisTemplate.opsForValue().increment(key);
        }
        return Result.success("发布成功");
    }

    @Override
    public Result<String> manuscriptToArticle(ArticleDTO articleDTO){
        UserContext.LoginUser loginUser = UserContext.get();
        if(!Objects.equals(loginUser.getRoleKey(), "admin")) return Result.fail(403, "权限不足");
        Article article = this.getById(articleDTO.getId());
        article.setStatus(1);
        return Result.success("发布成功");
    }

    @Override
    @Transactional(rollbackFor =  Exception.class)
    public Result<String> updateArticle(ArticleDTO articleDTO){
        UserContext.LoginUser loginUser = UserContext.get();
        //权限校验
        if(Objects.equals(loginUser.getRoleKey(), "user")) return Result.fail(403, "权限不足");
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId, loginUser.getId());
        User user = userService.getOne(wrapper);
        if(user == null) return Result.fail(401, "请先登录");
        //作者id校验
        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.eq(Article::getId, articleDTO.getId());
        Article article = this.getOne(articleWrapper);
        //判断是不是草稿转发布
        boolean isPublish = articleDTO.getStatus() == 1 && article.getStatus() == 0;
        if(article == null || (!Objects.equals(article.getCreateBy(), user.getId()) && !Objects.equals(loginUser.getRoleKey(), "admin"))) return Result.fail(403, "权限不足");
        //更新文章
        BeanUtils.copyProperties(articleDTO, article);
        //更新时间
        article.setUpdateTime(LocalDateTime.now());
        //分类
        if(articleDTO.getCategoryName() != null && !articleDTO.getCategoryName().trim().isEmpty()){
            LambdaQueryWrapper<Category> categoryWrapper = new LambdaQueryWrapper<>();
            categoryWrapper.eq(Category::getName, articleDTO.getCategoryName().trim())
                           .orderByAsc(Category::getIsDeleted)
                           .last("limit 1");
            Category category = categoryService.getOne(categoryWrapper, false);
            if(category == null){
                //创建分类
                category = new Category();
                category.setName(articleDTO.getCategoryName().trim());
                category.setCreateTime(LocalDateTime.now());
                category.setUpdateTime(LocalDateTime.now());
                category.setIsDeleted(0);
                categoryService.save(category);
                String key = "categoryList";
                redisCache.deleteCache(key);
            }
            else if (category.getIsDeleted() == 1) {
                category.setIsDeleted(0);
                categoryService.updateById(category);
            }
            article.setCategoryId(category.getId());
        } else {
            article.setCategoryId(null);
        }
        this.updateById(article);
        //草稿转发布就记录新发布到Redis
        if(isPublish){
            //新发布 + 1 记录到Redis
            String today = LocalDate.now().toString();
            String key = "sys_daily_statistics:" + today + ":new_article_count";
            redisTemplate.opsForValue().increment(key);
        }
        /// 删除Redis缓存
        redisCache.deleteCache("ArticleDetail_" + article.getId());
        // 处理表关联
        return Result.success("更新成功");
    }

    @Override
    @Transactional(rollbackFor =  Exception.class)
    public Result<String> deleteArticle(Long id){
        UserContext.LoginUser loginUser = UserContext.get();
        Article article = this.getById(id);
        if(article == null || article.getIsDeleted() == 1) return Result.fail(404, "文章不存在");
        if(!canManageArticle(loginUser, article)) return Result.fail(403, "权限不足");
        article.setIsDeleted(1);
        this.updateById(article);
        /// 删除Redis缓存
        redisCache.deleteCache("ArticleDetail_" + article.getId());
        return Result.success("删除成功");
    }
    @Override
    public void updateViewCountDaily(Long id, Integer viewCount){
        LambdaUpdateWrapper<Article> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Article::getId, id).setSql("view_count = view_count + " + viewCount);
        //第一个参数null 表示不更新其他字段
        this.update(null, wrapper);
    }

    @Override
    public Result<String> toggleCommentStatus(Long id) {
        UserContext.LoginUser loginUser = UserContext.get();
        Article article = this.getById(id);
        if (article == null) return Result.fail(404, "文章不存在");
        if(!canManageArticle(loginUser, article)) return Result.fail(403, "权限不足");
        // 0禁止 <-> 1允许
        article.setIsComment(article.getIsComment() == 1 ? 0 : 1);
        this.updateById(article);
        // 清除文章详情 Redis 缓存
        redisCache.deleteCache("ArticleDetail_" + id);
        String message = article.getIsComment() == 1 ? "已允许评论" : "已禁止评论";
        return Result.success(message);
    }

    private boolean canManageArticle(UserContext.LoginUser loginUser, Article article) {
        if(loginUser == null || article == null) return false;
        return Objects.equals(loginUser.getRoleKey(), "admin") || Objects.equals(article.getCreateBy(), loginUser.getId());
    }

    private boolean isPublicArticle(Article article) {
        return article != null && Objects.equals(article.getStatus(), 1) && Objects.equals(article.getIsDeleted(), 0);
    }
}
