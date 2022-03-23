package cn.edu.zjut.weining.anti.servlet;

import cn.edu.zjut.weining.anti.ValidateFormService;
import cn.edu.zjut.weining.anti.util.CrosUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author panda421
 * @since 2021/7/10
 */
public class RefreshFormServlet extends HttpServlet {

    private ValidateFormService validateFormService;

    private AtomicBoolean initialized = new AtomicBoolean(false);

    private synchronized void init(ServletContext servletContext) {
        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        assert ctx != null;
        this.validateFormService = ctx.getBean(ValidateFormService.class);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!initialized.get()) {
            init(request.getServletContext());
            initialized.set(true);
        }
        String result = validateFormService.refresh(request);
        CrosUtil.setCrosHeader(response);
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(200);
        response.getWriter().write(result);
        response.getWriter().close();
        return;
    }
}
