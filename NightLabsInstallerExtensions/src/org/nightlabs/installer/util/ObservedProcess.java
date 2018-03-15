/* *****************************************************************************
 * NightLabsBase - Utilities by NightLabs                                      *
 * Copyright (C) 2004-2007 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.installer.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;

import org.nightlabs.installer.Logger;

/**
 * Helper to wait for {@link Process}es started with {@link Runtime} and
 * to monitor their outputs. Create an {@link ObservedProcess} with a
 * {@link Process} obtained from Runtime similar to:
 * <pre>
 * ObservedProcess proc = new ObservedProcess(Runtime.getRuntime().exec(myCommand));
 * </pre>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ObservedProcess 
{
	private Executor executor;
	private Process proc;

	/**
	 * @author Marc Klinger - marc[at]nightlabs[dot]de
	 * This is only used since there are to implementations right now.
	 * It can be removed when the Writer implementation is removed.
	 */
	private static interface IStreamObserver extends Runnable
	{
		public boolean isDone();
	}
	
	/**
	 * Inner class used to observe a stream and redirect the output.
	 */
	private static class StreamObserver implements IStreamObserver, Runnable 
	{
		private InputStream is;
		private OutputStream output;
		private boolean done;

		/**
		 * Create a new StreamObserver. The invoker is responsible
		 * for closing the given stream/writer.
		 * @param is The stream to observer
		 * @param output The output stream to redirect the stream output to
		 */
		public StreamObserver(InputStream is, OutputStream output) 
		{
			this.is = is;
			this.output = output;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() 
		{
			done = false;
			try {
				try {
					Util.transferStreamData(is, output);
				} catch (IOException e) {
					Logger.err.println("Error reading process output in StreamObserver");
					e.printStackTrace(Logger.err);
				}
			} finally {
				done = true;
			}
		}
		
		public boolean isDone()
		{
			return done;
		}
	}	
	/**
	 * Create a new ObservedProcess. If you use many ObservedProcesses, consider
	 * using the {@link #ObservedProcess(Process, Executor)} constructor with a
	 * shared thread pool to improve performance.
	 * @param proc The process to observe
	 */
	public ObservedProcess(Process proc) 
	{
		this(proc, new Executor() {
			/* (non-Javadoc)
			 * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
			 */
			public void execute(Runnable command) {
				Thread t = new Thread(command);
				t.setPriority(Thread.NORM_PRIORITY);
				t.start();
			}
		});
	}

	/**
	 * Create a new ObservedProcess.
	 * @param proc The process to observe
	 * @param executor An executor to execute the observer threads.
	 * 		This may be a {@link java.util.concurrent.ThreadPoolExecutor} or similar.
	 */
	public ObservedProcess(Process proc, Executor executor) 
	{
		this.proc = proc;
		this.executor = executor;
	}
	
	/**
	 * Waits for the process this observer was created with.
	 * Additionally callers can pass {@link OutputStream} objects where
	 * the output end error-output of the running process will
	 * be written to.
	 * Return value is the exitValue of the running process.
	 * 
	 * @param output OutputStream for the process output.
	 * @param error OutputStream for the process error-output.
	 * @return The exitValue of the running process
	 * @throws InterruptedException When interrupted waiting for the actual process or the threads observing its output.
	 */
	public int waitForProcess(OutputStream output, OutputStream error)
	throws InterruptedException
	{
		return waitForProcess(
				new StreamObserver(proc.getInputStream(), output), 
				new StreamObserver(proc.getErrorStream(), error));
	}
	
	private int waitForProcess(IStreamObserver outputObserver, IStreamObserver errorObserver) throws InterruptedException
	{
		executor.execute(outputObserver);
		executor.execute(errorObserver);
		
		// wait for the process and check its exit status
		int exitVal = proc.waitFor();
		
		// now wait for the observer threads to finish
		while(!outputObserver.isDone() || !errorObserver.isDone())
			Thread.yield();
		
		// return the exit status
		return exitVal;		
	}
}
