package base.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 自定义注解，用于给属性赋值
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Resource {
	public String name();
}
