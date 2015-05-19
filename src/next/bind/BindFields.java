package next.bind;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import next.bind.annotation.Bind;
import next.bind.annotation.Produces;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindFields {

	private static final Logger logger = LoggerFactory.getLogger(BindFields.class);

	public BindFields(Reflections ref) {
		idMap = new HashMap<String, Object>();
		typeMap = new HashMap<Class<?>, Object>();
		makeProduces(ref);
	}

	private void makeProduces(Reflections ref) {
		Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();
		ref.getMethodsAnnotatedWith(Produces.class).forEach(method -> {
			Class<?> declaring = method.getDeclaringClass();
			method.setAccessible(true);
			Object obj = instances.get(declaring);
			logger.info("\n");
			logger.info(String.format("Produces %s", method.getName()));
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
			logger.info(String.format("Class %s -> %s", type.getSimpleName(), produced));
			typeMap.put(type, produced);
			return;
		}
		logger.info(String.format("ID %s -> %s", id, produced));
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
		return returned;
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
		Object bind = get(field);
		try {
			if (field.get(obj) != null)
				return;
			logger.info(String.format("%s.%s -> %s", field.getDeclaringClass().getSimpleName(), field.getName(), bind));
			field.set(obj, bind);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
