package cn.edu.zjut.weining.anti.rule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author panda421
 * @since 2021/7/8
 */
public abstract class AbstractRule implements AntiSpiderRule {


    @Override
    public boolean execute(HttpServletRequest request, HttpServletResponse response) {
        return doExecute(request,response);
    }

    protected abstract boolean doExecute(HttpServletRequest request, HttpServletResponse response);
}
