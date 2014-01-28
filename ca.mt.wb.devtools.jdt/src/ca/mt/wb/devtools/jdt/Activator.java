/**
 * Placed into public domain by Brian de Alwis.
 */
package ca.mt.wb.devtools.jdt;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Brian de Alwis
 */
public class Activator extends AbstractUIPlugin {
    
	//The shared instance.
    private static Activator instance;
    private BundleContext context;
	
	public void start(BundleContext context) throws Exception {
        instance = this;
        this.context = context;
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
        if (instance == this) {
            instance = null;
        }
        this.context = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static Activator getDefault() {
        return instance;
	}
    
    public static void log(IStatus status) {
        System.err.println(status);
    }
    
    public static Object invokeMethod(Object instance, String methodName,
            Class argTypes[], Object arguments[]) {
        Class clas = instance.getClass();
        try {
            Method method = findMethod(clas, methodName, argTypes);
            if(method == null) { return null; }
            method.setAccessible(true);
            return method.invoke(instance, arguments);
        } catch (Exception ex) {
            log(new Status(IStatus.ERROR, getBundleId(), 1,
                    "exception during reflective invocation of "
                            + clas.getName() + "." + methodName, ex));
            return null;
        }
    }
    
    public static String getBundleId() {
        return instance == null ? "" : instance.context.getBundle().getSymbolicName();
    }

    protected static Method findMethod(Class< ? > clas, String methodName, Class< ? > argTypes[]) {
    	do {
			try {
	    		Method method = clas.getDeclaredMethod(methodName, argTypes);
	    		if(method != null) { return method; }
			} catch (SecurityException e) {
				return null;
			} catch (NoSuchMethodException e) {
				/* do nothing */
			}
    	} while((clas = clas.getSuperclass()) != null);
    	return null;
    }

    public static void setField(Object instance, Class< ? > clas, String fieldName, int value) {
        try {
            Field field = clas.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setInt(instance, value);
        } catch (Exception ex) {
            log(new Status(IStatus.ERROR, getBundleId(), 1,
                    "exception while reflectively setting field '" + fieldName + "' in "
                        + clas.getName() + "." + fieldName,  ex));
        }
    }
}
