package org.ys.commens.pojo;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * 服务端和客户端数据交换的模板类
 */
@Data
public class CommentResult implements Serializable {
    private static final long serialVersionUID=1l;
    /** 与 HTTP 语义一致的状态码，兼容旧客户端 */
    private Integer status;
    /** 业务码：200 成功，4xx 客户端错误，5xx 服务端错误（与前端 axios 拦截器对齐） */
    private Integer code;
    // 返回数据
    private Object data;
    // 消息
    private String msg;

    public static CommentResult success(){
        CommentResult br =new CommentResult();
        br.setStatus(200);
        br.setCode(200);
        br.setMsg("OK");
        return br;
    }

    public static CommentResult success(Object data){
        CommentResult br =new CommentResult();
        br.setStatus(HttpStatus.OK.value());
        br.setCode(200);
        br.setMsg("OK");
        br.setData(data);
        return br;
    }

    public static CommentResult error(String msg){
        CommentResult br =new CommentResult();
        br.setStatus(HttpStatus.BAD_REQUEST.value());
        br.setCode(HttpStatus.BAD_REQUEST.value());
        br.setMsg(msg);
        return br;
    }
}
