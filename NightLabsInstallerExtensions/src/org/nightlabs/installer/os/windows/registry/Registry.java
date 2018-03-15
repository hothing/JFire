package org.nightlabs.installer.os.windows.registry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.nightlabs.installer.util.DaemonThreadFactory;
import org.nightlabs.installer.util.ObservedProcess;

public class Registry
{
	private Executor executor;

	private Map<String, RegistryValue> registrationValues = new HashMap<String, RegistryValue>();

	private enum LineType { NEWLINE, COMMENT, PATH, VALUE }

	public Registry()
	{
		executor = Executors.newCachedThreadPool(new DaemonThreadFactory());
	}

	private void parseResult(byte[] _result)
	{
		StringBuilder path = new StringBuilder();
		Registry.LineType lineType = LineType.NEWLINE;
		for(int i=0; i<_result.length; i++) {
			if(_result[i] == '\r') {
				lineType = LineType.NEWLINE;
				i++;
				if(_result[i] != '\n')
					throw new IllegalStateException("Illegal new line");
			} else if(lineType == LineType.NEWLINE) {
				if(_result[i] == '!') {
					lineType = LineType.COMMENT;
				} else if(_result[i] == ' ') {
					lineType = LineType.VALUE;
				} else {
					lineType = LineType.PATH;
					if(path.length() > 0)
						path.delete(0, path.length());
					path.append((char)_result[i]);
				}
			} else if(lineType == LineType.PATH) {
				path.append((char)_result[i]);
			} else if(lineType == LineType.VALUE) {
				// skip 3 remaining spaces
				i+=3;
				int delim;
				for(delim=i; delim<_result.length; delim++)
					if(_result[delim] == '\t')
						break;
				String keyName = new String(_result, i, delim-i);
				i = delim+1;
				for(delim=i; delim<_result.length; delim++)
					if(_result[delim] == '\t')
						break;
				String typeName = new String(_result, i, delim-i);
				i = delim+1;
				for(delim=i; delim<_result.length; delim++)
					if(_result[delim] == '\r')
						break;
				byte[] value = new byte[delim-i];
				System.arraycopy(_result, i, value, 0, delim-i);
				i = delim - 1;

				RegistryValue v = new RegistryValue(RegistryValue.Type.valueOf(typeName), value);
				registrationValues.put(path.toString()+keyName, v);
			}
		}

//		String result = new String(_result);
//		String[] lines = result.split("\r\n");
//		String currentPath = null;
//		for (String line : lines) {
//		if(line.isEmpty() || line.startsWith("!")) {
//		continue;
//		} else if(line.startsWith("    ")) {
//		if(currentPath == null)
//		throw new IllegalStateException("Invalid result format");
//		String[] parts = line.substring("    ".length()).split("\t", 3);
//		String key = parts[0];
//		String type = parts[1];
//		String value = parts[2];
//		Value v = new Value();
//		v.type = Value.Type.valueOf(type);
//		if(v.type == null)
//		throw new IllegalStateException("Unsupported value type: "+type);
//		v.value = value;
//		registrationValues.put(currentPath+key, v);
//		}
//		else {
//		currentPath = line;
//		}
//		}
	}

	public RegistryValue getValue(RegistryKey key) throws IOException
	{
		String fullQualifiedKey = key.getFull();
		synchronized(registrationValues) {
			if(!registrationValues.containsKey(fullQualifiedKey)) {
				//Process proc = Runtime.getRuntime().exec(new String[] {"REG", "QUERY", root+subPath, "/v", key});
				Process proc = Runtime.getRuntime().exec(new String[] {"REG", "QUERY", key.getRegFormPath()});
				ObservedProcess p = new ObservedProcess(proc, executor);
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				ByteArrayOutputStream berr = new ByteArrayOutputStream();
				try {
					p.waitForProcess(bout, berr);
				} catch (InterruptedException e) {
					throw new IOException("Interrupted", e);
				}
				//System.out.println(bout.toString());
				parseResult(bout.toByteArray());
			}
			return registrationValues.get(fullQualifiedKey);
		}
	}
}