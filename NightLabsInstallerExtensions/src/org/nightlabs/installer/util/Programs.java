package org.nightlabs.installer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class Programs
{
	private static final String JAVA_HOME = "JAVA_HOME";

	private static String[] defaultPaths = {
		"/bin",
		"/usr/bin",
		"/usr/local/bin",
		"/sbin",
		"/usr/sbin",
		"/usr/local/sbin",
	};

	public static String findProgram(String program)
	{
		for (String path : defaultPaths) {
			File f = new File(path, program);
			if(f.exists())
				return f.getAbsolutePath();
		}
		return program;
	}

	public static String findToolsJar() throws ProgramException
	{
		String javaHome = getJavaHome();
		File tools = new File(new File(javaHome, "lib"), "tools.jar");
		if(!tools.exists())
			throw new ProgramException(String.format("The environment variable %s does not point to a valid java JDK installation. The library %s was not found", JAVA_HOME, tools.getAbsolutePath()));
		return tools.getAbsolutePath();
	}

//	/**
//	 * Find the java executable
//	 * @return The full path to the java executable
//	 * @throws ProgramException If JAVA_HOME is not set or the
//	 * 		java executable could not be found.
//	 */
	public static String findJava() //throws ProgramException
	{
		String java;
		String javaEnv = System.getenv("JAVA");
		if(javaEnv == null || javaEnv.trim().length() == 0) {
			String javaHomeEnv = System.getenv("JAVA_HOME");
			if(javaHomeEnv != null && javaHomeEnv.trim().length() != 0) {
				java = javaHomeEnv + File.separator + "bin" + File.separator + "java";
			} else {
				java = "java";
			}
		} else {
			java = javaEnv;
		}
		return java;


//		String javaHome = getJavaHome();
//		String executable  = EnvironmentHelper.isWindows() ? "java.exe" : "java";
//		File java = new File(new File(javaHome, "bin"), executable);
//		if(!java.exists())
//			throw new ProgramException(String.format("The environment variable %s does not point to a valid java installation. The executable %s was not found", JAVA_HOME, java.getAbsolutePath()));
//		return java.getAbsolutePath();
	}

	private static String getJavaHome() throws ProgramException
	{
		String javaHome = System.getenv(JAVA_HOME);
		if(javaHome == null)
			throw new ProgramException(String.format("The environment variable %s was not found. It is needed to execute other java applications", JAVA_HOME));
		return javaHome;
	}

	public static void execute(int expectedResult, String ... cmd) throws ProgramException
	{
		execute(null, expectedResult, cmd);
	}

	public static void execute(File workingDir, int expectedResult, String ... cmd) throws ProgramException
	{
		Process p = null;
		try {
			ProcessBuilder pb = new ProcessBuilder(cmd);
			if(workingDir != null)
				pb.directory(workingDir);
			pb.redirectErrorStream(true);
			p = pb.start();
			InputStream in = p.getInputStream();

//			// It seems to hang in Windows because no data comes out from the stream and reader.readLine() waits forever.
//			// We try to avoid this situation by waiting first manually until data pops up in the stream. Marco.
//			// This seems to be a bug when starting a '.bat' file.
//			// Other '.bat' files can be executed - only the shutdown.bat hangs. Since this file is not used anymore
//			// anyway, this code is not required. Marco.
//			long timeoutBeforeDataArrivesInInputStreamMSec = 30000;
//			long processStartTime = System.currentTimeMillis();
//			while (true) {
//				if (in.available() > 0)
//					break;
//
//				try {
//					p.exitValue();
//					return; // the app has been closed before data came in the inputstream
//				} catch (IllegalThreadStateException x) {
//					// fine, the app is not yet closed
//				}
//
//				if (System.currentTimeMillis() - processStartTime > timeoutBeforeDataArrivesInInputStreamMSec)
//					throw new IllegalStateException("No data arrived in input-stream within timeout=" + timeoutBeforeDataArrivesInInputStreamMSec + " msec!");
//
//				try {
//					Thread.sleep(1000);
//				} catch(InterruptedException ex) {}
//			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while(true) {
				String line = reader.readLine();
				if(line == null)
					break;
			}

			long timeoutAfterInputStreamClosed = 30000; // wait max 30 sec for a result - it should be there immediately, when the input-stream is closed.
			long inputStreamClosedTime = System.currentTimeMillis();
			Integer exitValue = null;
			while(exitValue == null) {
				try {
					exitValue = p.exitValue();
				} catch(IllegalThreadStateException e) {
					// give it some more time
					try {
						Thread.sleep(100);
					} catch(InterruptedException ex) {}
				}
				if (exitValue == null && (System.currentTimeMillis() - inputStreamClosedTime > timeoutAfterInputStreamClosed))
					throw new IllegalStateException("Process.exitValue() still returned no result, even though input-stream was closed more than " + timeoutAfterInputStreamClosed + " msec ago!");
			}
			if(exitValue != expectedResult)
				throw new ProgramException("Execution of "+cmd[0]+" failed. Return value: "+exitValue);
		} catch(ProgramException e) {
			throw e;
		} catch(Exception e) {
			throw new ProgramException("Execution of "+cmd[0]+" failed", e);
		} finally {
			if(p != null)
				p.destroy();
		}
	}

	public static void execute(String ... cmd) throws ProgramException
	{
		execute(0, cmd);
	}

	public static void execute(File workingDir, String ... cmd) throws ProgramException
	{
		execute(workingDir, 0, cmd);
	}

	public static void chmod(File f, String type, boolean exceptionOnError) throws ProgramException
	{
		if(!f.exists()) {
			if(exceptionOnError)
				throw new ProgramException("File not found: "+f.getAbsolutePath());
			System.err.println("File not found: "+f.getAbsolutePath());
			return;
		}
		String chmod = findProgram("chmod");
		try {
			execute(chmod, type, f.getAbsolutePath());
		} catch (ProgramException e) {
			if(exceptionOnError)
				throw e;
			e.printStackTrace();
			return;
		}
	}
}
