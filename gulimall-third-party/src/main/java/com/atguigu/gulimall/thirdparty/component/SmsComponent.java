package com.atguigu.gulimall.thirdparty.component;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * @author dgc
 * @date 2024/8/26 10:16
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.cloud.sms")
public class SmsComponent {
    private String host;
    private String path;
    private String appcode;
    private String templateId;
    private String smsSignId;
    public Boolean sendCode(String mobile, String code) {
        String urlSend = host + path;  // 仅保留基本URL
        try {
            URL url = new URL(urlSend);
            HttpURLConnection httpURLCon = (HttpURLConnection) url.openConnection();

            // 设置请求方法为POST
            httpURLCon.setRequestMethod("POST");

            // 设置请求头
            httpURLCon.setRequestProperty("Authorization", "APPCODE " + appcode);
            httpURLCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // 允许向连接中写入数据
            httpURLCon.setDoOutput(true);

            // 拼接请求参数
            String urlParameters = "param=" + "**code**:"+code+",**minute**:5" + "&mobile=" + mobile + "&templateId=" + templateId + "&smsSignId=" + smsSignId;

            // 将参数写入请求体
            try (DataOutputStream wr = new DataOutputStream(httpURLCon.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }

            // 获取响应码
            int httpCode = httpURLCon.getResponseCode();
            if (httpCode == 200) {
                String json = read(httpURLCon.getInputStream());
                System.out.print(json);
            } else {
                Map<String, List<String>> map = httpURLCon.getHeaderFields();
                String error = map.get("X-Ca-Error-Message").get(0);
                if (httpCode == 400 && error.equals("Invalid AppCode `not exists`")) {
                    System.out.println("AppCode错误 ");
                } else if (httpCode == 400 && error.equals("Invalid Url")) {
                    System.out.println("请求的 Method、Path 或者环境错误");
                } else if (httpCode == 400 && error.equals("Invalid Param Location")) {
                    System.out.println("参数错误");
                } else if (httpCode == 403 && error.equals("Unauthorized")) {
                    System.out.println("服务未被授权（或URL和Path不正确）");
                } else if (httpCode == 403 && error.equals("Quota Exhausted")) {
                    System.out.println("套餐包次数用完 ");
                } else {
                    System.out.println("参数名错误 或 其他错误");
                    System.out.println(error);
                }
            }
        } catch (MalformedURLException e) {
            System.out.println("URL格式错误");
        } catch (UnknownHostException e) {
            System.out.println("URL地址错误");
        } catch (Exception e) {
            // 打开注释查看详细报错异常信息
            // e.printStackTrace();
        }
        return true;
    }

    private static String read(InputStream is) throws IOException {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = br.readLine()) != null) {
            line = new String(line.getBytes(), "utf-8");
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }
}
