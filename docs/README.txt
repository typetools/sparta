To make the manuals, run
  make

You need to have the following programs installed:
  dia
  epstopdf
  hevea
  hg
  latex

At least the first time, you need an Internet connection (to retrieve the
bibliographies).

After the first time, Run make NOHG=t if you do want to retrive the bibliographies.

Every section must have a label that starts with "sec:" inside the curly braces:
\section{Overview\label{sec:overview}}


