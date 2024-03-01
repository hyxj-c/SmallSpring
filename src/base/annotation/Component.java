package base.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 自定义注解，实例化对象
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
	public String name();
}
