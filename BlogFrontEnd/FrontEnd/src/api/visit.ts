import { request } from "./request";

export const visit = async () => {
    return request({
        url: '/dailyStats/addVisit',
        method: "POST"
    })
}