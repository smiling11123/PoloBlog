import { request } from "./request";

export interface AuthInfoDTO {
    userName: string;
    avatar: string;
    profile: string;
    slogan: string;
}

export const getAuthInfo = async () => {
    return request({
        url: '/authInfo/get',
        method: 'get'
    });
}

export const updateAuthInfo = async (data: AuthInfoDTO) => {
    return request({
        url: '/authInfo/update',
        method: 'post',
        data
    });
}
