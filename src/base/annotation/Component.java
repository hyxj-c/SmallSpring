package base.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * �Զ���ע�⣬ʵ��������
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
	public String name();
}
