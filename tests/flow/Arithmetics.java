import sparta.checkers.quals.FlowSources;

class Arithmetics {
  @FlowSources({FlowSources.FlowSource.CAMERA}) int cam;
  int clean;

  void m() {
    int i = 5;
    clean = i;

    int j = i + 2;
    clean = j;

    int x = i;
    x += 3;
    clean = x;
  }
}
