package com.atguigu.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author dgc
 * @date 2024/8/26 12:04
 */
@Data
public class UserRegistVo {
    @NotBlank(message = "用户名不能为空")
    @Length(min = 6,max = 18,message = "用户名必须是6-18位字符")
    private String userName;
    @NotBlank(message = "密码不能为空")
    @Length(min = 6,max = 18,message = "密码必须是6-18位字符")
    private String password;
    @Pattern(regexp ="^[1]([3-9])[0-9]{9}$",message = "手机号格式不正确")
    private String phone;
    @NotBlank(message = "验证码不能为空")
    private String code;
}
