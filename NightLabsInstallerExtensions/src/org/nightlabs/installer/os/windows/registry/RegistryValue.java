package org.nightlabs.installer.os.windows.registry;

import java.util.List;

public class RegistryValue
{
	public static enum Type
	{
		REG_SZ,
		REG_BINARY,
		REG_DWORD,
		REG_MULTI_SZ,
		REG_EXPAND_SZ
	}

	private Type type;
	private byte[] value;

	public RegistryValue(Type type, byte[] value) {
		super();
		this.type = type;
		this.value = value;
	}

	public Type getType() {
		return type;
	}

	public byte[] getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		//return "type="+String.valueOf(type)+",value="+Arrays.toString(value);
		return "type="+String.valueOf(type)+",value="+new String(value);
	}

	public String getSz()
	{
		return new String(value);
	}

	public byte[] getBinary()
	{
		throw new UnsupportedOperationException();
//		byte[] binary = new byte[value.length / 2];
//		for(int i=0; i<binary.length; i++) {
//		String sub = new String(value, i*2, 2);
//		binary[i] = (byte)Integer.parseInt(sub, 16);
//		}
//		return binary;
	}

	public long getDword()
	{
		throw new UnsupportedOperationException();
	}

	public List<String> getMultiSz()
	{
		throw new UnsupportedOperationException();
	}

	public String getExpandSz()
	{
		throw new UnsupportedOperationException();
	}
}