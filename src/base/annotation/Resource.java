package base.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * �Զ���ע�⣬���ڸ����Ը�ֵ
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Resource {
	public String name();
}
