package org.nightlabs.installer.util;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * XXX my own implementation. Not part of IzPack.
 *
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class FileExecutor
{
	private static Executor executor = Executors.newCachedThreadPool(new DaemonThreadFactory());

    /**
     * Executed a system command and waits for completion.
     *
     * @param params system command as string array
     * @param output contains output of the command index 0 = standard output index 1 = standard
     *               error
     * @return exit status of process
     */
    public int executeCommand(String[] params, String[] output)
    {
    	int exitStatus = -1;
        Process process = null;

        try
        {
            // execute command
            process = Runtime.getRuntime().exec(params);
            ObservedProcess op = new ObservedProcess(process, executor);
            StringBufferLineConsumer stdout = new StringBufferLineConsumer();
            StringBufferLineConsumer stderr = new StringBufferLineConsumer();
            exitStatus = op.waitForProcess(stdout, stderr);
            stdout.close();
            stderr.close();
            output[0] = stdout.getOutputString();
            output[1] = stderr.getOutputString();
        }
        catch (InterruptedException e)
        {
            output[0] = "";
            output[1] = e.getMessage() + "\n";
        }
        catch (IOException e)
        {
            output[0] = "";
            output[1] = e.getMessage() + "\n";
        }
        finally
        {
            // cleans up always resources like file handles etc.
            // else many calls (like chmods for every file) can produce
            // too much open handles.
            if (process != null)
            {
                process.destroy();
            }
        }
        return exitStatus;
    }


    // taken from izPack:
    /**
     * Gets the output of the given (console based) commandline
     *
     * @param aCommandLine to execute
     * @return the result of the command
     */
    public static String getExecOutput(String[] aCommandLine)
    {
        return getExecOutput(aCommandLine, false);

    }

    /**
     * Executes the given Command and gets the result of StdOut, or if exec returns !=0:  StdErr.
     *
     * @param aCommandLine     aCommandLine to execute
     * @param forceToGetStdOut if true returns stdout
     * @return the result of the command stdout or stderr if exec returns !=0
     */
    public static String getExecOutput(String[] aCommandLine, boolean forceToGetStdOut)
    {
        FileExecutor fe = new FileExecutor();

        String[] execOut = new String[2];

        int execResult = fe.executeCommand(aCommandLine, execOut);

        if (execResult == 0)

        {
            return execOut[0];
        }

        else if (forceToGetStdOut)
        {
            return execOut[0];
        }
        else
        {
            return execOut[1];
        }
    }

}
