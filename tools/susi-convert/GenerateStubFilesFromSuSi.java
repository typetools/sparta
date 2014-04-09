import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sparta.checkers.quals.FlowPermission;
/**
 * Class for parsing SuSi output files and generating stub files.
 * usage: javac GenerateStubFilesFromSuSi inputfile [moreinputfiles] > susi.astub
 * (sparta.jar needs to be on the class path)
 * 
 * Input files can be found in https://github.com/secure-software-engineering/SuSi
 * SourceSinkLists/Android\ 4.2/SourcesSinks/
 * @author smillst
 *
 */
public class GenerateStubFilesFromSuSi {
	public static final Map<String, List<SuSiMethod>> map = new HashMap<>();

	public static void main(String[] args) {
		String usage = "usage: javac GenerateStubFilesFromSuSi inputfile [more files]";
		if (args.length < 1) {
			System.out.println(usage);
			System.exit(0);
		}
		for (String file : args) {
			readFile(file);
		}
		printStubs();
	}
	private static void readFile(String filename) {
		try (Scanner scan = new Scanner(new File(filename));) {
			while (scan.hasNextLine()) {
				String line = scan.nextLine().trim();
				if (line.equals("")) {
					continue;
				}
				Pattern pat = Pattern.compile("<(.*):(.*)>(.*)\\((.*)\\)");
				Matcher mat = pat.matcher(line);
				if (mat.matches()) {
					SuSiMethod susi = new SuSiMethod(mat.group(1),
							mat.group(2), mat.group(3), mat.group(4));
					List<SuSiMethod> methods;
					if (map.containsKey(susi.getPckg())) {
						methods = map.get(susi.getPckg());
					} else {
						methods = new ArrayList<SuSiMethod>();
						map.put(susi.getPckg(), methods);
					}
					if (!susi.method.contains("<init>"))
						methods.add(susi);
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void printStubs() {
		StringBuilder header = new StringBuilder();
		header.append("/*\n");
		header.append("This file was generated using tools/susi"+GenerateStubFilesFromSuSi.class);
		header.append("*/\n");
		
		
		print(header.toString());
		for (List<SuSiMethod> methods : map.values()) {
			if (methods.size() > 0)
				printPackage(methods);
		}

	}

	private static void printPackage(List<SuSiMethod> methods) {
		String pkg = methods.get(0).getPckg();
		print("\npackage " + pkg + ";\n");
		Comparator<SuSiMethod> comp = new Comparator<SuSiMethod>() {
			@Override
			public int compare(SuSiMethod o1, SuSiMethod o2) {
				return o1.getClazz().compareTo(o2.getClazz());
			}
		};
		Collections.sort(methods, comp);
		String currentclass = "";
		for (SuSiMethod method : methods) {
			if (!method.getClazz().equals(currentclass)) {
				if (!currentclass.equals("")) {
					print("}");
				}
				currentclass = method.getClazz();
				print("class " + currentclass + "{");
			}
			printMethod(method);
		}
		print("}");
	}

	private static void printMethod(SuSiMethod method) {

		String methodString = method.method;
		if (methodString.contains("<init>"))
			return;

		int openP = methodString.indexOf("(");
		int closeP = methodString.indexOf(")");
		String pre = methodString.substring(0, openP + 1);
		String post = methodString.substring(closeP);
		String[] params = methodString.substring(openP + 1, closeP).split(",");

		String parString = addParamAnnotations(method, params);
		String sourceAnno = getSourceAnno(pre, method);

		print("   " + sourceAnno + pre + parString + post + ";");

	}


	private static String addParamAnnotations(SuSiMethod method, String[] params) {
		String[] newParams = new String[params.length];
		int i = 0;
		StringBuilder paramBuilder = new StringBuilder(" ");

		for (String param : params) {
			if (!param.equals("")) {
				String sink = getSinkAnnotation(method);
				paramBuilder.append(sink + param + " arg" + i + ",");
			}
			i++;
		}

		String paramString = paramBuilder.toString();
		String parString = paramString.substring(0, paramString.length() - 1);
		return parString;
	}

	private static String getSinkAnnotation(SuSiMethod method) {
		if (method.susi.isSink()) {

			return " @Sink(" + method.getSusi() + ") ";
		} else {
			return " ";
		}
	}
	private static String getSourceAnno(String pre, SuSiMethod method) {
		if (pre.contains("void") || !method.getSusi().isSource()) {
			return "";
		}
		return "@Source(" + method.getSusi() + ") ";
	}

	private static void print(String pkg) {
		System.out.println(pkg);
	}

	

}

class SuSiMethod {
	String pckg;
	String clazz;
	String method;
	String[] permissions;
	FlowPermission susi;

	public SuSiMethod(String packClass, String method, String permissions,
			String susi) {
		super();
		this.pckg = packClass.substring(0, packClass.lastIndexOf('.'));
		this.clazz = packClass.substring(packClass.lastIndexOf('.') + 1);
		this.method = method;
		this.permissions = permissions.split(",");
		this.susi = FlowPermission.valueOf("SUSI_" + susi);
	}

	public String getPckg() {
		return pckg;
	}

	public String getClazz() {
		return clazz;
	}

	public String getMethod() {
		return method;
	}

	public String[] getPermissions() {
		return permissions;
	}

	public FlowPermission getSusi() {
		return susi;
	}

	@Override
	public String toString() {
		return "SuSiMethod [pckg=" + pckg + ", clazz=" + clazz + ", method="
				+ method + ", permissions=" + Arrays.toString(permissions)
				+ ", susi=" + susi + "]";
	}

}
