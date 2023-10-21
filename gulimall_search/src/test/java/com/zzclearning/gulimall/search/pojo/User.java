package com.zzclearning.gulimall.search.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author bling
 * @create 2023-09-27 10:59
 */
@Data
@AllArgsConstructor
public class User {
    String name;
    String sex;
    Integer age;
    Long employeeId;

}
