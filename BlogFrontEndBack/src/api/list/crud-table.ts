interface CrudTableModel {
  id?: number
  /**
   * 名称
   */
  name: string
  /**
   * 值
   */
  value: string
  /**
   * 描述
   */
  remark?: string
}
import { request } from "../request"

export const getArticleManagerList = async () => {
  return request({
    url: '/article/managerList',
  })
}

type CrudTableParams = Partial<Omit<CrudTableModel, 'id'>>


export type {
  CrudTableModel,
  CrudTableParams,
}

