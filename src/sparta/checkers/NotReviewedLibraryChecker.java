package sparta.checkers;

import static sparta.checkers.FlowChecker.SPARTA_OUTPUT_DIR;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.qual.StubFiles;
import org.checkerframework.framework.qual.TypeQualifiers;
import org.checkerframework.framework.qual.Unqualified;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.source.SupportedOptions;
import org.checkerframework.framework.stub.StubGenerator;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.util.AnnotatedTypes;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.InternalUtils;
import org.checkerframework.javacutil.TreeUtils;

import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;

/**
 * This checker outputs a warning every location a method, constructor, or field
 * that has not been reviewed is used.
 * 
 * Not reviewed is defined as being absent from the information_flow.astub file.
 * 
 * This checker also prints a stub file with all of the methods, constructors,
 * and fields that need to be reviewed. This stub file can be annotated and
 * passed to the Flow Checker.
 * 
 * This checker has options to change the file name and directory of the stub
 * file. The default is sparta-out/missingAPI.astub in the user directory. This
 * checker also has an option to print the number of times not reviewed method,
 * constructor, or field is used in the stub file.
 * 
 * -AsuppressWarnings surpresses all warnings, but the missingAPI file will 
 * still be created.
 * 
 * @author smillst
 * 
 */
@StubFiles("information_flow.astub")
@SupportedOptions({ NotReviewedLibraryChecker.OUTPUT_DIR_OPTION,
        NotReviewedLibraryChecker.OUTPUT_FILE_OPTION,
        NotReviewedLibraryChecker.PRINT_FREQUENCY_OPTION })
@TypeQualifiers({ Unqualified.class })
public class NotReviewedLibraryChecker extends BaseTypeChecker {
    public static final String OUTPUT_DIR_OPTION = "outputdir";
    public static final String OUTPUT_FILE_OPTION = "outputfile";
    public static final String PRINT_FREQUENCY_OPTION = "printFreq";
    private final String defaultOutputFile = "missingAPI.astub";

    private Map<String, Map<String, Map<Element, Integer>>> notInStubFile = new HashMap<>();

    private String outputMissingFile;
    private boolean printFrequency;
    private String outputMissingDir;

    public NotReviewedLibraryChecker() {
        super();
    }

    @Override
    public void initChecker() {
        super.initChecker();
        outputMissingDir = this.getOption(OUTPUT_DIR_OPTION, SPARTA_OUTPUT_DIR);
        outputMissingFile = outputMissingDir + File.separator
                + this.getOption(OUTPUT_FILE_OPTION, defaultOutputFile);
        printFrequency = this.getOptions().containsKey(PRINT_FREQUENCY_OPTION);
    }

    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new NotReviewedLibraryVisitor(this);
    }

    @Override
    public void typeProcessingOver() {
        File outputDir = new File(outputMissingDir);
        boolean dirCreated = true;
        if (!outputDir.exists()) {
            dirCreated = outputDir.mkdir();
        }
        if (dirCreated && outputDir.isDirectory()) {
            printMethods();
        }
    }

    private void printMethods() {
        if (notInStubFile.isEmpty()) {
            report(Kind.NOTE, "All methods reviewed.");
            return;
        }

        int methodCount = 0;
        try (PrintStream out = new PrintStream(new File(outputMissingFile))) {
            for (String pack : notInStubFile.keySet()) {
                out.println("package " + pack + ";");
                for (String clss : notInStubFile.get(pack).keySet()) {
                    out.println("class " + clss + "{");
                    Map<Element, Integer> map = notInStubFile.get(pack).get(
                            clss);
                    for (Element element : map.keySet()) {
                        StubGenerator stubGen = new StubGenerator(out);
                        if (printFrequency)
                            out.println("    //" + map.get(element));
                        stubGen.skeletonFromMethod(element);
                        stubGen.skeletonFromField(element);
                        methodCount++;
                    }
                    out.println("}");
                }
            }
            String s_have = methodCount > 1 ? "s have" : " has";
            report(Kind.WARNING, methodCount + " byte code method" + s_have
                    + " not been reviewed.");
        } catch (FileNotFoundException e) {
            report(Kind.ERROR, "File not found: " + outputMissingFile);
        }

    }

    public void report(Kind kind, String message) {
        if (!this.getOptions().containsKey("suppressWarnings"))
            processingEnv.getMessager().printMessage(kind, message);
    }

    /**
     * Adds the element to list of methods that need to be added to the stub
     * file and reviewed
     * 
     * @param element
     *            element that needs to be reviewed
     */
    void notAnnotated(final Element element) {
        String pkg = ElementUtils.getVerboseName(ElementUtils
                .enclosingPackage(element));
        String clss = getClassName(element);

        Map<String, Map<Element, Integer>> classmap = this.notInStubFile
                .get(pkg);
        if (classmap == null) {
            classmap = new HashMap<>();
            this.notInStubFile.put(pkg, classmap);
        }
        Map<Element, Integer> elementmap = classmap.get(clss);
        if (elementmap == null) {
            elementmap = new HashMap<Element, Integer>();
            classmap.put(clss, elementmap);
        }

        if (elementmap.containsKey(element)) {
            Integer i = elementmap.get(element);
            i++;
            elementmap.put(element, i);

        } else {
            elementmap.put(element, 1);
        }
    }

    private String getClassName(Element element) {
        TypeElement typeElement = ElementUtils.enclosingClass(element);
        String pck = ElementUtils.getVerboseName(ElementUtils
                .enclosingPackage(element));

        String fullClassName = ElementUtils.getQualifiedClassName(typeElement)
                .toString();
        String className = fullClassName.substring(fullClassName.indexOf(pck)
                + pck.length() + 1);
        return className.replace('.', '$');
    }
}

class NotReviewedLibraryVisitor extends
        BaseTypeVisitor<BaseAnnotatedTypeFactory> {

    public NotReviewedLibraryVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree tree, Void p) {
        ExecutableElement methodElt = TreeUtils.elementFromUse(tree);
        warnNotReviewed(tree, methodElt, "not.reviewed");
        return super.visitMethodInvocation(tree, p);
    }

    @Override
    public Void visitNewClass(NewClassTree node, Void p) {
        ExecutableElement methodElt = TreeUtils.elementFromUse(node);
        warnNotReviewed(node, methodElt, "not.reviewed");
        return super.visitNewClass(node, p);
    }

    @Override
    public Void visitMethod(MethodTree node, Void p) {
        Map<AnnotatedDeclaredType, ExecutableElement> overriddenMethods = AnnotatedTypes
                .overriddenMethods(elements, atypeFactory,
                        TreeUtils.elementFromDeclaration(node));
        for (ExecutableElement methodElt : overriddenMethods.values())
            warnNotReviewed(node, methodElt, "not.reviewed.overrides");
        return super.visitMethod(node, p);
    }

    @Override
    public Void visitMemberSelect(MemberSelectTree node, Void p) {
        Element e = InternalUtils.symbol(node);
        if (e.getKind() == ElementKind.FIELD) {
            warnNotReviewed(node, InternalUtils.symbol(node), "not.reviewed");
        }
        return super.visitMemberSelect(node, p);
    }

    private void warnNotReviewed(Tree callingTree, Element byteCodeEle,
            String errorKey) {
        if (atypeFactory.isFromByteCode(byteCodeEle)) {
            checker.report(
                    Result.warning(errorKey,
                            ElementUtils.getVerboseName(byteCodeEle)),
                    callingTree);
            ((NotReviewedLibraryChecker) checker).notAnnotated(byteCodeEle);
        }
    }

}
