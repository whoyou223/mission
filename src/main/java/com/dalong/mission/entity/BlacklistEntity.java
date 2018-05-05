package com.dalong.mission.entity;

import lombok.Data;

/**
 * 黑名单实体
 *
 * @author dalong
 * @summary
 * @since 2018-05-04 上午10:33
 **/
@Data
public class BlacklistEntity {

    /**
     * 类型，1-添加，2-删除
     */
    private int type;

    /**
     * 执行任务的类名
     */
    private String className;

    /**
     * 执行任务的方法名
     */
    private String methodName;

    public BlacklistEntity(){}


    @Override
    public int hashCode(){
        return 3 * className.hashCode() + 7 * methodName.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }

        if (obj instanceof BlacklistEntity){
            BlacklistEntity entity = (BlacklistEntity) obj;
            return entity.methodName.equals(this.methodName) && entity.className.equals(this.className);
        }else {
            return false;
        }
    }

}
