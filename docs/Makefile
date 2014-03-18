# This is a nearly unmodified version of the
# Checker Framework manual Makefile.

# Put user-specific changes in your own Makefile.user.
# Make will silently continue if that file does not exist.
-include Makefile.user

PLUME_LIB ?= ../../plume-lib
CHECKERFRAMEWORK ?= ../../checker-framework/checkers

# To regenerate this list, run:   latex-process-inputs -makefilelist manual.tex
TEX_FILES = \
manual.tex \
manual-style.tex \
introduction.tex \
installation.tex \
flow.tex \
annotated-app.tex \
app-annotation.tex \
appanalysis.tex \
rules-of-engagement.tex \
sparta-internals.tex

all: figures-all manual.pdf manual.html # javadoc

.PHONY: figures-all
figures-all:
	${MAKE} -C figures all

# TODO: once Javadoc is enabled
#javadoc:
#	ln -sf ../javadoc doc

manual.dvi: ${TEX_FILES} bib-update
	$(MAKE) -C figures
	latex manual.tex
	bibtex manual
	latex manual.tex
	while grep "Rerun to get" manual.log; do latex manual.tex; done

manual.pdf: manual.dvi bib-update
	pdflatex manual.tex
	bibtex manual
	pdflatex manual.tex
	while grep "Rerun to get" manual.log; do pdflatex manual.tex; done

manual.ps: manual.dvi
	dvips -P cmz -t letter -o manual.ps manual.dvi

html: manual.html
manual.html: manual.dvi
	hevea -fix -exec xxdate.exe urlhref.hva manual.tex
	hevea -exec xxdate.exe urlhref.hva manual.tex
	\mv -f manual.html manual.html-with-htoc
	./hevea-retarget-crossrefs < manual.html-with-htoc > manual.html
	\rm -f manual.html-with-htoc
	perl -pi -e 's/\/&#XA0;/\/~/' manual.html
#	perl -pi -e 's/(<!--TOC section Introduction-->)/$$1\n\n<p>Contents:<\/p>\n<!-- start toc.  do not edit; run html-update-toc instead -->\n<!-- end toc -->\n\n<br>\n/' manual.html
#	html-update-toc manual.html

export BIBINPUTS = $BIBINPUTS:.:bib

bib:
	hg clone https://code.google.com/p/plume-bib/ bib
.PHONY: bib-update
bib-update: bib
# To skip running the hg command, invoke make like this:  make NOHG=t ...
ifndef NOHG
	cd bib && hg pull -u && make
endif

# Update files that were copied from elsewhere to make the manual
# self-contained and able to be built without first installing (which
# requires one to read the manual...)
copies-up-to-date:
ifeq "$(wildcard ${CHECKERFRAMEWORK}/manual/manual-style.tex)" "${CHECKERFRAMEWORK}/manual/manual-style.tex"
# file exists
	cp -pf ${CHECKERFRAMEWORK}/manual/manual-style.tex .
endif
ifeq "$(wildcard ${PLUME_LIB}/bin/hevea-retarget-crossrefs)" "${PLUME_LIB}/bin/hevea-retarget-crossrefs"
# file exists
	cp -pf ${PLUME_LIB}/bin/hevea-retarget-crossrefs .
endif

clean:
	@\rm -f doc *.aux *.blg *.haux *.htoc *.log *.pdf *.ps *.dvi *.idx *.ind *.ilg *.toc

very_clean: clean
	@\rm -f manual.html
	${MAKE} -C figures clean

.PHONY: checklink
checklink:
	${INV}/scripts/checklink -q -e `cat ${INV}/scripts/checklink-args.txt` manual.html

tags: TAGS
TAGS: ${TEX_FILES}
	etags ${TEX_FILES}
