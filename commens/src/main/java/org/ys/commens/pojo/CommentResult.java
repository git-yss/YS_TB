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
    // 状态码
    private Integer status;
    // 返回数据
    private Object data;
    // 消息
    private String msg;

    public static CommentResult ok(){
        CommentResult br =new CommentResult();
        br.setStatus(200);
        br.setMsg("OK");
        return br;
    }

    public static CommentResult ok(Object data){
        CommentResult br =new CommentResult();
        br.setStatus(HttpStatus.OK.value());
        br.setMsg("OK");
        br.setData(data);
        return br;
    }

    public static CommentResult error(String msg){
        CommentResult br =new CommentResult();
        br.setStatus(HttpStatus.BAD_REQUEST.value());
        br.setMsg(msg);
        return br;
    }
}
