package cn.edu.zjut.weining.anti.config;

import cn.edu.zjut.weining.anti.ValidateFormService;
import cn.edu.zjut.weining.anti.constant.AntiSpiderConsts;
import cn.edu.zjut.weining.anti.interceptor.AntiSpiderInterceptor;
import cn.edu.zjut.weining.anti.rule.AntiSpiderRule;
import cn.edu.zjut.weining.anti.rule.IpRule;
import cn.edu.zjut.weining.anti.rule.RuleActuator;
import cn.edu.zjut.weining.anti.rule.UaRule;
import cn.edu.zjut.weining.anti.servlet.RefreshFormServlet;
import cn.edu.zjut.weining.anti.servlet.ValidateFormServlet;
import cn.edu.zjut.weining.anti.util.VerifyImageUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RedissonAutoConfiguration 的 AutoConfigureOrder 为默认值(0)，此处在它后面加载
 * @author panda421
 * @since 2021/7/8
 */
@Configuration
@EnableConfigurationProperties(AntiSpiderProperties.class)
@ConditionalOnProperty(prefix = "anti.spider.manager", value = "enabled", havingValue = "true")
@Import({RedisAutoConfiguration.class, WebMvcConfig.class})
public class AntiSpiderAutoConfig {

    @Bean
    public ServletRegistrationBean validateFormServlet() {
        return new ServletRegistrationBean(new ValidateFormServlet(), AntiSpiderConsts.VALIDATE_REQUEST_URI);
    }

    @Bean
    public ServletRegistrationBean refreshFormServlet() {
        return new ServletRegistrationBean(new RefreshFormServlet(), AntiSpiderConsts.REFRESH_REQUEST_URI);
    }

    @Bean
    @ConditionalOnProperty(prefix = "anti.spider.manager.ip-rule",value = "enabled", havingValue = "true", matchIfMissing = true)
    public IpRule ipRule(){
        return new IpRule();
    }

    @Bean
    @ConditionalOnProperty(prefix = "anti.spider.manager.ua-rule",value = "enabled", havingValue = "true", matchIfMissing = true)
    public UaRule uaRule() {
        return new UaRule();
    }

    @Bean
    public VerifyImageUtil verifyImageUtil() {
        return new VerifyImageUtil();
    }

    @Bean
    public RuleActuator ruleActuator(final List<AntiSpiderRule> rules){
        final List<AntiSpiderRule> AntiSpiderRules = rules.stream()
                .sorted(Comparator.comparingInt(AntiSpiderRule::getOrder)).collect(Collectors.toList());
        return new RuleActuator(AntiSpiderRules);
    }

    @Bean
    public ValidateFormService validateFormService(){
        return new ValidateFormService();
    }

    @Bean
    public AntiSpiderInterceptor AntiSpiderInterceptor() {
        return new AntiSpiderInterceptor();
    }

}
