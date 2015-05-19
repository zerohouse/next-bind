package next.bind;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import next.bind.annotation.Bind;
import next.bind.annotation.Produces;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

public class ProducedInstances {

	public ProducedInstances(String basePackage) {
		Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();
		Reflections ref = new Reflections(basePackage, new MethodAnnotationsScanner());
		ref.getMethodsAnnotatedWith(Produces.class).forEach(method -> {
			Class<?> declaring = method.getDeclaringClass();
			Object obj = instances.get(declaring);
			if (obj == null) {
				obj = MakeInstance.make(declaring);
				instances.put(declaring, obj);
			}
			try {
				Object produced = method.invoke(obj);
				String id = method.getAnnotation(Produces.class).value();
				put(method.getReturnType(), id, produced);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void put(Class<?> type, String id, Object produced) {
		if ("".equals(id)) {
			typeMap.put(type, produced);
			return;
		}
		idMap.put(id, produced);
	}

	private Map<Class<?>, Object> typeMap;
	private Map<String, Object> idMap;

	public Object get(Field field) {
		String id = field.getAnnotation(Bind.class).value();
		Object returned;
		returned = get(field.getType(), id);
		if (returned == null) {
			returned = MakeInstance.make(field.getType());
			bindFields(returned.getClass(), returned);
			put(returned.getClass(), id, returned);
		}

		return null;
	}

	private Object get(Class<?> type, String id) {
		Object returned;
		if (id.equals(""))
			returned = typeMap.get(type);
		else
			returned = idMap.get(id);
		return returned;
	}

	public void bindFields(Class<?> type, Object obj) {
		Field[] fields = type.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (!fields[i].isAnnotationPresent(Bind.class))
				continue;
			setFields(obj, fields[i]);
		}
		Class<?> supperClass = type.getSuperclass();
		if (supperClass != null)
			bindFields(supperClass, obj);
	}

	private void setFields(Object obj, Field field) {
		field.setAccessible(true);
		try {
			field.set(obj, get(field));
		} catch (Exception e) {
		}
	}

}
