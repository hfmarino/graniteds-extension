package org.granite.messaging.amf.io.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.granite.messaging.amf.io.convert.Converters;

public class DummyProperty extends Property {

	protected DummyProperty(Converters converters, String name) {
		super(converters, name);
	}

	@Override
	public void setProperty(Object instance, Object value, boolean convert) {
	}

	@Override
	public Object getProperty(Object instance) {
		return null;
	}

	@Override
	public Type getType() {
		return null;
	}

	@Override
	public boolean isAnnotationPresent(
			Class<? extends Annotation> annotationClass) {
		return false;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return null;
	}

}
