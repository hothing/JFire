package org.nightlabs.installer.base.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Method;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ConsoleFormatter
{
	private int cols = 75;
	private BufferedReader reader;
	private PrintWriter writer;

	public ConsoleFormatter()
	{
		Object console = getConsole();
		reader = getReader(console);
		writer = getWriter(console);
	}

	private static Object getConsole()
	{
		// do this using reflection as Console is only available with Java 6 and we should at least be able to print an error message 
		try {
			Method consoleMethod = System.class.getMethod("console", (Class<?>[])null);
			Object console = consoleMethod.invoke(null, (Object[])null);
			return console;
		} catch(Throwable e) {
		}		
		return null;
	}
	
	private static BufferedReader getReader(Object console)
	{
		// do this using reflection as Console is only available with Java 6 and we should at least be able to print an error message 
		if(console != null) {
			try {
				Method readerMethod = console.getClass().getMethod("reader", (Class<?>[])null);
				Reader consoleReader = (Reader) readerMethod.invoke(console, (Object[])null);
				return new BufferedReader(consoleReader);
			} catch(Throwable e) {
			}
		}
		return new BufferedReader(new InputStreamReader(System.in));
	}

	private static PrintWriter getWriter(Object console)
	{
		// do this using reflection as Console is only available with Java 6 and we should at least be able to print an error message 
		if(console != null) {
			try {
				Method writerMethod = console.getClass().getMethod("writer", (Class<?>[])null);
				PrintWriter consoleWriter = (PrintWriter) writerMethod.invoke(console, (Object[])null);
				return consoleWriter;
			} catch(Throwable e) {
			}
		}
		return new PrintWriter(System.out);
	}
	
	/**
	 * Finds the next text wrap position after <code>startPos</code> for the text
	 * in <code>sb</code> with the column width <code>width</code>.
	 * The wrap point is the last postion before startPos+width having a whitespace
	 * character (space, \n, \r).
	 *
	 * @param sb text to be analyzed
	 * @param width width of the wrapped text
	 * @param startPos position from which to start the lookup whitespace character
	 * @return postion on which the text must be wrapped or -1 if the wrap position is at the end
	 *         of the text
	 */
	protected static int findWrapPos( String text, int width, int startPos )
	{
		int pos = -1;
		// the line ends before the max wrap pos or a new line char found
		if ( ((pos = text.indexOf('\n', startPos)) != -1 && pos - startPos <= width)  ||
				((pos = text.indexOf('\t', startPos)) != -1 && pos - startPos <= width) )
		{
			return pos;
		}
		else if ( (startPos + width) >= text.length() )
		{
			return -1;
		}

		//look for the last whitespace character before startPos+width
		pos = startPos + width;
		char c;
		while ( pos >= startPos && (c = text.charAt(pos)) != ' ' && c != '\n' && c != '\r' )
		{
			--pos;
		}
		//if we found it - just return
		if ( pos > startPos )
		{
			return pos;
		}
		else
		{
			//must look for the first whitespace chearacter after startPos + width
			pos = startPos + width;
			while ( pos < text.length() && (c = text.charAt(pos)) != ' ' && c != '\n' && c != '\r' )
			{
				++pos;
			}
			return pos == text.length() ? -1 : pos;
		}
	}

	protected void doPrintlnCentered(String s)
	{
		for(int i=0; i<((cols-s.length())/2); i++)
			writer.print(" ");
		writer.println(s);
	}

	public void printlnCentered(String s)
	{
		int start = 0;
		while(true) {
			int stop = findWrapPos(s, cols, start);
			if(stop < 0) {
				doPrintlnCentered(s.substring(start));
				break;
			} else
				doPrintlnCentered(s.substring(start, stop));
			start = stop+1;
		}
	}

	public void print(String s)
	{
		writer.print(s);
		writer.flush();
	}
	
	public void println(String s)
	{
		int start = 0;
		while(true) {
			int stop = findWrapPos(s, cols, start);
			if(stop < 0) {
				writer.println(s.substring(start));
				break;
			} else
				writer.println(s.substring(start, stop));
			start = stop+1;
		}
	}

	public String getWrapped(String s)
	{
		StringBuffer sb = new StringBuffer();
		int start = 0;
		while(true) {
			int stop = findWrapPos(s, cols, start);
			if(stop < 0) {
				sb.append(s.substring(start));
				break;
			} else {
				sb.append(s.substring(start, stop));
				sb.append("\n");
			}
			start = stop+1;
		}
		return sb.toString();
	}

	public void println()
	{
		writer.println();
	}

	public String read()
	{
		return read(null);
	}

	public String read(String defaultValue)
	{
		try {
			String line = reader.readLine();
//			String line = reader.readLine();
			if("".equals(line))
				return defaultValue;
			return line;
		} catch (IOException e) {
			throw new RuntimeException("Reading result failed", e);
		}
	}

	public String readPassword()
	{
		return readPassword(null);
	}
	
	public String readPassword(String defaultValue)
	{
		Object console = getConsole();
		if(console != null) {
			try {
				Method readPasswordMethod = console.getClass().getMethod("readPassword", (Class<?>[])null);
				char[] pwd = (char[]) readPasswordMethod.invoke(console, (Object[])null);
				if(pwd == null || pwd.length == 0)
					return defaultValue;
				else
					return String.valueOf(pwd);
			} catch(Throwable e) {
				// ignore
			}		
		}
		
		// fall-back:
		return read(defaultValue);
	}
}
