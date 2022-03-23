package cn.edu.zjut.weining.anti.rule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author panda421
 * @since 2021/7/8
 */
public interface AntiSpiderRule {
    /**
     * 反爬规则具体实现
     * @param request 请求
     * @param response 响应
     * @return true为击中反爬规则
     */
    boolean execute(HttpServletRequest request, HttpServletResponse response);

    /**
     * 重置已记录规则
     * @param request 请求
     * @param realRequestUri 原始请求uri
     */
    void reset(HttpServletRequest request, String realRequestUri);

    /**
     * 规则优先级
     * @return 优先级
     */
    int getOrder();
}
