import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.List;




class Method{
	List<String> perms = new ArrayList<String>();
	String method;
	String classname;
	public Method(String method, String classname){
	    this.method = method;
	  
	    String[] s = classname.split("\\.");
	    this.classname = s[s.length-1];
	}
	public void addPermission(String perm){
	    String modPerm = "\""+perm+"\"";
	    perms.add(modPerm);
	}
	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result
		    + ((method == null) ? 0 : method.hashCode());
	    return result;
	}
	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    Method other = (Method) obj;
	    if (method == null) {
		if (other.method != null)
		    return false;
	    } else if (!method.equals(other.method))
		return false;
	    return true;
	}
	@Override
	public String toString() {
	    String stubmethod = convertMethod(method);
	    
	    String print = perms.toString().replace('[', '{');
	    print = print.replace(']', '}');
	    print =  "@RequiredPermissions(" + print + ") " + stubmethod+";";
	    return print ;
	}
	private String convertMethod(String m) {
	    int begin = m.indexOf("(");
	    String params = m.substring(begin+1,m.length()-1);
	    String newM = m.substring(0,begin+1);
	    int i=0;
	    for(String par: params.split(",")){
		if(par.equals("")) break;
		if(i>0){
		    newM+=", ";
		}
		if(par.contains("$")){
		  par =  par.substring(par.lastIndexOf("$")+1,par.length());
		}
		newM+= par+" arg"+i;
		i++;
	    }
	    newM+=")";
	    m = newM;
	    return m.replace("void <init>", classname);
	}
}
public class PermissionMap {


public static void main(String[] args) {
   Map<String, List<Method>> packages = new HashMap<String, List<Method>>();
   try {
    Scanner scan = new Scanner(new File(args[0]));
	String currentPerm = "";

    while(scan.hasNextLine()){
	String	line = scan.nextLine();

	String perm = getPerm(line);
	    String packageN = getPackage(line);
	    String method = getMethod(line);
	if(method != null){
	    Method m = getMethod(packageN,method, packages);
	    m.addPermission(currentPerm);
	}else if(perm != null){
	    currentPerm = perm;
	}

    }
    System.out.println("import sparta.checkers.quals.*;");
    printPerm(packages);
    
//    Permission:android.permission.CHANGE_WIFI_STATE
//    16 Callers:
//    <android.net.wifi.WifiManager: boolean reassociate()>
} catch (FileNotFoundException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
} 
   
}
public static
<T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
  List<T> list = new ArrayList<T>(c);
  java.util.Collections.sort(list);
  return list;
}
private static void printPerm(Map<String, List<Method>> packages) {  
    List<String> classes = asSortedList(packages.keySet());
    String cpackage = "XXXXX";
    for(String classs:classes){
	int i = classs.lastIndexOf(".");
	String newpackage = classs.substring(0,i);
	if(!newpackage.equals(cpackage)){
	    cpackage = newpackage;
	    System.out.println("package "+cpackage+";");
	}
	System.out.println("class "+classs.substring(i+1,classs.length())+"{");
	printClasses(packages.get(classs));
	System.out.println("}");
    }
    
}
private static void printClasses(List<Method> methods) {
    for(Method m: methods){
	System.out.println(m.toString());
	
    }
    
}
private static Method getMethod(String packageN, String method,
	Map<String, List<Method>> packages) {
   List<Method> methods = packages.get(packageN);
   if(methods == null){
       methods = new ArrayList<Method>();
       packages.put(packageN, methods);
   }
   Method m = new Method(method, packageN);
   int index = methods.indexOf(m);
   if (index !=-1){
       m = methods.get(index);
   }else{
       methods.add(m);
   }
    return m;
}

private static String getPackage(String line) {
    Pattern pattern = Pattern.compile("<(.*):.*>.*");
    Matcher mat = pattern.matcher(line);
    if(mat.matches()){
	 return mat.group(1);
    }
    return null;
}

private static String getMethod(String line) {
    Pattern pattern = Pattern.compile("<.*:(.*)>.*");
    Matcher mat = pattern.matcher(line);
    if(mat.matches()){
	 return mat.group(1);
    }
    return null;
}

private static String getPerm(String line) {
    Pattern permissionPat = Pattern.compile("Permission:(.*)");
    Matcher permissionMat = permissionPat.matcher(line);
    if(permissionMat.matches()){
	 return permissionMat.group(1);
    }
    return null;
}

}
