package org.ys.transaction.domain.entity;

import lombok.Getter;

/**
 * (YsUserAddr)表实体类
 *
 * @author makejava
 * @since 2025-07-16 19:41:46
 */
@Getter
public class YsUserAddr{
    //ID
    private Long id;
    //用户id
    private Long userId;
    //地址
    private String addr;

    private YsUserAddr(Long id, Long userId, String addr) {
        this.id = id;
        this.userId = userId;
        this.addr = addr;
    }

    public static YsUserAddr rehydrate(Long id, Long userId, String addr) {
        return new YsUserAddr(id, userId, addr);
    }

    public void updateAddr(String addr) {
        if (addr == null ) {
            throw new IllegalArgumentException("addr cannot be empty");
        }
        this.addr = addr;
    }



}

