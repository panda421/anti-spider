package cn.edu.zjut.weining.anti.interceptor;

import cn.edu.zjut.weining.anti.annotation.AntiSpider;
import cn.edu.zjut.weining.anti.config.AntiSpiderProperties;
import cn.edu.zjut.weining.anti.module.VerifyImageDTO;
import cn.edu.zjut.weining.anti.rule.RuleActuator;
import cn.edu.zjut.weining.anti.util.CrosUtil;
import cn.edu.zjut.weining.anti.util.VerifyImageUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * @author panda421
 * @since 2020/2/4
 */
public class AntiSpiderInterceptor extends HandlerInterceptorAdapter {


    private String AntiSpiderForm;

    private RuleActuator actuator;

    private List<String> includeUrls;

    private boolean globalFilterMode;

    private VerifyImageUtil verifyImageUtil;

    private AtomicBoolean initialized = new AtomicBoolean(false);

    public void init(ServletContext context) {
        ClassPathResource classPathResource = new ClassPathResource("verify/index.html");
        try {
            classPathResource.getInputStream();
            byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            this.AntiSpiderForm = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("反爬虫验证模板加载失败！");
            e.printStackTrace();
        }
        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
        assert ctx != null;
        this.actuator = ctx.getBean(RuleActuator.class);
        this.verifyImageUtil = ctx.getBean(VerifyImageUtil.class);
        this.includeUrls = ctx.getBean(AntiSpiderProperties.class).getIncludeUrls();
        this.globalFilterMode = ctx.getBean(AntiSpiderProperties.class).isGlobalFilterMode();
        if (this.includeUrls == null) {
            this.includeUrls = new ArrayList<>();
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!initialized.get()) {
            init(request.getServletContext());
            initialized.set(true);
        }
        HandlerMethod handlerMethod;
        try {
            handlerMethod = (HandlerMethod) handler;
        } catch (ClassCastException e) {
            return true;
        }
        Method method = handlerMethod.getMethod();
        AntiSpider AntiSpider = AnnotationUtils.findAnnotation(method, AntiSpider.class);
        boolean isAntiSpiderAnnotation = AntiSpider != null;
        String requestUrl = request.getRequestURI();
        if (isIntercept(requestUrl, isAntiSpiderAnnotation) && !actuator.isAllowed(request, response)) {
            CrosUtil.setCrosHeader(response);
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(509);
            VerifyImageDTO verifyImage = verifyImageUtil.generateVerifyImg();
            verifyImageUtil.saveVerifyCodeToRedis(verifyImage);
            String str1 = this.AntiSpiderForm.replace("verifyId_value", verifyImage.getVerifyId());
            String str2 = str1.replaceAll("verifyImg_value", verifyImage.getVerifyImgStr());
            String str3 = str2.replaceAll("realRequestUri_value", requestUrl);
            response.getWriter().write(str3);
            response.getWriter().close();
            return false;
        }
        return true;
    }

    /**
     * 是否拦截
     * @param requestUrl 请求uri
     * @param isAntiSpiderAnnotation 是否有AntiSpider注解
     * @return 是否拦截
     */
    public boolean isIntercept(String requestUrl, Boolean isAntiSpiderAnnotation) {
        if (this.globalFilterMode || isAntiSpiderAnnotation || this.includeUrls.contains(requestUrl)) {
            return true;
        } else {
            for (String includeUrl : includeUrls) {
                if (Pattern.matches(includeUrl, requestUrl)) {
                    return true;
                }
            }
            return false;
        }
    }
}
