package com.polo.Blog.Domain.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("blog_article_tag")
public class ArticleTag {
    private Long articleId;
    private Long tagId;
}
