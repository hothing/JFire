package org.nightlabs.jfire.web.demoshop;

import javax.jdo.FetchPlan;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Khaled - khaled[at]nightlabs[dot]de
 * @version $Version$ - $Date$
 */
public class Util
{

	public static String getParameter(HttpServletRequest request, String name)
	{
		String x = request.getParameter(name);
		if(x == null || "".equals(x))
			throw new IllegalArgumentException("Invalid parameter: "+name);
		return x;
	}
	
	public static boolean haveParameter(HttpServletRequest request, String name)
	{
		try {
			getParameter(request, name);
			return true;
		} catch(Throwable e) {
			return false;
		}
	}

	public static int getParameterAsInt(HttpServletRequest request, String name)
	{
		try {
			return Integer.parseInt(getParameter(request, name));
		} catch(Throwable e) {
			throw new IllegalArgumentException("Invalid parameter: "+name, e);
		}
	}

	public static boolean haveParameterAsInt(HttpServletRequest request, String name)
	{
		try {
			getParameterAsInt(request, name);
			return true;
		} catch(Throwable e) {
			return false;
		}
	}


	public static String[] getFetchPlan(String... fetchGroups)
	{
		String[] fetchPlan = new String[fetchGroups.length + 1];
		System.arraycopy(fetchGroups, 0, fetchPlan, 0, fetchGroups.length);
		fetchPlan[fetchPlan.length-1] = FetchPlan.DEFAULT;
		return fetchPlan;
	}
	
	/**
	 * Generates a random password String with the given length
	 * @param n The password length
	 * @return The random Password
	 */
	public static String generateRandomPassword(int n) {
		char[] pw = new char[n];
		int c  = 'A';
		int  r1 = 0;
		for (int i=0; i < n; i++)
		{
			r1 = (int)(Math.random() * 3);
			switch(r1) {
			case 0: c = '0' +  (int)(Math.random() * 10); break;
			case 1: c = 'a' +  (int)(Math.random() * 26); break;
			case 2: c = 'A' +  (int)(Math.random() * 26); break;
			}
			pw[i] = (char)c;
		}
		return new String(pw);
	}

	public static boolean isValidEmailAddress(String email) {
		String regex = "^[^@]+@([0-9a-zA-Z\\-]+\\.)+[a-zA-Z]+$";
		return email.matches(regex);
	}
}
