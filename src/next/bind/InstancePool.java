package next.bind;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

public class InstancePool {

	private Map<Class<?>, Object> instanceMap;

	private Set<Class<?>> methodLevel;
	private Set<Class<?>> classLevel;
	private Set<Class<?>> fieldLevel;

	private Map<Class<?>, Set<Object>> annotationMap;

	private String basePackage;
	private ProducedInstances pi;

	public InstancePool(String basePackage) {
		pi = new ProducedInstances(basePackage);
		methodLevel = new HashSet<Class<?>>();
		classLevel = new HashSet<Class<?>>();
		fieldLevel = new HashSet<Class<?>>();
		this.basePackage = basePackage;
		instanceMap = new ConcurrentHashMap<Class<?>, Object>();
		annotationMap = new ConcurrentHashMap<Class<?>, Set<Object>>();
	}

	public Object getInstance(Class<?> type) {
		return instanceMap.get(type);
	}

	public Set<Object> getAnnotatedInstance(Class<?> type) {
		return annotationMap.get(type);
	}

	public Object getInstance(Method method) {
		return instanceMap.get(method.getDeclaringClass());
	}

	public Object getInstance(Field field) {
		return instanceMap.get(field.getDeclaringClass());
	}

	public void addMethodAnnotations(Class<?>... classes) {
		for (int i = 0; i < classes.length; i++) {
			methodLevel.add(classes[i]);
		}
	}

	public void addClassAnnotations(Class<?>... classes) {
		for (int i = 0; i < classes.length; i++) {
			classLevel.add(classes[i]);
		}
	}

	public void addFieldAnnotations(Class<?>... classes) {
		for (int i = 0; i < classes.length; i++) {
			fieldLevel.add(classes[i]);
		}
	}

	@SuppressWarnings("unchecked")
	public void build() {
		Reflections ref = new Reflections(basePackage, new SubTypesScanner(), new TypeAnnotationsScanner(), new FieldAnnotationsScanner(),
				new MethodAnnotationsScanner());
		classLevel.forEach(annotation -> {
			annotationMap.put(annotation, new HashSet<Object>());
			ref.getTypesAnnotatedWith((Class<? extends Annotation>) annotation).forEach(type -> {
				Object obj = MakeInstance.make(type);
				pi.bindFields(type, obj);
				instanceMap.put(type, obj);
				annotationMap.get(annotation).add(obj);
			});
		});
		methodLevel.forEach(annotation -> {
			annotationMap.put(annotation, new HashSet<Object>());
			ref.getMethodsAnnotatedWith((Class<? extends Annotation>) annotation).forEach(method -> {
				Class<?> type = method.getDeclaringClass();
				Object obj = MakeInstance.make(type);
				pi.bindFields(type, obj);
				instanceMap.put(type, obj);
				annotationMap.get(annotation).add(obj);
			});
		});
		fieldLevel.forEach(annotation -> {
			annotationMap.put(annotation, new HashSet<Object>());
			ref.getFieldsAnnotatedWith((Class<? extends Annotation>) annotation).forEach(method -> {
				Class<?> type = method.getDeclaringClass();
				Object obj = MakeInstance.make(type);
				pi.bindFields(type, obj);
				instanceMap.put(type, obj);
				annotationMap.get(annotation).add(obj);
			});
		});
	}

	
}
