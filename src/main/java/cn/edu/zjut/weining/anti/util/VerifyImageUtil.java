package cn.edu.zjut.weining.anti.util;


import cn.edu.zjut.weining.anti.captcha.utils.CaptchaUtil;
import cn.edu.zjut.weining.anti.module.VerifyImageDTO;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

import java.io.ByteArrayOutputStream;

import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author panda421
 * @since 2021/7/16
 */
public class VerifyImageUtil {

    public VerifyImageDTO generateVerifyImg() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String result = CaptchaUtil.out(outputStream);
        String base64Image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        String verifyId = UUID.randomUUID().toString();
        return new VerifyImageDTO(verifyId, null, base64Image, result);
    }

    public void saveVerifyCodeToRedis(VerifyImageDTO verifyImage) {
        stringRedisTemplate.boundValueOps(VERIFY_CODE_KEY + verifyImage.getVerifyId()).set(verifyImage.getResult(), 60, TimeUnit.SECONDS);
    }



    public void deleteVerifyCodeFromRedis(String verifyId) {
        stringRedisTemplate.delete(VERIFY_CODE_KEY + verifyId);
    }

    public String getVerifyCodeFromRedis(String verifyId) {

        return stringRedisTemplate.boundValueOps(VERIFY_CODE_KEY + verifyId).get();

    }
    private static final String VERIFY_CODE_KEY = "anti—spider_verifycode_";

    @Resource
    private StringRedisTemplate stringRedisTemplate;
}
