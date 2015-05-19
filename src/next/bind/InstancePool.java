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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstancePool {

	private static final Logger logger = LoggerFactory.getLogger(InstancePool.class);

	private Map<Class<?>, Object> instanceMap;

	private Set<Class<?>> methodLevelAnnotation;
	private Set<Class<?>> classLevelAnnotation;
	private Set<Class<?>> fieldLevelAnnotation;

	private Set<Class<?>> classes;

	private Map<Class<?>, Set<Object>> annotationMap;

	private String basePackage;
	private BindFields buildFields;

	public InstancePool(String basePackage) {
		methodLevelAnnotation = new HashSet<Class<?>>();
		classLevelAnnotation = new HashSet<Class<?>>();
		fieldLevelAnnotation = new HashSet<Class<?>>();
		classes = new HashSet<Class<?>>();
		this.basePackage = basePackage;
		instanceMap = new ConcurrentHashMap<Class<?>, Object>();
		annotationMap = new ConcurrentHashMap<Class<?>, Set<Object>>();
	}

	public Object getInstance(Class<?> type) {
		return buildFields.get(type) != null ? buildFields.get(type) : instanceMap.get(type);
	}

	public Set<Object> geInstancesAnnotatedWith(Class<?> type) {
		return annotationMap.get(type);
	}

	public Object getInstance(Method method) {
		return instanceMap.get(method.getDeclaringClass());
	}

	public Object getInstance(Field field) {
		return instanceMap.get(field.getDeclaringClass());
	}

	public Object getInstance(String id) {
		return buildFields.get(id);
	}

	public void addClasses(Class<?>... classes) {
		for (int i = 0; i < classes.length; i++) {
			this.classes.add(classes[i]);
		}
	}

	public void addMethodAnnotations(Class<?>... classes) {
		for (int i = 0; i < classes.length; i++) {
			methodLevelAnnotation.add(classes[i]);
		}
	}

	public void addClassAnnotations(Class<?>... classes) {
		for (int i = 0; i < classes.length; i++) {
			classLevelAnnotation.add(classes[i]);
		}
	}

	public void addFieldAnnotations(Class<?>... classes) {
		for (int i = 0; i < classes.length; i++) {
			fieldLevelAnnotation.add(classes[i]);
		}
	}

	@SuppressWarnings("unchecked")
	public void build() {
		Reflections ref = new Reflections(basePackage, new SubTypesScanner(), new TypeAnnotationsScanner(), new FieldAnnotationsScanner(),
				new MethodAnnotationsScanner());
		buildFields = new BindFields(ref);
		classLevelAnnotation.forEach(annotation -> {
			annotationMap.put(annotation, new HashSet<Object>());
			ref.getTypesAnnotatedWith((Class<? extends Annotation>) annotation).forEach(type -> {
				make(annotation, type);
			});
		});
		methodLevelAnnotation.forEach(annotation -> {
			annotationMap.put(annotation, new HashSet<Object>());
			ref.getMethodsAnnotatedWith((Class<? extends Annotation>) annotation).forEach(method -> {
				Class<?> type = method.getDeclaringClass();
				make(annotation, type);
			});
		});
		fieldLevelAnnotation.forEach(annotation -> {
			annotationMap.put(annotation, new HashSet<Object>());
			ref.getFieldsAnnotatedWith((Class<? extends Annotation>) annotation).forEach(method -> {
				Class<?> type = method.getDeclaringClass();
				make(annotation, type);
			});
		});
		classes.forEach(type -> {
			make(type);
		});
	}

	private void make(Class<?> annotation, Class<?> type) {
		Object obj = make(type);
		if (obj != null)
			annotationMap.get(annotation).add(obj);
	}

	private Object make(Class<?> type) {
		if (instanceMap.get(type) != null)
			return null;
		logger.info("\n");
		logger.info(String.format("%s 인스턴스를 생성합니다. ", type.getSimpleName()));
		Object obj = MakeInstance.make(type);
		buildFields.bindFields(type, obj);
		instanceMap.put(type, obj);
		return obj;
	}

}
