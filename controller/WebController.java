package com.example.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.Result;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.User;
import com.example.service.UserService;
import com.example.utils.MailCodeCache;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 基础前端接口
 */
@RestController
public class WebController {

    @Resource
    private UserService userService;

    @GetMapping("/")
    public Result hello() {
        return Result.success("访问成功");
    }

    @PostMapping("/islogin")
    public Result islogin(){
        return Result.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        if (ObjectUtil.isEmpty(user.getUsername()) || ObjectUtil.isEmpty(user.getPassword())) {
            return Result.error(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        user = userService.login(user);
//        System.out.println(user.getUsername()+" "+user.getPassword()+" "+user.getEmail());
//        System.out.println(user.getToken());
        return Result.success(user);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        if (ObjectUtil.isEmpty(user.getUsername()) || ObjectUtil.isEmpty(user.getPassword())
                || ObjectUtil.isEmpty(user.getEmail())) {
            return Result.error(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        if(!MailCodeCache.validateCode(user.getEmail(), user.getCode())){
            return Result.error(ResultCodeEnum.VALIDATE_CODE_ERROR);
        }
        System.out.println(user.getUsername()+" "+user.getPassword()+" "+user.getEmail());
        userService.register(user);
        return Result.success();
    }
    /**
     * 请求邮箱验证码
     */
    @GetMapping("/sendEmailCode")
    public Result sendEmailCode(@RequestParam String email) {
        if (StrUtil.isEmpty(email)) {
            return Result.error(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        //生成六位随机数
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        //发送邮件
        String scriptPath = "C:/Users/april/Downloads/jgw/scripts/mailSender.py";
        //String scriptPath = "/home/ubuntu/test/mailSender.py";    // 服务器上的路径
//        String[] args1 = new String[]{"python3", scriptPath, code, email};
        String[] args1 = new String[]{"python", scriptPath, code, email};


        try{
            // 使用Runtime执行Python脚本发送邮件。
            Process proc = Runtime.getRuntime().exec(args1);
            // 等待进程结束，并获取退出值。
            int f = proc.waitFor();
            // 如果进程正常结束（退出值为0），则将验证码存储到缓存中。
            if(f == 0 ){
                MailCodeCache.setCache(email, code);

            }else{
                // 如果进程异常结束，则读取脚本的输出信息。
                BufferedReader in = new BufferedReader(new InputStreamReader( proc.getInputStream() ));
                // 打印脚本输出的信息。
                String actionStr = in.readLine();
                if (actionStr != null)
                    System.out.println(actionStr);
                // 返回发送邮件错误的结果。
                return Result.error(ResultCodeEnum.SEND_MAIL_ERROR);
            }

        }catch (Exception e){
            // 打印异常堆栈信息。
            e.printStackTrace();
        }
        // 如果一切顺利，则返回成功结果。
        return Result.success();
    }


}
