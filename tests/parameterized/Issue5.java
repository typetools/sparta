import sparta.checkers.quals.*;

class Test {
    @Source("READ_SMS(hello)") @Sink("DISPLAY") String s = "hello";
    @Source("READ_TIME") String readTime = "hello";

    void foo(@Sink("DISPLAY") String display) {
    }

    void setReadTime(@Source("READ_TIME") @Sink("INTERNET(uw.edu)") String display) {
    }

    void bar() {
        setReadTime(readTime);
        foo(s);
    }
}
