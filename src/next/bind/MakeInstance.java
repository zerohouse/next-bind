package next.bind;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class MakeInstance {

	@SuppressWarnings("unchecked")
	public static <T> T make(Class<T> type) {
		List<Object> params = new ArrayList<Object>();
		Constructor<?>[] constructors = type.getConstructors();
		if (constructors.length == 0)
			return null;
		for (int i = 0; i < constructors.length; i++)
			if (constructors[i].getParameterTypes().length == 0) {
				try {
					return (T) constructors[i].newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		Class<?>[] paramTypes = constructors[0].getParameterTypes();
		for (int i = 0; i < paramTypes.length; i++) {
			params.add(getDefaultValue(paramTypes[i]));
		}
		try {
			return (T) type.getConstructors()[0].newInstance(params.toArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Object getDefaultValue(Class<?> paramType) {
		if (paramType.equals(byte.class)) {
			return 0;
		}
		if (paramType.equals(short.class)) {
			return 0;
		}
		if (paramType.equals(int.class)) {
			return 0;
		}
		if (paramType.equals(long.class)) {
			return 0L;
		}
		if (paramType.equals(float.class)) {
			return 0.0f;
		}
		if (paramType.equals(double.class)) {
			return 0.0d;
		}
		if (paramType.equals(char.class)) {
			return '\u0000';
		}
		if (paramType.equals(boolean.class)) {
			return false;
		}
		if (paramType.equals(byte[].class)) {
			return new byte[] { 0 };
		}
		if (paramType.equals(short[].class)) {
			return new short[] { 0 };
		}
		if (paramType.equals(int[].class)) {
			return new int[] { 0 };
		}
		if (paramType.equals(long[].class)) {
			return new long[] { 0L };
		}
		if (paramType.equals(float[].class)) {
			return new float[] { 0.0f };
		}
		if (paramType.equals(double[].class)) {
			return new double[] { 0.0d };
		}
		if (paramType.equals(char[].class)) {
			return new char[] { '\u0000' };
		}
		if (paramType.equals(boolean[].class)) {
			return new boolean[] { false };
		}
		return null;
	}
}
