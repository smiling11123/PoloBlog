import { request } from "./request";

export const getDailyStats = async () => {
    return request({
        url: '/dailyStats/recent',
        method: 'GET'
    })
}