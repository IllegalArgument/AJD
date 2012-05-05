package classfile;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import analysis.MethodAnalyzer;
import classfile.struct.ClassStruct;

public class ClassStore {

	private static Map<ClassReference, JavaClass> classes = new HashMap<>();
	private static Map<String, Set<Path>> paths = new HashMap<>();

	private static final ClassVisitor VISITOR = new ClassVisitor();

	public static JavaClass findClass(String className) {
		return findClass(ClassReference.fromName(className));
	}

	public static JavaClass findClass(ClassReference classReference) {
		if (classes.containsKey(classReference)) {
			return classes.get(classReference);
		} else {
			String name = classReference.className;
			name = name.substring(name.lastIndexOf('.') + 1);
			if (paths.containsKey(name)) {
				for (Path file : paths.get(name)) {
					JavaClass clazz;
					try {
						clazz = loadClass(file);
						if (clazz.thisType.equals(classReference)) {
							return clazz;
						}
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				}
				return null;
			} else {
				return null;
			}
		}
	}

	public static void addSearchPath(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			Files.walkFileTree(path, VISITOR);
		} else {
			Files.walkFileTree(FileSystems.newFileSystem(path, null).getPath("/"), VISITOR);
		}
	}

	public static void clear() {
		classes.clear();
	}

	private static JavaClass loadClass(Path file) throws IOException {
		ByteBuffer data = ByteBuffer.wrap(Files.readAllBytes(file));
		JavaClass clazz = new JavaClass(new ClassStruct().read(data));
		classes.put(clazz.thisType, clazz);
		return clazz;
	}

	private static final class ClassVisitor extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			PathMatcher matcher = file.getFileSystem().getPathMatcher("glob:*.class");
			if (!attrs.isDirectory() && matcher.matches(file.getFileName())) {
				String fileName = file.getFileName().toString();
				fileName = fileName.substring(0, fileName.lastIndexOf('.'));
				if (paths.containsKey(fileName)) {
					paths.get(fileName).add(file);
				} else {
					Set<Path> pathSet = new HashSet<>();
					pathSet.add(file);
					paths.put(fileName, pathSet);
				}
			}
			return FileVisitResult.CONTINUE;
		}
	}

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		Path rs = FileSystems.getDefault().getPath("jars/rs_optimus.jar");
		Path allatori = FileSystems.getDefault().getPath("jars/allatori.jar");
		Path rt = FileSystems.getDefault().getPath("jars/rt.jar");
		addSearchPath(rs);
		addSearchPath(allatori);
		addSearchPath(rt);
		long endTime = System.currentTimeMillis();
		System.out.println("Adding paths took " + (endTime - startTime) + " ms");
		startTime = System.currentTimeMillis();
		/*for (Set<Path> pathSet : paths.values()) {
			for (Path path : pathSet) {*/
				JavaClass clazz = findClass("aem");
				//System.out.println(new PrettyPrinter().print(clazz));
				for (JavaMethod method : clazz.methods.values()) {
					if (method.code != null) {
						//System.out.println(method.reference);
						MethodAnalyzer analyzer = new MethodAnalyzer(method);
						String gml = analyzer.dataGML();
						String file = "graphs/" + method.reference.toString().replaceAll("[\\[\\]()<>:]", ".");
						file = file.length() > 64 ? file.substring(0, 64) : file;
						file += ".gml";
						PrintWriter p = new PrintWriter(file);
						p.print(gml);
						p.flush();
						p.close();
					}
				}
		/*	}
		}*/
		endTime = System.currentTimeMillis();
		System.out.println("Total analysis took " + (endTime - startTime) + " ms");
	}

}
