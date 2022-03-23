package cn.edu.zjut.weining.anti.config;

import cn.edu.zjut.weining.anti.interceptor.AntiSpiderInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


/**
 * @author panda421
 * @since 2021/7/4
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    private AntiSpiderInterceptor AntiSpiderInterceptor;

    public WebMvcConfig(AntiSpiderInterceptor AntiSpiderInterceptor) {
        this.AntiSpiderInterceptor = AntiSpiderInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.AntiSpiderInterceptor).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
