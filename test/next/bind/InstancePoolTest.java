package next.bind;

import static org.junit.Assert.*;

import org.junit.Test;

public class InstancePoolTest {

	@Test
	public void test() {
		InstancePool pool = new InstancePool("");
		pool.addMethodAnnotations(TestAnnotation.class);
		pool.build();
		assertEquals(pool.getInstancesAnnotatedWith(TestAnnotation.class).size(), 2);
	}
}
