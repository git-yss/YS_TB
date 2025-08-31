package org.ys.commens.entity;

/**
 * (YsUserAddr)表实体类
 *
 * @author makejava
 * @since 2025-07-16 19:41:46
 */
@SuppressWarnings("serial")
public class YsUserAddr{
    //ID
    private Long id;
    //用户id
    private Long userId;
    //地址
    private String addr;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

}

