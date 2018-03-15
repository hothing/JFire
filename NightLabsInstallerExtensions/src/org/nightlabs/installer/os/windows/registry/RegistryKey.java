package org.nightlabs.installer.os.windows.registry;

public class RegistryKey
{
	RegistryRoot root;
	String subPath;
	String key;

	public RegistryKey(RegistryRoot root, String subPath, String key) {
		super();
		this.root = root;
		this.subPath = subPath;
		this.key = key;
	}

	public RegistryRoot getRoot() {
		return root;
	}

	public void setRoot(RegistryRoot root) {
		this.root = root;
	}

	public String getSubPath() {
		return subPath;
	}

	public void setSubPath(String subPath) {
		this.subPath = subPath;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	private static String escapeKey(String key)
	{
		// TODO
		/* Keys:
		 * Darzustellendes Zeichen 	Escapecodierung
; 	%3b
/ 	%2f
? 	%3f
: 	%3a
@ 	%40
& 	%26
= 	%3b
+ 	%2b
$ 	%24
, 	%2c
% 	%25
		 */			
		return key;
	}

	public String getRegFormPath()
	{
		if(root == null || subPath == null)
			throw new NullPointerException();
		if(!subPath.startsWith("\\"))
			subPath = "\\"+subPath;
		if(!subPath.endsWith("\\") && !key.startsWith("\\"))
			subPath = subPath+"\\";
		return root.getShortForm()+escapeKey(subPath);				
	}

	public String getRegFormFull()
	{
		String regFormPath = getRegFormPath();
		if(key == null)
			throw new NullPointerException();
		return regFormPath+escapeKey(key);
	}

	public String getFull()
	{
		if(root == null || subPath == null)
			throw new NullPointerException();
		if(!subPath.startsWith("\\"))
			subPath = "\\"+subPath;
		if(!subPath.endsWith("\\") && !key.startsWith("\\"))
			subPath = subPath+"\\";
		if(key == null)
			throw new NullPointerException();
		return root+subPath+key;				
	}
}