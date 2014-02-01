import org.freecode.irc.votebot.api.ExternalModule;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import java.io.File;
import java.util.Properties;

/**
 * Created by shivam on 12/9/13.
 */
public class JythonTest {

    public static void main(String[] args) {
        String directory = System.getProperty("user.home") + File.separator + ".fvb-modules";
        File dir = new File(directory);
        if (dir.exists()) {
            try {
                Properties props = new Properties();
                props.setProperty("python.path", directory);
                PythonInterpreter.initialize(System.getProperties(), props, new String[]{""});
                PythonInterpreter interpreter = new PythonInterpreter();
               // PyCode code = interpreter.compile(new FileReader(new File(directory, "TestModule.py")));
                interpreter.exec("from TestModule import TestModule");
                //interpreter.eval(code);
               // System.out.println(interpreter.getLocals());
                PyObject object = interpreter.get("TestModule");

                PyObject buildObject = object.__call__();
                ExternalModule ext = (ExternalModule) buildObject.__tojava__(ExternalModule.class);
                System.out.println(ext.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
