class Simple {
    void m() {
        try {
            //:: error: (methodcall)
            Class.forName("bad.Class");
            //:: error: (methodcall)
            this.getClass().forName("Ha");
        } catch (Exception e) {}
    }
}