package test;

import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.TargetFactory;
import com.izforge.izpack.util.os.Shortcut;

public class Test {
	public static void main(String[] args) throws Exception
	{
////		ShellLink sl = new ShellLink(ShellLink.START_MENU, ShellLink.CURRENT_USER, "Test", "name");
//		ShellLink sl = new ShellLink(ShellLink.START_MENU, "name");
//		sl.setTargetPath("C:\\Programme\\eclipse\\eclipse.exe");
////		sl.setLinkName("linkName");
//		sl.save();
//		//sl.save("C:\\Dokumente und Einstellungen\\Marc\\Desktop\\linkFileName.lnk");
		
		//Librarian.getInstance().setNativeDirectory("native");
		
		Shortcut shortcut = (Shortcut)TargetFactory.getInstance().makeObject(Shortcut.class.getName());
		
		shortcut.initialize(Shortcut.DESKTOP, "Mein neues Eclipse");
		//shortcut.setUserType(Shortcut.ALL_USERS);
		//shortcut.setProgramGroup("Test");
		shortcut.setDescription("Meine Beschreibung mit € Zeichen");
		shortcut.setTargetPath("C:\\Programme\\eclipse\\eclipse.exe");
		shortcut.save();
	}
}
