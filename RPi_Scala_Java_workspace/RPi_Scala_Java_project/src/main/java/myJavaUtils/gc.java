package myJavaUtils;

import java.lang.ref.WeakReference;;

public class gc {
	/*
	 * This method guarantees that garbage collection is
	 * done unlike <code>{@link System#gc()}</code>
	 */
	public static void garbageCollect() {
		Object obj = new Object();
		WeakReference ref = new WeakReference<Object>(obj);
		obj = null;
		while(ref.get() != null) {
			System.gc();
		}
	}
}