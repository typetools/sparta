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

            //:: error: (argument.type.incompatible)
            fis.read(writeTimeBytes);
            //TODO:THis one should have an "AddSource" error
            fis.read(writeSMSObj);

        } catch (Exception e) {

        }
    }

    void testMyFile(){
        @Source(FILESYSTEM+"(file1)")
        @Sink(FILESYSTEM+"(file1)")
        MyFile f1 = new MyFile("file1");

        @Sink(FILESYSTEM+"(file1)")
        MyFileOutputStream fos = new MyFileOutputStream(f1);

        @Source(FILESYSTEM+"(file1)")
        MyFileInputStream fis = new MyFileInputStream(f1);

    }
    void testMyFileDir(){
        @Source(FILESYSTEM+"(dir/file1)")
        @Sink(FILESYSTEM+"(dir/file1)")
        MyFile dirf1 = new MyFile("file1","dir");

        @Source(FILESYSTEM+"(dir/file1)")
        MyFileInputStream fis2 = new MyFileInputStream(dirf1);

        @Sink(FILESYSTEM+"(dir/file1)")
        MyFileOutputStream fos2 = new MyFileOutputStream(dirf1);
    }

    class MyFile{
        @InferParameterizedPermission(value=FILESYSTEM, isA="both")
        @SuppressWarnings("forbidden.flow")
        public  @Source(FILESYSTEM) @Sink(FILESYSTEM) MyFile(String filename){}

        @InferParameterizedPermission(value=FILESYSTEM, isA="Both", param={2,1}, separator="/")
        @SuppressWarnings("forbidden.flow")
        public  @Source(FILESYSTEM) @Sink(FILESYSTEM) MyFile(String filename, String dirname){}
    }

    class MyFileOutputStream{

        @InferParameterizedPermission(value=FILESYSTEM, isA="sink")
        public @Sink(FILESYSTEM) MyFileOutputStream(String string) {}


        @InferParameterizedPermission(value=FILESYSTEM, isA="sink")
        public @Sink(FILESYSTEM) MyFileOutputStream(MyFile string) {}


        public void write(@PolySourceR @PolySinkR  MyFileOutputStream this,
                @PolySourceR @PolySinkR byte[] readSMSBytes) {}

        public void write(@PolySourceR @PolySinkR  MyFileOutputStream this,
                @PolySourceR @PolySinkR Object readSMSBytes) {}
    }

    class MyFileInputStream{

        @InferParameterizedPermission(FILESYSTEM)
        public @Source(FILESYSTEM) MyFileInputStream(String string) {}

        @InferParameterizedPermission(FILESYSTEM)
        public @Source(FILESYSTEM) MyFileInputStream(MyFile string) {}

        public void read(@PolySourceR @PolySinkR MyFileInputStream this,
                @PolySourceR @PolySinkR  byte @Source({}) [] writeSMSBytes) {}

        public void read(@PolySourceR @PolySinkR MyFileInputStream this,
                @PolySourceR @PolySinkR  Object writeSMSBytes) {}
    }
}
