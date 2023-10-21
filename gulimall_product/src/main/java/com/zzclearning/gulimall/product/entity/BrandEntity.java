package com.zzclearning.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.zzclearning.common.validator.group.AddGroup;
import com.zzclearning.common.validator.group.UpdateGroup;
import com.zzclearning.common.validator.group.UpdateStatusGroup;
import com.zzclearning.common.validator.validate.ListValue;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author zzc
 * @email zzc@gmail.com
 * @date 2022-10-24 16:44:33
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@NotNull(message = "修改必须携带指定品牌Id",groups = {UpdateGroup.class, UpdateStatusGroup.class})
	@Null(message = "新增不能指定品牌Id",groups = {AddGroup.class})
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名不能为空",groups = {AddGroup.class,UpdateGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotBlank(message = "品牌logo不能为空",groups = {AddGroup.class})
	@URL(message = "品牌地址必须为url",groups = {AddGroup.class,UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 自定义参数校验注解和校验器
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(message = "显示状态不能为空",groups = {AddGroup.class,UpdateGroup.class,UpdateStatusGroup.class})
	@ListValue(value = {0,1},groups = {AddGroup.class,UpdateGroup.class,UpdateStatusGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotBlank(message = "检索首字母不能为空",groups = {AddGroup.class,UpdateGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须为一个英文字母", groups = {AddGroup.class,UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(message = "排序字段不能为空",groups = {AddGroup.class,UpdateGroup.class})
	@Min(value = 0,message = "排序字段必须大于等于0", groups = {AddGroup.class,UpdateGroup.class})
	private Integer sort;

}
