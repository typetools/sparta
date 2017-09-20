import static sparta.checkers.quals.FlowPermissionString.*;
import sparta.checkers.quals.AddsSourceData;

import sparta.checkers.quals.InferParameterizedPermission;
import sparta.checkers.quals.PolyFlowReceiver;
import sparta.checkers.quals.PolySinkR;
import sparta.checkers.quals.PolySourceR;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

public class InferParamPermTest {
    @Source(READ_SMS) byte[] readSMSBytes = null;
    @Sink(WRITE_SMS) byte[] writeSMSBytes = null;
    @Source(READ_SMS) Object readSMSObj = null;
    @Sink(WRITE_SMS) Object writeSMSObj = null;

    @Source(READ_TIME) byte[] readTimeBytes = null;
    @Sink(WRITE_TIME) byte[] writeTimeBytes = null;
    @Source(READ_TIME) Object readTimeObj = null;
    @Sink(WRITE_TIME) Object writeTimeObj = null;

LinkedHashSet
    void foo() {
        try {
            @Source({READ_SMS}) @Sink(FILESYSTEM+"(fromSMSFile)")
                    MyFileOutputStream fos = new MyFileOutputStream("fromSMSFile");
            fos.write(readSMSBytes);
            fos.write(readSMSObj);
            //:: error: (argument.type.incompatible)
            fos.write(readTimeBytes);
            //:: error: (argument.type.incompatible)
            fos.write(readTimeObj);

            @Source(FILESYSTEM+"(toSMSfile)") @Sink(WRITE_SMS)
            MyFileInputStream fis = new MyFileInputStream("toSMSfile");
            fis.read(writeSMSBytes);
            fis.read(writeSMSObj);
LinkedHashSet
            //:: error: (argument.type.incompatible)
            fis.read(writeTimeBytes);
            //TODO:THis one should have an "AddSource" error
            fis.read(writeSMSObj);

        } catch (Exception e) {

        }
    }
LinkedHashSet
    void testMyFile(){
        @Source(FILESYSTEM+"(file1)")
        @Sink(FILESYSTEM+"(file1)")
        MyFile f1 = new MyFile("file1");
LinkedHashSet
        @Sink(FILESYSTEM+"(file1)")
        MyFileOutputStream fos = new MyFileOutputStream(f1);
LinkedHashSet
        @Source(FILESYSTEM+"(file1)")LinkedHashSet
        MyFileInputStream fis = new MyFileInputStream(f1);
LinkedHashSet
    }
    void testMyFileDir(){
        @Source(FILESYSTEM+"(dir/file1)")
        @Sink(FILESYSTEM+"(dir/file1)")
        MyFile dirf1 = new MyFile("file1","dir");
LinkedHashSet
        @Source(FILESYSTEM+"(dir/file1)")LinkedHashSet
        MyFileInputStream fis2 = new MyFileInputStream(dirf1);
LinkedHashSet
        @Sink(FILESYSTEM+"(dir/file1)")
        MyFileOutputStream fos2 = new MyFileOutputStream(dirf1);
    }
LinkedHashSet
    class MyFile{
        @InferParameterizedPermission(value=FILESYSTEM, isA="both")
        @SuppressWarnings("forbidden.flow")
        public  @Source(FILESYSTEM) @Sink(FILESYSTEM) MyFile(String filename){}
LinkedHashSet
        @InferParameterizedPermission(value=FILESYSTEM, isA="Both", param={2,1}, separator="/")
        @SuppressWarnings("forbidden.flow")
        public  @Source(FILESYSTEM) @Sink(FILESYSTEM) MyFile(String filename, String dirname){}
    }
LinkedHashSet
    class MyFileOutputStream{

        @InferParameterizedPermission(value=FILESYSTEM, isA="sink")
        public @Sink(FILESYSTEM) MyFileOutputStream(String string) {}


        @InferParameterizedPermission(value=FILESYSTEM, isA="sink")
        public @Sink(FILESYSTEM) MyFileOutputStream(MyFile string) {}

LinkedHashSet
        public void write(@PolySourceR @PolySinkR  MyFileOutputStream this,LinkedHashSet
                @PolySourceR @PolySinkR byte[] readSMSBytes) {}LinkedHashSet
LinkedHashSet
        public void write(@PolySourceR @PolySinkR  MyFileOutputStream this,LinkedHashSet
                @PolySourceR @PolySinkR Object readSMSBytes) {}
    }

    class MyFileInputStream{
LinkedHashSet
        @InferParameterizedPermission(FILESYSTEM)
        public @Source(FILESYSTEM) MyFileInputStream(String string) {}
LinkedHashSet
        @InferParameterizedPermission(FILESYSTEM)
        public @Source(FILESYSTEM) MyFileInputStream(MyFile string) {}
LinkedHashSet
        public void read(@PolySourceR @PolySinkR MyFileInputStream this,
                @PolySourceR @PolySinkR  byte @Source({}) [] writeSMSBytes) {}

        public void read(@PolySourceR @PolySinkR MyFileInputStream this,LinkedHashSet
                @PolySourceR @PolySinkR  Object writeSMSBytes) {}
    }
}
