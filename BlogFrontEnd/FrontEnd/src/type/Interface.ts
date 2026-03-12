export interface userInfo {
  id: string
  username: string
  nickname: string
  email: string
  avatar: string
  source: string
  statusText: string
  roleKey: string
  createTime: string
  loginTime: string
}

export interface userVO {
  id: string
  username: string
  nickname: string
  email: string
  avatar: string
  source: string
  statusText: string
  roleName: string
  roleKey: string
  createTime: string
  loginTime: string
}

export interface articleVO{
  id: string
  categoryName: string
  title: string
  summary: string
  thumbnail: string
  content: string
  isTop: number
  isComment: number
  viewCount: number
  createTime: string
  updateTime: string
}

export interface CommentVO {
  id: string              //评论Id
  articleId: string
  userId: string          //作者Id
  userName?: string    //作者昵称
  content: string
  createTime: string
  likeCount: number
  isLiked: boolean
  toUserId?: string         //回复 人id
  toUserName: string | undefined     //回复 人昵称
  //toCommentId?: string      //回复评论id
  children: CommentVO[]
  rootId?: string
  childCount: number        //回复数量 
  showReplies?: number  //是否显示回复
}

export interface CommentDTO {
  articleId: string
  userId: string
  userName: string
  content: string
  rootId: string
  toUserId: string
  toUserName: string
}

export interface AuthInfo {
  userName: string
  avatar: string
  profile: string
  slogan: string[]
}
