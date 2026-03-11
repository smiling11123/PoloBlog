import { request } from "./request";

export const getRecentArticleDaily = async (articleId: number | string) => {
    return request({
        url: '/articleDaily/recent',
        method: 'GET',
        params: {
            articleId
        }
    })
}
