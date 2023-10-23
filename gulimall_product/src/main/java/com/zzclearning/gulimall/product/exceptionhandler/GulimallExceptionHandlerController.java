package com.zzclearning.gulimall.product.exceptionhandler;

import com.zzclearning.common.exception.BizExceptionEnum;
import com.zzclearning.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

/**
 * @author bling
 * @create 2022-10-27 10:02
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.zzclearning.gulimall.product")
public class GulimallExceptionHandlerController {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R validateException(MethodArgumentNotValidException ex) {
        log.error("数据校验出现问题{}, 异常类型:{}", ex.getMessage(), ex.getClass());
        HashMap<String, String> errorMap = new HashMap<>();
        BindingResult bindingResult = ex.getBindingResult();
        bindingResult.getFieldErrors().forEach(item->{
            String field = item.getField();
            String defaultMessage = item.getDefaultMessage();
            errorMap.put(field, defaultMessage);
        });
        return R.error(BizExceptionEnum.VALIDATION_EXCEPTION.getCode(), BizExceptionEnum.VALIDATION_EXCEPTION.getMessage()).put("data",errorMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R globalException(Throwable ex) {
        log.error("全局异常,异常信息：{},异常类：{}" ,ex.getMessage(),ex.getClass());
        return R.error(BizExceptionEnum.UNKNOWN_EXCEPTION.getCode(), BizExceptionEnum.UNKNOWN_EXCEPTION.getMessage());
    }
}
